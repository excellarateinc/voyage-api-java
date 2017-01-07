package launchpad

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@SpringBootApplication
class App extends WebMvcConfigurerAdapter {

    static void main(String[] args) {
        SpringApplication.run(App, args)
    }

    @Override
    void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController('/login').setViewName('login')
    }
}
