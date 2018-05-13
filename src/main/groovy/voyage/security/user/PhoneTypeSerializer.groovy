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
package voyage.security.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import voyage.core.error.ErrorUtils
import voyage.security.error.AppOAuth2Exception

/**
 * Jackson json serializer for PhoneType to return <code>code</code> property.
 */
class PhoneTypeSerializer extends StdSerializer<PhoneType> {
    PhoneTypeSerializer() {
        super(AppOAuth2Exception)
    }

    @Override
    void serialize(PhoneType type, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.with {
            writeString(type == null ? null : type.getCode())
        }
    }
}
