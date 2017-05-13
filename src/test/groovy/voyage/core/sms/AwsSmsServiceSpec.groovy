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
package voyage.core.sms

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
