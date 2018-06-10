package voyage.config

import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ResponseHeader

import static com.google.common.base.Predicates.or
import static com.google.common.collect.Lists.newArrayList

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.builders.ResponseMessageBuilder
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class Swagger2Config {

    public static final String ERROR_RESPONSE = 'ErrorResponse'
    public static final String VOYAGE = 'voyage'

    @Bean
    Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(MetaClass)//needed so that groovy resource files have property 'metadata' ignored
                .groupName(VOYAGE)
                .select()

                .apis(RequestHandlerSelectors.basePackage(VOYAGE))
                .paths(or(PathSelectors.regex('/api.*'), PathSelectors.regex('/error.*')))

                .build()
                .apiInfo(apiInfo())
//                .securitySchemes(newArrayList(securityScheme()))
//                .securityContexts(newArrayList(sec))
                .globalResponseMessage(RequestMethod.GET,
                newArrayList(
                        new ResponseMessageBuilder()
                                .code(500)
                                .message('500 Internal Server Error')
                                .responseModel(new ModelRef(ERROR_RESPONSE))
                                .build()))
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title('Voyage.Api')
                .description('A foundational set of web services that implement industry standard guidelines, common ' +
                'best practices, and the experienced insights afforded to Lighthouse Software thru decades of ' +
                'enterprise business software development.')
                .version('1.0.0')
                .license('Apache License Version 2.0')
                .licenseUrl('https://www.apache.org/licenses/LICENSE-2.0')
                .contact(new Contact('Lighthouse Software', 'http://LighthouseSoftware.com', 'contact@LighthouseSoftware.com'))
                .build()
    }

//    @Bean
//    public SecurityConfiguration security() {
//        return SecurityConfigurationBuilder.builder()
//                .clientId(CLIENT_ID)
//                .clientSecret(CLIENT_SECRET)
//                .scopeSeparator(' ')
//                .useBasicAuthenticationWithAccessCodeGrant(true)
//                .build();
//    }
//    private SecurityScheme securityScheme() {
//        GrantType grantType = new AuthorizationCodeGrantBuilder()
//                .tokenEndpoint(new TokenEndpoint(AUTH_SERVER + '/token', 'oauthtoken'))
//                .tokenRequestEndpoint(
//                new TokenRequestEndpoint(AUTH_SERVER + '/authorize', CLIENT_ID, CLIENT_ID))
//                .build();
//
//        SecurityScheme oauth = new OAuthBuilder().name('spring_oauth')
//                .grantTypes(Arrays.asList(grantType))
//                .scopes(Arrays.asList(scopes()))
//                .build();
//        return oauth;
//    }
//    private SecurityContext securityContext() {
//        return SecurityContext.builder()
//                .securityReferences(Arrays.asList(new SecurityReference('spring_oauth', scopes())))
//                .forPaths(PathSelectors.regex('/api.*'))
//                .build();
//    }
//    private AuthorizationScope[] scopes() {
//        AuthorizationScopeBuilder.
//        AuthorizationScope[] scopes = {
//            new AuthorizationScope('read', 'for read operations'),
//            new AuthorizationScope('write', 'for write operations'),
//            new AuthorizationScope('foo', 'Access foo API') };
//        return scopes;
//    }
}
