package com.cg;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.cg.repository.UserRepository;
import com.fasterxml.classmate.TypeResolver;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@ComponentScan(basePackages = { "com.cg","com.cg.jobs" }, basePackageClasses = {
		SpringSecurityConfig.class, UserRepository.class })
public class CgApplication {

	private final static String PUBLISH_KEY = "pub-c-28da4633-86e1-4f08-8b51-3cacb80367b6";
	private final static String SUBSCRIBE_KEY = "sub-c-adcae88e-78cd-11e6-a627-0619f8945a4f";
	private final static String SECRET_KEY = "sec-c-ODNlYTkzZTYtYzAyNi00NDcwLTliMDItY2Q3NmM4NWYyZmUz";

	public static void main(final String[] args) {
		// pnConfiguration.

		SpringApplication.run(CgApplication.class, args);
	}
	
	@Bean
	  public FilterRegistrationBean tokenFilter() {
	      final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
	      registrationBean.setFilter(new CorsFilter());
	      registrationBean.addUrlPatterns("/api/*");

	      return registrationBean;
	  }

	@Bean
	public Docket petApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(
						new ApiInfo(
								"CG Rest api",
								"The CG API will be described by the HTTP uniform interface, contextual links to resources, supported media types for representations, and schema of JSON representations. The cg API has been designed and developed to easily integrate into your existing panel management solution(s) with nominal effort.",
								"1.1", "url", "mandeep.sigh@redblink.com",
								"licence", "test url"))
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.cg"))
				.paths(PathSelectors.any())
				.build()
				.pathMapping("/")
				.directModelSubstitute(LocalDate.class, String.class)
				.genericModelSubstitutes(ResponseEntity.class)
				.alternateTypeRules(
						newRule(typeResolver.resolve(DeferredResult.class,
								typeResolver.resolve(ResponseEntity.class,
										WildcardType.class)), typeResolver
								.resolve(WildcardType.class)))
				.useDefaultResponseMessages(false)
				.globalResponseMessage(
						RequestMethod.GET,
						newArrayList(new ResponseMessageBuilder().code(500)
								.message("500 message")
								.responseModel(new ModelRef("Error")).build()))
				// .securitySchemes(newArrayList(apiKey()))
				.securityContexts(newArrayList(securityContext()));
	}

	@Autowired
	private TypeResolver typeResolver;

	private ApiKey apiKey() {
		return new ApiKey("mykey", "api_key", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth())
				.forPaths(PathSelectors.regex("/anyPath.*")).build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope(
				"global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return newArrayList(new SecurityReference("mykey", authorizationScopes));
	}

	@Bean
	SecurityConfiguration security() {
		return new SecurityConfiguration("test-app-client-id",
				"test-app-realm", "test-app", "apiKey");
	}

	@Bean
	UiConfiguration uiConfig() {
		return new UiConfiguration("validatorUrl");
	}

	@Bean
	PubNub pubNub() {
		System.out.println("=============Pub NUb Object ==============");
		PNConfiguration pnConfiguration = new PNConfiguration();
		pnConfiguration.setPublishKey(PUBLISH_KEY);
		pnConfiguration.setSubscribeKey(SUBSCRIBE_KEY);
		// pnConfiguration.setSecretKey(SECRET_KEY);
		PubNub pubnub = new PubNub(pnConfiguration);
		return pubnub;
	}
}