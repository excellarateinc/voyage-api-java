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
package voyage.security.audit

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class JsonUtil {
    static String replaceAll(String jsonText, String[] keys, Object value) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Object jsonMap = jsonSlurper.parseText(jsonText)
        replaceAll(jsonMap, keys, value)
    }

    static String replaceAll(Object subValue, String[] keys, Object value) {
        if (subValue instanceof Map) {
            replaceAllMap((Map)subValue, keys, value)
        } else if (subValue instanceof Iterable) {
            subValue.each { collectionItem ->
                replaceAll(collectionItem, keys, value)
            }
        }
        return (new JsonBuilder(subValue)).toString()
    }

    private static String replaceAllMap(Map json, String[] keys, Object value) {
        json.findResults { Map.Entry mapEntry ->
            for (String matchKey in keys) {
                if (mapEntry.key == matchKey) {
                    mapEntry.value = value
                } else {
                    replaceAll((Object)mapEntry.value, keys, value)
                }
            }
            return mapEntry
        }
    }
}
