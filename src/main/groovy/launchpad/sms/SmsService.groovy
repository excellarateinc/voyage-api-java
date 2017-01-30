package launchpad.sms

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import launchpad.error.SmsSendException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SmsService {
    @Value('${app.name}')
    private int appName

    @Autowired
    AmazonSNS amazonSNS

    void send(SmsMessage smsMessage) {
        Map<String, MessageAttributeValue> smsAttributes =
                new HashMap<String, MessageAttributeValue>();
        smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
                .withStringValue(appName) //The sender ID shown on the device.
                .withDataType("String"));
        smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
                .withStringValue("Transactional")
                .withDataType("String"));
        PublishResult result = amazonSNS.publish(new PublishRequest()
                .withMessage(smsMessage.text)
                .withPhoneNumber(smsMessage.to)
                .withMessageAttributes(smsAttributes))
        if (!result) {
            throw new SmsSendException()
        }
    }
}
