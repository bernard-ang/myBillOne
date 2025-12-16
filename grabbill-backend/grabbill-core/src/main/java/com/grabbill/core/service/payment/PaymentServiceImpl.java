package com.grabbill.core.service.payment;

import com.grabbill.core.entity.*;
import com.grabbill.core.entity.Account;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.SubscriptionMode;
import com.grabbill.core.model.plan.PlanType;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.Invoice;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.InvoiceItemCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author michaellow
 */
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private ProductService productService;


    @Override
    public Invoice getByInvoiceId(final String invoiceId) {
        try {
            return Invoice.retrieve(invoiceId);

        } catch (StripeException e) {
            throw new GrabbillException("Invoice with id [" + invoiceId + "] not found on payment platform", e);
        }
    }

    @Override
    public Invoice createInvoice(
            final Account account,
            final AccountSubscription newAccountSubscription,
            final Integer planId,
            final SubscriptionMode subscriptionMode,
            final StoragePlanOption storageOption,
            final TransactionalEmailPlanOption transactionalEmailOption,
            final EmailCampaignPlanOption emailCampaignOption,
            final double offsetAmount,
            final PromoCode promoCode
    ) {
        Invoice invoice;
        try {
            InvoiceCreateParams.Builder invoiceCreateParamsBuilder = InvoiceCreateParams.builder()
                    .setCustomer(account.getStripeCustomerId())
                    .setAutoAdvance(true)
                    .setCollectionMethod(InvoiceCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY)
                    .setDescription(
                            DATE_TIME_FORMATTER.format(newAccountSubscription.getCycleStartDate()) + " - "
                                    + DATE_TIME_FORMATTER.format(newAccountSubscription.getCycleEndDate())
                    );

            // apply annual subscription discount
            if (SubscriptionMode.ANNUALLY.equals(newAccountSubscription.getMode())) {
                InvoiceCreateParams.Discount annualSubscriptionDiscount = new InvoiceCreateParams.Discount.Builder()
                        .setCoupon("UjJhNqPK").build();
                invoiceCreateParamsBuilder.addDiscount(annualSubscriptionDiscount);
            }

            // apply invoice offset discount
            if (offsetAmount > 0) {
                CouponCreateParams params = CouponCreateParams.builder()
                        .setCurrency("MYR")
                        .setAmountOff(BigDecimal.valueOf(offsetAmount * 100).longValue())
                        .setDuration(CouponCreateParams.Duration.ONCE)
                        .build();
                Coupon coupon = Coupon.create(params);
                InvoiceCreateParams.Discount invoiceDiscount = new InvoiceCreateParams.Discount.Builder()
                        .setCoupon(coupon.getId()).build();
                invoiceCreateParamsBuilder.addDiscount(invoiceDiscount);
            }

            // apply promo code discount
            if (promoCode != null && promoCode.isActive()) {
                InvoiceCreateParams.Discount promoDiscount = new InvoiceCreateParams.Discount.Builder()
                        .setCoupon(promoCode.getCode()).build();
                invoiceCreateParamsBuilder.addDiscount(promoDiscount);
            }

            invoice = Invoice.create(invoiceCreateParamsBuilder.build());

            Product storageProduct = productService.getProduct(
                    planId.toString(), PlanType.STORAGE, storageOption.getId().toString()).get();
            Product transactionalEmailProduct = productService.getProduct(
                    planId.toString(), PlanType.TRANSACTION_EMAIL, transactionalEmailOption.getId().toString()).get();
            Product emailCampaignProduct = productService.getProduct(
                    planId.toString(), PlanType.MARKETING_EMAIL, emailCampaignOption.getId().toString()).get();
            appendProductToInvoice(account, invoice, storageProduct, subscriptionMode, promoCode);
            appendProductToInvoice(account, invoice, transactionalEmailProduct, subscriptionMode, promoCode);
            appendProductToInvoice(account, invoice, emailCampaignProduct, subscriptionMode, promoCode);

        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to create invoice for customer [" + account.getStripeCustomerId() + "] from payment platform", e);
        }

        return invoice;
    }

    @Override
    public Invoice createSmsCreditsTopupInvoice(final Account account, final CreditsPlanOption creditsPlanOption) {
        if (!StringUtils.hasLength(creditsPlanOption.getStripeProductId())) {
            throw new GrabbillException("CreditsPlanOption does not have a corresponding stripe product id");
        }

        Invoice invoice;
        try {
            InvoiceCreateParams invoiceCreateParams = InvoiceCreateParams.builder()
                    .setCustomer(account.getStripeCustomerId())
                    .setAutoAdvance(true)
                    .setCollectionMethod(InvoiceCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY)
                    .setDescription("SMS Credits Topup").build();
            invoice = Invoice.create(invoiceCreateParams);

            Product product = productService.getProductByStripeId(creditsPlanOption.getStripeProductId())
                    .orElseThrow(() -> new GrabbillException(
                            "Product with id [" + creditsPlanOption.getStripeProductId() + "] not found!"));

            Map<String, Price> prices = productService.getPricesByProduct(product);
            Price price = prices.get("per_unit");
            if (price == null) {
                throw new GrabbillException("No per_unit price for the given product - [" + creditsPlanOption.getStripeProductId() + "]");
            }

            InvoiceItemCreateParams.Builder invoiceItemCreateParamsBuilder = InvoiceItemCreateParams.builder()
                    .setCustomer(account.getStripeCustomerId())
                    .setInvoice(invoice.getId())
                    .setDescription(product.getDescription())
                    .setCurrency(price.getCurrency())
                    .setPrice(price.getId())
                    .setQuantity(1L);

            InvoiceItem.create(invoiceItemCreateParamsBuilder.build());

        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to create invoice for customer [" + account.getStripeCustomerId() + "] from payment platform", e);
        }

        return invoice;
    }

    @Override
    public Invoice chargeInvoice(final String invoiceId) {
        Invoice invoice = getByInvoiceId(invoiceId);
        try {
            return invoice.finalizeInvoice().pay();
        } catch (StripeException e) {
            log.warn("Failed to charge invoice [" + invoiceId + "] from payment platform", e);
            return getByInvoiceId(invoiceId);
        }
    }

    @Override
    public Invoice retryChargeInvoice(final String invoiceId) {
        Invoice invoice = getByInvoiceId(invoiceId);
        try {
            return invoice.pay();
        } catch (StripeException e) {
            log.warn("Failed to retry charge invoice [" + invoiceId + "] from payment platform", e);
            return getByInvoiceId(invoiceId);
        }
    }

    @Override
    public PaymentIntent getPaymentIntentByInvoiceId(final String invoiceId) {
        try {
            Invoice targetInvoice = Invoice.retrieve(invoiceId);
            return PaymentIntent.retrieve(targetInvoice.getPaymentIntent());

        } catch (StripeException e) {
            throw new GrabbillException("Unable to retrieve payment intent for invoice with id [" + invoiceId + "].", e);
        }
    }

    @Override
    public PaymentMethod getPaymentMethodByInvoiceId(final String invoiceId) {
        try {
            Invoice targetInvoice = Invoice.retrieve(invoiceId);
            PaymentIntent targetPaymentIntent = PaymentIntent.retrieve(targetInvoice.getPaymentIntent());
            return PaymentMethod.retrieve(targetPaymentIntent.getPaymentMethod());

        } catch (StripeException e) {
            throw new GrabbillException("Unable to retrieve payment method for invoice with id [" + invoiceId + "].", e);
        }
    }

    private void appendProductToInvoice(
            final Account account,
            final Invoice invoice,
            final Product product,
            final SubscriptionMode subscriptionMode,
            final PromoCode promoCode
    ) throws StripeException {
        String billingCycle = SubscriptionMode.MONTHLY.equals(subscriptionMode) ? "monthly" : "yearly";

        Price price = productService.getPricesByProduct(product).get(billingCycle);
        InvoiceItemCreateParams.Builder invoiceItemCreateParamsBuilder = InvoiceItemCreateParams.builder()
                .setCustomer(account.getStripeCustomerId())
                .setInvoice(invoice.getId())
                .setDescription(product.getDescription())
                .setCurrency(price.getCurrency())
                .setPrice(price.getId())
                .setQuantity(1L);

//        if (promoCode != null && promoCode.isActive() && promoCode.getDiscountType().equals(DiscountType.PERCENTAGE)) {
//            InvoiceItemCreateParams.Discount annualDiscount = InvoiceItemCreateParams.Discount.builder().setCoupon(promoCode.getCode()).build();
//            invoiceItemCreateParamsBuilder.addDiscount(annualDiscount);
//        }

        InvoiceItem.create(invoiceItemCreateParamsBuilder.build());
    }

}
