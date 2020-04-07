package de.borisskert.springjwt.user.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.sql.rowset.serial.SerialClob;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collection;

@Component
public class JsonStringArrayConverter implements AttributeConverter<Collection<String>, Clob> {
    private static final TypeReference<Collection<String>> STRING_COLLECTION_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper json;

    public JsonStringArrayConverter(ObjectMapper jsonMapper) {
        this.json = jsonMapper;
    }

    @Override
    public Clob convertToDatabaseColumn(Collection<String> attribute) {
        try {
            String jsonString = json.writeValueAsString(attribute);
            return new SerialClob(jsonString.toCharArray());
        } catch (JsonProcessingException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<String> convertToEntityAttribute(Clob dbData) {
        try {
            Reader characterStream = dbData.getCharacterStream();
            return json.readValue(characterStream, STRING_COLLECTION_TYPE);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
