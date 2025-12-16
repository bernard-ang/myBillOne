package com.grabbill.server.controller;

import com.grabbill.core.entity.AffiliateCode;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.AccountService;
import com.grabbill.core.service.AffiliateCodeService;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.AffiliateCodeRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/mgmt/affiliate-codes")
public class AffiliateCodeManagementController extends BaseManagementController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AffiliateCodeService affiliateCodeService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> searchAffiliateCodes(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam(required = false) String code,
            @PageableDefault(sort = { "code" }, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        checkStatus(userDetails);

        Page<AffiliateCode> page = StringUtils.hasLength(code) ?
                affiliateCodeService.getAllByCode(code, pageable) :
                affiliateCodeService.getAll(pageable);

        SearchResultPayload<AffiliateCodeBasicPayload> searchResultPayload =
                SearchResultPayload.<AffiliateCodeBasicPayload>builder()
                        .items(page.get()
                                .map(AffiliateCodeBasicPayload::from)
                                .collect(Collectors.toList()))
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }

    @Transactional
    @GetMapping(path = "/{affiliateCodeId}")
    public ResponseEntity<GrabbillApiResponse> getAffiliateCodeDetails(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer affiliateCodeId
    ) {
        checkStatus(userDetails);

        AffiliateCode affiliateCode = affiliateCodeService.getById(affiliateCodeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1032,
                        "Affilicate code with ID [" + affiliateCodeId + "] is not found!"
                )
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AffiliateCodeDetailsPayload.from(affiliateCode)
                )
        );
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newAffiliateCode(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @Valid @RequestBody AffiliateCodeRequest request
    ) {
        checkStatus(userDetails);

        Optional<AffiliateCode> affiliateCodeOptional = affiliateCodeService.getByCode(request.getCode());
        if (affiliateCodeOptional.isPresent()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1033,
                    "Affilicate code [" + request.getCode() + "] already exist!"
            );
        }

        AffiliateCode affiliateCode = new AffiliateCode();
        request.to(affiliateCode);
        AffiliateCode savedInstance = affiliateCodeService.save(affiliateCode);

        auditLogService.log(
                savedInstance.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                DomainType.AFFILIATE_CODE,
                ActionType.CREATE,
                savedInstance.getCode(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AffiliateCodeDetailsPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{affiliateCodeId}")
    public ResponseEntity<GrabbillApiResponse> updateAffiliateCode(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer affiliateCodeId,
            @Valid @RequestBody AffiliateCodeRequest request
    ) {
        checkStatus(userDetails);

        // another affiliate code is using the updated code
        Optional<AffiliateCode> affiliateCodeOptional = affiliateCodeService.getByCode(request.getCode());
        if (affiliateCodeOptional.isPresent() && !Objects.equals(affiliateCodeOptional.get().getId(), affiliateCodeId)) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1033,
                    "Affilicate code [" + request.getCode() + "] already exist!"
            );
        }

        // no affiliate code with the given id
        AffiliateCode affiliateCode = affiliateCodeService.getById(affiliateCodeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1032,
                        "Affilicate code with ID [" + affiliateCodeId + "] is not found!"
                )
        );

        // affiliate code already referenced, cannot be changed
        if (!request.getCode().equals(affiliateCode.getCode())) {
            if (!accountService.getByAffiliateMasterCode(affiliateCode.getCode()).isEmpty()) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1034,
                        "Affilicate code [" + affiliateCode.getCode() + "] is referenced, not allowed to be changed!"
                );
            }
        }

        request.to(affiliateCode);
        AffiliateCode savedInstance = affiliateCodeService.save(affiliateCode);

        auditLogService.log(
                savedInstance.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                DomainType.AFFILIATE_CODE,
                ActionType.UPDATE,
                savedInstance.getCode(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AffiliateCodeDetailsPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/{affiliateCodeId}")
    public ResponseEntity<GrabbillApiResponse> deleteAffiliateCode(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer affiliateCodeId
    ) {
        checkStatus(userDetails);

        AffiliateCode affiliateCode = affiliateCodeService.getById(affiliateCodeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1032,
                        "Affilicate code with ID [" + affiliateCodeId + "] is not found!"
                )
        );

        if (!accountService.getByAffiliateMasterCode(affiliateCode.getCode()).isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1034,
                    "Affilicate code with ID [" + affiliateCodeId + "] is referenced, not removeable!"
            );
        }

        affiliateCodeService.delete(affiliateCode);

        auditLogService.log(
                affiliateCode.getId(),
                Optional.empty(),
                affiliateCode.getId().longValue(),
                DomainType.AFFILIATE_CODE,
                ActionType.DELETE,
                affiliateCode.getCode(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Affiliate code with id [" + affiliateCodeId + "] removed successfully.")
                )
        );
    }

    @GetMapping(path = "/generate-master-code")
    public ResponseEntity<GrabbillApiResponse> getAffiliateCodeDetails(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails
    ) {
        checkStatus(userDetails);

        String uniqueMasterCode = affiliateCodeService.generateUniqueCode();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AffiliateCodeUniqueMasterCodePayload.from(uniqueMasterCode)
                )
        );
    }

}
