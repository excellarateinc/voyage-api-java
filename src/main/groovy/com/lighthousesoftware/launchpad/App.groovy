package com.lighthousesoftware.launchpad

import org.apache.catalina.connector.Connector
import org.grails.boot.internal.EnableAutoConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.boot.autoconfigure.domain.EntityScan

@SpringBootApplication
@ComponentScan
@EnableAutoConfiguration
public class App {
    @Value('${tomcat.ajp.port}') int ajpPort
    @Value('${tomcat.ajp.enabled}') boolean tomcatAjpEnabled

    public static void main(String[] args) {
        SpringApplication.run(App.class, args)
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory()
        if (tomcatAjpEnabled) {
            Connector ajpConnector = new Connector("AJP/1.3")
            ajpConnector.setProtocol("AJP/1.3")
            ajpConnector.setPort(ajpPort)
            ajpConnector.setSecure(false)
            ajpConnector.setAllowTrace(false)
            ajpConnector.setScheme("http")
            tomcat.addAdditionalTomcatConnectors(ajpConnector)
        }
        return tomcat;
    }
}
