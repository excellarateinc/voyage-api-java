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
