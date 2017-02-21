package voyage.sms

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import voyage.error.SmsSendException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SmsService {
    @Value('${app.name}')
    private String appName

    @Autowired
    AmazonSNS amazonSNS

    void send(SmsMessage smsMessage) {
        String dataType = 'String'
        Map<String, MessageAttributeValue> smsAttributes = [:]
        smsAttributes.put('AWS.SNS.SMS.SenderID', new MessageAttributeValue()
                .withStringValue(appName) //The sender ID shown on the device.
                .withDataType(dataType))
        smsAttributes.put('AWS.SNS.SMS.SMSType', new MessageAttributeValue()
                .withStringValue('Transactional')
                .withDataType(dataType))
        PublishResult result = amazonSNS.publish(new PublishRequest()
                .withMessage(smsMessage.text)
                .withPhoneNumber(smsMessage.to)
                .withMessageAttributes(smsAttributes))
        if (!result) {
            throw new SmsSendException()
        }
    }
}
