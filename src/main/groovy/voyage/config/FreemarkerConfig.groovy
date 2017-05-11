/*
 * Copyright 2017 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
