package launchpad

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Class which forms the entry point into the application which uses the Spring-boot to kickstart the server.
 */
@SpringBootApplication
class App {
    static void main(String[] args) {
        SpringApplication.run(App, args)
    }
}
