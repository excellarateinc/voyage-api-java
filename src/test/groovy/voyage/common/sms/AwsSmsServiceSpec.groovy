package voyage.common.sms

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishResult
import spock.lang.Specification

class AwsSmsServiceSpec extends Specification {

    def 'test send AWS disabled' () {
        given:
            def amazonSNS = Mock(AmazonSNS)
            AwsSmsService awsSmsService = new AwsSmsService(amazonSNS)
            awsSmsService.appName = 'test app'

            SmsMessage smsMessage = new SmsMessage()
            smsMessage.to = '9318977099970'
            smsMessage.text = 'test message'

        when:
            awsSmsService.send(smsMessage)

        then:
            0 * amazonSNS.publish(_)
            noExceptionThrown()
    }

    def 'test send AWS SNS message' () {
        given:
            def amazonSNS = Mock(AmazonSNS)
            AwsSmsService awsSmsService = new AwsSmsService(amazonSNS)
            awsSmsService.isAwsEnabled = true
            awsSmsService.appName = 'test app'

            SmsMessage smsMessage = new SmsMessage()
            smsMessage.to = '9318977099970'
            smsMessage.text = 'test message'

        when:
            awsSmsService.send(smsMessage)

        then:
            amazonSNS.publish(_) >> { pubReq ->
                assert pubReq.phoneNumber[0] == smsMessage.to
                assert pubReq.message[0] == smsMessage.text
                assert pubReq.messageAttributes[0].'AWS.SNS.SMS.SenderID'.stringValue == 'test app'
                assert pubReq.messageAttributes[0].'AWS.SNS.SMS.SMSType'.stringValue == 'Transactional'
                return new PublishResult()
            }

            noExceptionThrown()
    }

    def 'test send AWS SNS message throw SmsSendException' () {
        given:
            def amazonSNS = Mock(AmazonSNS)
            AwsSmsService awsSmsService = new AwsSmsService(amazonSNS)
            awsSmsService.isAwsEnabled = true
            awsSmsService.appName = 'test app'

            SmsMessage smsMessage = new SmsMessage()
            smsMessage.to = '9318977099970'
            smsMessage.text = 'test message'

        when:
            awsSmsService.send(smsMessage)

        then:
            amazonSNS.publish(_) >> null
            thrown SmsSendException
    }
}
