package com.lighthousesoftware.launchpad.common.jsondeserializer

import com.lighthousesoftware.launchpad.domain.User
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer
import groovy.json.JsonOutput;

class UserDeserializer extends JsonDeserializer<User>{
    @Override
    public User deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        //TODO : Generate custom json response from the object

    }
}
