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

import spock.lang.Specification

class JsonUtilSpec extends Specification {
    def 'replaceAll() finds no matches and returns the given JSON'() {
        given:
            String jsonText = '{"test1":"value1","test2":"value2","test3":"value3"}'
            String[] keyMatches = ['test']
        when:
            String jsonResponse = JsonUtil.replaceAll(jsonText, keyMatches, '**')
        then:
            jsonText == jsonResponse
    }

    def 'replaceAll() finds a single match and replaces the value'() {
        given:
           String jsonText = '{"test1":"value1","test2":"value2","test3":"value3"}'
            String[] keyMatches = ['test1']
        when:
            String jsonResponse = JsonUtil.replaceAll(jsonText, keyMatches, '**')
        then:
            '{"test1":"**","test2":"value2","test3":"value3"}' == jsonResponse
    }

    def 'replaceAll() finds multiple matches and replaces the values'() {
        given:
            String jsonText = '{"test1":"value1","test2":"value2","test3":"value3"}'
            String[] keyMatches = ['test1', 'test2', 'test3']
        when:
            String jsonResponse = JsonUtil.replaceAll(jsonText, keyMatches, '**')
        then:
            '{"test1":"**","test2":"**","test3":"**"}' == jsonResponse
    }

    def 'replaceAll() finds a nested object match and replaces the value'() {
        given:
            String jsonText = '{"test1":{"sub-test1":"sub-value1"},"test2":"value2","test3":"value3"}'
            String[] keyMatches = ['sub-test1']
        when:
           String jsonResponse = JsonUtil.replaceAll(jsonText, keyMatches, '**')
        then:
        '{"test1":{"sub-test1":"**"},"test2":"value2","test3":"value3"}' == jsonResponse
    }

    def 'replaceAll() finds multiple nested object matches and replaces the values'() {
        given:
            String jsonText = '{"test1":{"sub1":"sub-value1"},"test2":{"sub2":{"sub-sub2":"value2"}},' +
                    '"test3":[{"sub3":{"sub-sub3":"value3"}},{"sub4":{"sub-sub4":"value4"}}]}'
            String[] keyMatches = ['sub1', 'sub-sub2', 'sub-sub3', 'sub-sub4']
        when:
           String jsonResponse = JsonUtil.replaceAll(jsonText, keyMatches, '**')
        then:
            '{"test1":{"sub1":"**"},"test2":{"sub2":{"sub-sub2":"**"}},' +
                    '"test3":[{"sub3":{"sub-sub3":"**"}},{"sub4":{"sub-sub4":"**"}}]}' == jsonResponse
    }

    def 'replaceAll() finds object match in a multi-level array of arrays'() {
        given:
            String jsonText = '[[[{"test1":"value1"}]]]'
            String[] keyMatches = ['test1']
        when:
            String jsonResponse = JsonUtil.replaceAll(jsonText, keyMatches, '**')
        then:
            '[[[{"test1":"**"}]]]' == jsonResponse
    }
}
