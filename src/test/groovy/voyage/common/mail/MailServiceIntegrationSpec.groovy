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
package voyage.common.mail

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import org.springframework.beans.factory.annotation.Autowired
import voyage.test.AbstractIntegrationTest

import javax.mail.Message

class MailServiceIntegrationSpec extends AbstractIntegrationTest {

    @Autowired
    MailService mailService

    private GreenMail greenMailSMTP

    def 'test sendMail method' () {
        setup:
            ServerSetup setup = new ServerSetup(3025, 'localhost', ServerSetup.PROTOCOL_SMTP)
            greenMailSMTP = new GreenMail(setup)
            greenMailSMTP.start()
            MailMessage mailMessage = new MailMessage()
            mailMessage.to = 'testmsg@lssinc.com'
            mailMessage.from = 'testmsg@lssinc.com'
            mailMessage.subject = 'test subject'
            mailMessage.text = 'test message'
        when:
            mailService.send(mailMessage)
        then:
            Message[] messages = greenMailSMTP.receivedMessages
            assert 1 == messages.length
            assert 'test subject' == messages[0].subject
            assert GreenMailUtil.getBody(messages[0]).contains('test message')
        cleanup:
            greenMailSMTP.stop()
    }

    def 'test sendMail exception with out smtp server' () {
        setup:
            MailMessage mailMessage = new MailMessage()
            mailMessage.to = 'testmsg@lssinc.com'
            mailMessage.from = 'testmsg@lssinc.com'
            mailMessage.subject = 'test subject'
            mailMessage.text = 'test message'
        when:
            mailService.send(mailMessage)
        then:
            thrown(MailSendException)
    }

    def 'test sendMail with out template and with out text' () {
        setup:
            MailMessage mailMessage = new MailMessage()
            mailMessage.to = 'testmsg@lssinc.com'
            mailMessage.from = 'testmsg@lssinc.com'
            mailMessage.subject = 'test subject'
        when:
          mailService.send(mailMessage)
        then:
            thrown(MailSendException)
    }

    def 'test sendMail io exception' () {
        setup:
            MailMessage mailMessage = new MailMessage()
            mailMessage.to = 'testmsg@lssinc.com'
            mailMessage.from = 'testmsg@lssinc.com'
            mailMessage.subject = 'test subject'
            mailMessage.template = 'test'
        when:
            mailService.send(mailMessage)
        then:
            thrown(MailSendException)
    }

    def 'test sendMail template exception' () {
        setup:
            MailMessage mailMessage = new MailMessage()
            mailMessage.to = 'testmsg@lssinc.com'
            mailMessage.from = 'testmsg@lssinc.com'
            mailMessage.subject = 'test subject'
            mailMessage.template = 'welcome.ftl'
        when:
            mailService.send(mailMessage)
        then:
            thrown(MailSendException)
    }
}
