package voyage.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@Configuration
class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Override
    void addViewControllers(ViewControllerRegistry registry) {
        // Override the Spring MVC generic pages
        registry.addViewController('/').setViewName('index')
        registry.addViewController('/login').setViewName('login')
    }
}
