package launchpad.common.jsondeserializer

import launchpad.domain.User
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer

class UserDeserializer extends JsonDeserializer<User>{
    @Override
    public User deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        //TODO : Generate custom json response from the object

    }
}
