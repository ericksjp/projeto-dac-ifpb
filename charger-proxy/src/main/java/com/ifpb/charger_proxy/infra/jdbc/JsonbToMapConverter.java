package com.ifpb.charger_proxy.infra.jdbc;

import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mapping.MappingException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Mapeia um objeto PGobject do PostgreSQL com tipo jsonb para um Map<String, Object>.
 */
@ReadingConverter
public class JsonbToMapConverter implements Converter<PGobject, Map<String, Object>> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public @Nullable Map<String, Object> convert(final PGobject source) {
        final String value = source.getValue();
        if (value == null) return null;
        try {
            return mapper.readValue(value, new TypeReference<Map<String, Object>>() {});
        } catch (final Exception e) {
            throw new MappingException("Could not deserialize PGobject<jsonb> to Map. Value: " + source.getValue(), e);
        }
    }
}
