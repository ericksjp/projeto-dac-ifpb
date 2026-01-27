package com.ifpb.charger_proxy.infra.jdbc;

import java.util.Map;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mapping.MappingException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Mapeia um Map<String, Object> para um objeto PGobject do PostgreSQL com tipo jsonb.
 */
@WritingConverter
public class MapToJsonbConverter implements Converter<Map<String, Object>, PGobject> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public PGobject convert(final Map<String, Object> source) {
        final PGobject pg = new PGobject();
        pg.setType("jsonb");

        try {
            pg.setValue(mapper.writeValueAsString(source));
            return pg;
        } catch (final Exception e) {
            throw new MappingException("Could not serialize Map to PGobject<jsonb>. Value: " + e.toString(), e);
        }
    }
}
