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
package voyage.core.mail

import freemarker.template.Configuration
import freemarker.template.TemplateException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils

import javax.mail.internet.MimeMessage

@Service
class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(MailService)

    @Value('${app.contact-support.email}')
    private String from

    @Value('${spring.mail.overrideAddress}')
    private String overrideAddress

    private final JavaMailSender javaMailSender
    private final Configuration freeMarkerConfig

    @Autowired
    MailService(JavaMailSender javaMailSender, Configuration freeMarkerConfig) {
        this.javaMailSender = javaMailSender
        this.freeMarkerConfig = freeMarkerConfig
    }

    void send(MailMessage mailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage()
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true)
        mailMessage.from = mailMessage.from ?: from
        mimeMessageHelper.setFrom(mailMessage.from)
        if (overrideAddress) {
            mailMessage.to = overrideAddress
        }
        mimeMessageHelper.setTo(mailMessage.to)
        mimeMessageHelper.setSubject(mailMessage.subject)

        if (mailMessage.template) {
            String text = getContentFromTemplate(mailMessage.model, mailMessage.template)
            mimeMessageHelper.setText(text, true)
        } else if (mailMessage.text) {
            mimeMessageHelper.setText(mailMessage.text, true)
        }

        try {
            if (LOG.debugEnabled) {
                LOG.debug('Sending e-mail to ' + mailMessage.to)
            }
            javaMailSender.send(mimeMessageHelper.mimeMessage)
        } catch (MailException ignore) {
            throw new MailSendException()
        }
    }

    private String getContentFromTemplate(Map<String, Object> model, String template) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfig.getTemplate(template), model)
        } catch (IOException e) {
            LOG.error('Template {} was not found or could not be read, exception : {}', template, e.message)
            throw new MailSendException()
        } catch (TemplateException e) {
            LOG.error('Template {} rendering failed, exception : {}', template, e.message)
            throw new MailSendException()
        }
    }
}
