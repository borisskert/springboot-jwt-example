package de.borisskert.springjwt.user.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Collection;

@Component
public class JsonStringArrayConverter implements AttributeConverter<Collection<String>, String> {
    private static final TypeReference<Collection<String>> STRING_COLLECTION_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper json;

    public JsonStringArrayConverter(ObjectMapper jsonMapper) {
        this.json = jsonMapper;
    }

    @Override
    public String convertToDatabaseColumn(Collection<String> attribute) {
        try {
            return json.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<String> convertToEntityAttribute(String dbData) {
        try {
            return json.readValue(dbData, STRING_COLLECTION_TYPE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
