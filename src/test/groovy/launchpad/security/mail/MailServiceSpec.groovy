package launchpad.security.mail

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import launchpad.mail.MailMessage
import launchpad.mail.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import spock.lang.Specification

import javax.mail.Message

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class MailServiceSpec extends Specification {

    @Autowired
    MailService mailService

    @Shared
    private GreenMail greenMailSMTP

    def setup() {
        ServerSetup setup = new ServerSetup(3025, "localhost", ServerSetup.PROTOCOL_SMTP);
        greenMailSMTP = new GreenMail(setup)
        greenMailSMTP.start()
    }

    def cleanup() {
        greenMailSMTP.stop()
    }

    def 'test sendMail method' () {
        setup:
            MailMessage mailMessage = new MailMessage()
            mailMessage.to = 'receiver@launchpad.com'
            mailMessage.from = 'sender@launchpad.com'
            mailMessage.subject = 'test subject'
            mailMessage.text = 'test message'
            mailMessage.cc = 'cc-receiver@launchpad.com'
            mailMessage.bcc = 'bcc-receiver@launchpad.com'
        when:
            mailService.send(mailMessage)
        then:
            Message[] messages = greenMailSMTP.getReceivedMessages()
            assert 1 == messages.length
            assert 'test subject' == messages[0].getSubject()
            assert GreenMailUtil.getBody(messages[0]).contains('test message');
    }
}
