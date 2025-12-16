package com.grabbill.core.service.payment;

import com.grabbill.core.model.plan.PlanType;
import com.stripe.model.Price;
import com.stripe.model.Product;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface ProductService {

    Optional<Product> getProduct(String planId, PlanType planType, String optionId);

    Optional<Product> getSmsCreditProduct();

    Optional<Product> getProductByStripeId(String stripeId);

    List<Product> getByPlanType(PlanType planType);

    Map<String, Price> getPricesByProduct(Product product);

}
