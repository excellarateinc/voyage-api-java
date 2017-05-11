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
package voyage.security.error

import com.fasterxml.jackson.core.JsonGenerator
import org.springframework.http.HttpStatus
import spock.lang.Specification

class AppOAuth2ExceptionSerializerSpec extends Specification {

    def 'serialize exception as JSON'() {
        given:
            AppOAuth2Exception exception = new AppOAuth2Exception(HttpStatus.BAD_REQUEST, 'test message')
            AppOAuth2ExceptionSerializer serializer = new AppOAuth2ExceptionSerializer()
            JsonGenerator jsonGenerator = Mock(JsonGenerator)

        when:
            serializer.serialize(exception, jsonGenerator, null)

        then:
            1 * jsonGenerator.writeStartArray()
            1 * jsonGenerator.writeStartObject()
            1 * jsonGenerator.writeStringField('error', '400_bad_request')
            1 * jsonGenerator.writeStringField('errorDescription', '400 Bad Request. test message')
            1 * jsonGenerator.writeEndArray()
            1 * jsonGenerator.writeEndObject()
    }
}
