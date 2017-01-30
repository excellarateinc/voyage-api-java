package launchpad.security.sms

import launchpad.sms.SmsMessage
import launchpad.sms.SmsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class SmsServiceSpec extends Specification {

    @Autowired
    SmsService smsService

    def 'test send method' () {
        setup:
            SmsMessage smsMessage = new SmsMessage()
            smsMessage.to = '918977099970'
            smsMessage.text = 'test message'
        when:
            smsService.send(smsMessage)
        then:
            noExceptionThrown()
    }
}
