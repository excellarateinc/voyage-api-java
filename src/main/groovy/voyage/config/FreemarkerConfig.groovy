package voyage.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer

/**
 * Overrides the default spring-boot configuration to allow adding shared variables to the freemarker context
 */
@Configuration
class FreemarkerConfig extends FreeMarkerAutoConfiguration.FreeMarkerWebConfiguration  {

    @Value('${app.name}')
    private String appName

    @Value('${app.contact-support.email}')
    private String appSupportEmail

    @Value('${app.contact-support.phone}')
    private String supportPhone

    @Value('${app.contact-support.website}')
    private String website

    @Override
    FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = super.freeMarkerConfigurer()
        Map sharedVariables = [:]
        sharedVariables.put('appName', appName)
        sharedVariables.put('appSupportEmail', appSupportEmail)
        sharedVariables.put('supportPhone', supportPhone)
        sharedVariables.put('website', website)
        configurer.setFreemarkerVariables(sharedVariables)

        return configurer
    }
}
