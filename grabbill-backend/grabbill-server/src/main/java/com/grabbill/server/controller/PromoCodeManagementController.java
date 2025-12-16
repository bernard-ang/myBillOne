package com.grabbill.server.controller;

import com.grabbill.core.entity.PromoCode;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DiscountType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.PromoCodeService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.PromoCodeNewRequest;
import com.grabbill.server.controller.request.PromoCodeStatusUpdateRequest;
import com.grabbill.server.controller.request.PromoCodeUpdateRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.param.CouponCreateParams;
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
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/mgmt/promo-codes")
public class PromoCodeManagementController extends BaseManagementController {

    private static final String MYR = "MYR";

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PromoCodeService promoCodeService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> searchPromoCodes(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam(required = false) String code,
            @PageableDefault(sort = { "code" }, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        checkStatus(userDetails);

        Page<PromoCode> page = StringUtils.hasLength(code) ?
                promoCodeService.getAllByCode(code, pageable) :
                promoCodeService.getAll(pageable);

        SearchResultPayload<PromoCodeBasicPayload> searchResultPayload =
                SearchResultPayload.<PromoCodeBasicPayload>builder()
                        .items(page.get()
                                .map(PromoCodeBasicPayload::from)
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
    @GetMapping(path = "/{promoCodeId}")
    public ResponseEntity<GrabbillApiResponse> getPromoCodeDetails(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer promoCodeId
    ) {
        checkStatus(userDetails);

        PromoCode promoCode = promoCodeService.getById(promoCodeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1037,
                        "Promo code with ID [" + promoCodeId + "] is not found!"
                )
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        PromoCodeDetailsPayload.from(promoCode)
                )
        );
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newPromoCode(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @Valid @RequestBody PromoCodeNewRequest request
    ) {
        checkStatus(userDetails);

        Optional<PromoCode> promoCodeOptional = promoCodeService.getByCode(request.getCode());
        if (promoCodeOptional.isPresent()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1038,
                    "Promo code [" + request.getCode() + "] already exist!"
            );
        }

        CouponCreateParams.Builder couponBuilder = CouponCreateParams.builder()
                .setId(request.getCode())
                .setName(request.getName())
                .setCurrency(MYR)
                .setDuration(CouponCreateParams.Duration.FOREVER);
        if (DiscountType.ABSOLUTE_AMOUNT.equals(request.getDiscountType())) {
            couponBuilder.setAmountOff(request.getDiscount() * 100L);   // amount in cent
        } else if (DiscountType.PERCENTAGE.equals(request.getDiscountType())) {
            couponBuilder.setPercentOff(BigDecimal.valueOf(request.getDiscount()));
        } else {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1039,
                    "Invalid discount type of [" + request.getDiscountType() + "] for promo code!"
            );
        }

        try {
            Coupon.create(couponBuilder.build());
        } catch (StripeException e) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1040, "Discount coupon (Stripe) creation failed!", e);
        }

        PromoCode promoCode = new PromoCode();
        request.to(promoCode);
        PromoCode savedInstance = promoCodeService.save(promoCode);

        auditLogService.log(
                savedInstance.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                DomainType.PROMO_CODE,
                ActionType.CREATE,
                savedInstance.getCode(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        PromoCodeDetailsPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{promoCodeId}")
    public ResponseEntity<GrabbillApiResponse> updatePromoCode(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer promoCodeId,
            @Valid @RequestBody PromoCodeUpdateRequest request
    ) {
        checkStatus(userDetails);

        // no promo code with the given id
        PromoCode promoCode = promoCodeService.getById(promoCodeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1037,
                        "Promo code with ID [" + promoCodeId + "] is not found!"
                )
        );

        request.to(promoCode);
        PromoCode savedInstance = promoCodeService.save(promoCode);

        auditLogService.log(
                savedInstance.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                DomainType.PROMO_CODE,
                ActionType.UPDATE,
                savedInstance.getCode(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        PromoCodeDetailsPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{promoCodeId}/status")
    public ResponseEntity<GrabbillApiResponse> setPromoCodeStatus(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer promoCodeId,
            @Valid @RequestBody PromoCodeStatusUpdateRequest request
    ) {
        checkStatus(userDetails);

        // no promo code with the given id
        PromoCode promoCode = promoCodeService.getById(promoCodeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1037,
                        "Promo code with ID [" + promoCodeId + "] is not found!"
                )
        );

        promoCode.setActive(request.isActive());
        PromoCode savedInstance = promoCodeService.save(promoCode);

        auditLogService.log(
                savedInstance.getId(),
                Optional.empty(),
                savedInstance.getId().longValue(),
                DomainType.PROMO_CODE,
                ActionType.UPDATE,
                savedInstance.getCode(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        PromoCodeDetailsPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/{promoCodeId}")
    public ResponseEntity<GrabbillApiResponse> deletePromoCode(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer promoCodeId
    ) {
        checkStatus(userDetails);

        // no promo code with the given id
        PromoCode promoCode = promoCodeService.getById(promoCodeId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1037,
                        "Promo code with ID [" + promoCodeId + "] is not found!"
                )
        );

        try {
            Coupon.retrieve(promoCode.getCode()).delete();
        } catch (StripeException e) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1041, "Discount coupon (Stripe) deletion failed!", e);
        }

        promoCodeService.delete(promoCode);

        auditLogService.log(
                promoCode.getId(),
                Optional.empty(),
                promoCode.getId().longValue(),
                DomainType.PROMO_CODE,
                ActionType.DELETE,
                promoCode.getCode(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Promo code with id [" + promoCodeId + "] removed successfully.")
                )
        );
    }

}
