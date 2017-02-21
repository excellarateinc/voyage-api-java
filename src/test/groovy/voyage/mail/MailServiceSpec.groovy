package voyage.mail

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import javax.mail.Message

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class MailServiceSpec extends Specification {

    @Autowired
    MailService mailService

    private GreenMail greenMailSMTP

    def setup() {
        ServerSetup setup = new ServerSetup(3025, 'localhost', ServerSetup.PROTOCOL_SMTP)
        greenMailSMTP = new GreenMail(setup)
        greenMailSMTP.start()
    }

    def cleanup() {
        greenMailSMTP.stop()
    }

    def 'test sendMail method' () {
        setup:
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
    }
}
