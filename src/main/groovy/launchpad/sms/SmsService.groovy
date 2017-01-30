package launchpad.sms

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SmsService {

    @Autowired
    AmazonSNS amazonSNS

    boolean send(SmsMessage smsMessage) {
        //TODO: Handle the exception when sending message is failed - Jagadeesh Manne - 01/20/2017
        Map<String, MessageAttributeValue> smsAttributes =
                new HashMap<String, MessageAttributeValue>();
        smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
                .withStringValue("Voyage") //The sender ID shown on the device.
                .withDataType("String"));
        smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
                .withStringValue("Transactional")
                .withDataType("String"));
        PublishResult result = amazonSNS.publish(new PublishRequest()
                .withMessage(smsMessage.text)
                .withPhoneNumber(smsMessage.to)
                .withMessageAttributes(smsAttributes))
        if (result) {
            smsMessage.isSmsSent = true
        }
        return smsMessage.isSmsSent
    }
}
