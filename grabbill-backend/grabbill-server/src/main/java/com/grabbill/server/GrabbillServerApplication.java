package com.grabbill.server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.grabbill.core.CoreConfiguration;
import com.grabbill.core.conf.DeploymentProperties;
import com.grabbill.server.configuration.*;
import com.grabbill.server.security.SecurityConfiguration;
import com.grabbill.server.security.WebMvcConfiguration;
import com.stripe.Stripe;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author michaellow
 */
@Import({
		AuditConfiguration.class,
		CoreConfiguration.class,
		JobConfiguration.class,
		JobSchedulerConfiguration.class,
		SecurityConfiguration.class,
		ServerConfiguration.class,
		WebMvcConfiguration.class,
		WhatsappEventConfiguration.class
})
@EnableConfigurationProperties({
		DeploymentProperties.class
})
@EnableRabbit
@SpringBootApplication
public class GrabbillServerApplication {

	@Value("${firebase.service-account.filename}")
	private String firebaseServiceAccountFileName;

	@Value("${payment.stripe.mode}")
	private String mode;

	@Value("${payment.stripe.api.key.test}")
	private String stripeTestApiKey;

	@Value("${payment.stripe.api.key.live}")
	private String stripeLiveApiKey;


	@PostConstruct
	public void init() throws IOException {
		FirebaseApp.initializeApp(
				FirebaseOptions.builder().setCredentials(
						GoogleCredentials.fromStream(
								new ClassPathResource(
										firebaseServiceAccountFileName,
										GrabbillServerApplication.class.getClassLoader()
								).getInputStream()
						)
				).build()
		);

		Stripe.apiKey = "live".equals(mode) ? stripeLiveApiKey: stripeTestApiKey;
	}

	public static void main(String[] args) {
		SpringApplication.run(GrabbillServerApplication.class, args);
	}

}
