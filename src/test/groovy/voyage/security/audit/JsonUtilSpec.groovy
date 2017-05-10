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
