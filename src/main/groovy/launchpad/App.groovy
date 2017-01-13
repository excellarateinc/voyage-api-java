package launchpad

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer

@SpringBootApplication
class App extends SpringBootServletInitializer {

    static void main(String[] args) {
        SpringApplication.run(App, args)
    }

    /**
     * Initialization for Java application container like Apache Tomcat when deploying as a .war file
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(App)
    }
}
