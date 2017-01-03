package launchpad.security.mail

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetupTest
import launchpad.mail.MailMessage
import launchpad.mail.MailService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification

import javax.mail.Message

class MailServiceSpec extends Specification {

    @Autowired
    MailService mailService

    @Shared
    private GreenMail greenMailSMTP

    def setup() {
        greenMailSMTP = new GreenMail(ServerSetupTest.SMTP)
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
        when:
            mailService.send(mailMessage)
        then:
            Message[] messages = greenMailSMTP.getReceivedMessages()
            assert 1 == messages.length
            assert 'test subject' == messages[0].getSubject()
    }
}
