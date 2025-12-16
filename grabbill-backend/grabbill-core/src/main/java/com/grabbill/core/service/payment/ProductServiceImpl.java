package com.grabbill.core.service.payment;

import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.plan.PlanType;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceSearchResult;
import com.stripe.model.Product;
import com.stripe.model.ProductSearchResult;
import com.stripe.param.PriceSearchParams;
import com.stripe.param.ProductSearchParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public class ProductServiceImpl implements ProductService {

    @Override
    public Optional<Product> getProduct(final String planId, final PlanType planType, final String optionId) {
        ProductSearchParams productSearchParams = ProductSearchParams.builder()
                .setQuery(
                        "active:'true' AND metadata['plan_id']:'" + planId + "'" +
                                " AND metadata['plan_type']:'" + planType.getName() + "'" +
                                " AND metadata['option_id']:'" + optionId + "'"
                )
                .setLimit(1L)
                .build();

        try {
            ProductSearchResult productSearchResult = Product.search(productSearchParams);
            if (!productSearchResult.getData().isEmpty()) {
                return Optional.of(productSearchResult.getData().get(0));
            }
        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to retrieve product [planId=" + planId + ", optionId=" + optionId + "] from payment platform", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> getSmsCreditProduct() {
        ProductSearchParams productSearchParams = ProductSearchParams.builder()
                .setQuery("active:'true' AND metadata['type']:'sms'")
                .setLimit(1L)
                .build();

        try {
            ProductSearchResult productSearchResult = Product.search(productSearchParams);
            if (!productSearchResult.getData().isEmpty()) {
                return Optional.of(productSearchResult.getData().get(0));
            }
        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to retrieve sms product from payment platform", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> getProductByStripeId(final String stripeId) {
        try {
            return Optional.ofNullable(Product.retrieve(stripeId));
        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to retrieve product by id from payment platform", e);
        }
    }

    @Override
    public List<Product> getByPlanType(final PlanType planType) {
        ProductSearchParams productSearchParams = ProductSearchParams.builder()
                .setQuery("active:'true' AND metadata['plan_type']:'" + planType.getName() + "'")
                .setLimit(50L)
                .build();

        try {
            return Product.search(productSearchParams).getData();
        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to retrieve products [planType=" + planType.getName() + "] from payment platform", e);
        }
    }

    @Override
    public Map<String, Price> getPricesByProduct(final Product product) {
        PriceSearchParams priceSearchParams = PriceSearchParams.builder()
                .setQuery("active:'true' AND product:'" + product.getId() + "'")
                .build();

        try {
            PriceSearchResult priceSearchResult = Price.search(priceSearchParams);
            Map<String, Price> result = new HashMap<>();
            for (Price price : priceSearchResult.getData()) {
                result.put(price.getMetadata().get("billing_cycle"), price);
            }
            return result;

        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to retrieve prices for product [" + product.getId() + "] from payment platform", e);
        }
    }

}
