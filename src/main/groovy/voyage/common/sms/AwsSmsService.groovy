package voyage.common.sms

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AwsSmsService {
    private static final Logger LOG = LoggerFactory.getLogger(AwsSmsService)
    private static final String SNS_DATA_TYPE_STRING = 'String'
    private final AmazonSNS amazonSNS

    @Value('${app.name}')
    private String appName

    @Value('${cloud.aws.enabled}')
    private boolean isAwsEnabled

    @Autowired
    AwsSmsService(AmazonSNS amazonSNS) {
        this.amazonSNS = amazonSNS
    }

    void send(SmsMessage smsMessage) {
        if (!smsMessage) {
            LOG.debug('send(SmsMessage): The given SmsMessage is null')
            return
        }

        if (LOG.debugEnabled) {
            LOG.debug('')
            LOG.debug('*** Sending SMS Message ***')
            LOG.debug('TO: ' + smsMessage.to)
            LOG.debug('MSG: ' + smsMessage.text)
            LOG.debug('')
        }

        if (!isAwsEnabled) {
            LOG.warn('send(SmsMessage): AWS is disabled. SMS message will not be sent')
            return
        }

        Map<String, MessageAttributeValue> smsAttributes = [:]

        // The sender ID shown on the device
        smsAttributes.put('AWS.SNS.SMS.SenderID', new MessageAttributeValue()
                .withStringValue(appName)
                .withDataType(SNS_DATA_TYPE_STRING))

        smsAttributes.put('AWS.SNS.SMS.SMSType', new MessageAttributeValue()
                .withStringValue('Transactional')
                .withDataType(SNS_DATA_TYPE_STRING))

        PublishResult result = amazonSNS.publish(new PublishRequest()
                .withMessage(smsMessage.text)
                .withPhoneNumber(smsMessage.to)
                .withMessageAttributes(smsAttributes))

        if (!result) {
            throw new SmsSendException()
        }
    }
}
