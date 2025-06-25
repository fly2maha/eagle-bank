package org.eagle.bank;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class JsonReader {

    public <T> T jsonToObject(String filename, Class<T> actualClass) {
        InputStream in = this.getClass().getResourceAsStream(filename);
        if(in != null){
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                return objectMapper.readValue(in, actualClass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
