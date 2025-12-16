package com.grabbill.engine;

import com.grabbill.core.CoreConfiguration;
import com.grabbill.core.conf.DeploymentProperties;
import com.grabbill.engine.configuration.AuditConfiguration;
import com.grabbill.engine.configuration.EngineConfiguration;
import com.grabbill.engine.configuration.EngineJobQueueConfiguration;
import com.grabbill.engine.configuration.SubscriptionManagementConfiguration;
import com.stripe.Stripe;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.integration.config.EnableIntegration;

import javax.annotation.PostConstruct;

/**
 * @author michaellow
 */
@Import({
		AuditConfiguration.class,
		CoreConfiguration.class,
		EngineConfiguration.class,
		EngineJobQueueConfiguration.class,
		SubscriptionManagementConfiguration.class
})
@EnableConfigurationProperties({
		DeploymentProperties.class
})
@EnableIntegration
@EnableRabbit
@SpringBootApplication
public class GrabbillEngineApplication {

	@Value("${payment.stripe.mode}")
	private String mode;

	@Value("${payment.stripe.api.key.test}")
	private String stripeTestApiKey;

	@Value("${payment.stripe.api.key.live}")
	private String stripeLiveApiKey;


	@PostConstruct
	public void init() {
		Stripe.apiKey = "live".equals(mode) ? stripeLiveApiKey: stripeTestApiKey;
	}

	public static void main(String[] args) {
		SpringApplication.run(GrabbillEngineApplication.class, args);
	}

}
