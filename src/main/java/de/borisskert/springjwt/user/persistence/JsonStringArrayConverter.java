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
import java.util.List;

@Component
public class JsonStringArrayConverter implements AttributeConverter<List<String>, Clob> {
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper json;

    public JsonStringArrayConverter(ObjectMapper jsonMapper) {
        this.json = jsonMapper;
    }

    @Override
    public Clob convertToDatabaseColumn(List<String> attribute) {
        try {
            String jsonString = json.writeValueAsString(attribute);
            return new SerialClob(jsonString.toCharArray());
//            return json.readTree(jsonAsString);
        } catch (JsonProcessingException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(Clob dbData) {
        try {
//            String jsonAsString = json.writeValueAsString(dbData);
            Reader characterStream = dbData.getCharacterStream();
            return json.readValue(characterStream, STRING_LIST_TYPE);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
