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
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import voyage.common.error.ErrorUtils

/**
 * Overrides the default OAuth2Exception JSON serializer with the standard error object format for this app.
 * This is used by the Spring Security OAuth2 Authorization & Resource servlet filters.
 *
 * NOTE: Spring MVC Controllers & DefaultExceptionHandler processes occur after all of the authentication is complete,
 * which is why we need a special process to handle exceptions for the OAuth2 Authorization & Resource servers.
 */
class AppOAuth2ExceptionSerializer extends StdSerializer<AppOAuth2Exception> {
    AppOAuth2ExceptionSerializer() {
        super(AppOAuth2Exception)
    }

    @Override
    void serialize(AppOAuth2Exception ex, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.with {
            writeStartArray()
                writeStartObject()
                    writeStringField('error', ErrorUtils.getErrorCode(ex.httpStatus.value()))
                    writeStringField('errorDescription', "${ex.httpStatus.value()} ${ex.httpStatus.reasonPhrase}. ${ex.message}")
                writeEndObject()
            writeEndArray()
        }
    }
}
