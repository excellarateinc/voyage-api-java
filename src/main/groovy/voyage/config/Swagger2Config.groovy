package voyage.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.AuthorizationCodeGrantBuilder
import springfox.documentation.builders.AuthorizationScopeBuilder
import springfox.documentation.builders.OAuthBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.builders.ResponseMessageBuilder
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.service.GrantType
import springfox.documentation.service.SecurityReference
import springfox.documentation.service.SecurityScheme
import springfox.documentation.service.TokenEndpoint
import springfox.documentation.service.TokenRequestEndpoint
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.swagger2.annotations.EnableSwagger2

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.base.Predicates.*;
@Configuration
@EnableSwagger2
class Swagger2Config {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(groovy.lang.MetaClass.class)//needed so that groovy resource files have property 'metadata' ignored
                .groupName('voyage')
                .select()

                .apis(RequestHandlerSelectors.basePackage('voyage'))
                .paths(or(PathSelectors.regex('/api.*'), PathSelectors.regex('/error.*')))

                .build()
                .apiInfo(apiInfo())
//                .securitySchemes(newArrayList(securityScheme()))
//                .securityContexts(newArrayList(sec))
                .globalResponseMessage(RequestMethod.GET,
                    newArrayList(
                        new ResponseMessageBuilder()
                                .code(400)
                                .message("400 Bad Request")
                                .responseModel(new ModelRef("ErrorResponse"))
                                .build(),
                        new ResponseMessageBuilder()
                                .code(401)
                                .message("401 Unauthorized")
                                .responseModel(new ModelRef("ErrorResponse"))
                                .build(),
                        new ResponseMessageBuilder()
                                .code(403)
                                .message("403 Forbidden")
                                .responseModel(new ModelRef("ErrorResponse"))
                                .build(),
                        new ResponseMessageBuilder()
                                .code(404)
                                .message("404 Not Found")
                                .responseModel(new ModelRef("ErrorResponse"))
                                .build(),
                        new ResponseMessageBuilder()
                                .code(500)
                                .message("500 Internal Server Error")
                                .responseModel(new ModelRef("ErrorResponse"))
                                .build()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Voyage.Api")
                .description('"A foundational set of web services that implement industry standard guidelines, common best practices, and the experienced insights afforded to Lighthouse Software thru decades of enterprise business software development."')
                .version("1.0.0")
                .license("Apache License Version 2.0")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0\"")
                .contact(new Contact("Tim Michalski", "https://lighthousesoftware.com/contact.html", "contact@lighthousesoftware.com"))
                .build();
    }

//    @Bean
//    public SecurityConfiguration security() {
//        return SecurityConfigurationBuilder.builder()
//                .clientId(CLIENT_ID)
//                .clientSecret(CLIENT_SECRET)
//                .scopeSeparator(" ")
//                .useBasicAuthenticationWithAccessCodeGrant(true)
//                .build();
//    }
//    private SecurityScheme securityScheme() {
//        GrantType grantType = new AuthorizationCodeGrantBuilder()
//                .tokenEndpoint(new TokenEndpoint(AUTH_SERVER + "/token", "oauthtoken"))
//                .tokenRequestEndpoint(
//                new TokenRequestEndpoint(AUTH_SERVER + "/authorize", CLIENT_ID, CLIENT_ID))
//                .build();
//
//        SecurityScheme oauth = new OAuthBuilder().name("spring_oauth")
//                .grantTypes(Arrays.asList(grantType))
//                .scopes(Arrays.asList(scopes()))
//                .build();
//        return oauth;
//    }
//    private SecurityContext securityContext() {
//        return SecurityContext.builder()
//                .securityReferences(Arrays.asList(new SecurityReference("spring_oauth", scopes())))
//                .forPaths(PathSelectors.regex("/api.*"))
//                .build();
//    }
//    private AuthorizationScope[] scopes() {
//        AuthorizationScopeBuilder.
//        AuthorizationScope[] scopes = {
//            new AuthorizationScope("read", "for read operations"),
//            new AuthorizationScope("write", "for write operations"),
//            new AuthorizationScope("foo", "Access foo API") };
//        return scopes;
//    }
}
