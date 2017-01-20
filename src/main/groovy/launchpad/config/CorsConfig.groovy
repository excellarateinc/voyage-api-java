package launchpad.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {
    private static Logger LOG = LoggerFactory.getLogger(CorsConfig)
    private static final String ANY = '*'
    private static final String ALL_PATHS = '/**'

    @Value('${security.cors.allowed-origin}')
    private String allowedOrigin

    @Bean
    FilterRegistrationBean corsFilter() {
        if (LOG.debugEnabled) LOG.debug('CorsConfig: Initializing CORS Servlet Filter')

        CorsConfiguration config = new CorsConfiguration()
        config.setAllowCredentials(true)
        config.addAllowedOrigin(allowedOrigin)
        config.addAllowedHeader(ANY)
        config.addAllowedMethod(ANY)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration(ALL_PATHS, config)

        // Create a new filter and make sure this filter executes before Spring Security
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source))
        bean.setOrder(0)

        return bean
    }
}
