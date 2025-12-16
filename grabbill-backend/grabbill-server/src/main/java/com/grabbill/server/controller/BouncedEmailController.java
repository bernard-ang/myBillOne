package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.BouncedEmail;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.BouncedEmailService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.BouncedEmailPayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/bounced-emails")
public class BouncedEmailController {

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    private BouncedEmailService bouncedEmailService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> filterBouncedEmails(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Account account = userDetails.getUser().getAccount();
        Page<BouncedEmail> page = bouncedEmailService.searchByFilters(
                account.getId(),
                email,
                startDate,
                endDate,
                pageable
        );

        SearchResultPayload<BouncedEmailPayload> searchResultPayload =
                SearchResultPayload.<BouncedEmailPayload>builder()
                        .items(page.get()
                                .map(BouncedEmailPayload::from)
                                .collect(Collectors.toList())
                        )
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
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<GrabbillApiResponse> delete(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long id
    ) {
        BouncedEmail target = getBouncedEmail(userDetails.getUser(), id);

        bouncedEmailService.delete(target);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                target.getId(),
                getDomainType(),
                ActionType.DELETE,
                target.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Bounced email with id [" + id + "] removed successfully.")
                )
        );
    }

    private BouncedEmail getBouncedEmail(
            final User user,
            final Long typeId
    ) {
        return bouncedEmailService.getById(user, typeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB5001,
                        "Bounced email of id [" + typeId + "] not found!"
                )
        );
    }

    public DomainType getDomainType() {
        return DomainType.BOUNCED_EMAIL;
    }
}
