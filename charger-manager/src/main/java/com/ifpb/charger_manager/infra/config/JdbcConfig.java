package com.ifpb.charger_manager.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifpb.charger_manager.domain.enums.BillingType;
import com.ifpb.charger_manager.domain.enums.ChargeStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuração do Spring Data JDBC
 */
@Configuration
@EnableJdbcRepositories(basePackages = "com.ifpb.charger_manager.domain.repository")
public class JdbcConfig extends AbstractJdbcConfiguration {
    
    private final ObjectMapper objectMapper;
    
    public JdbcConfig(@Lazy ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Bean
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new BillingTypeToStringConverter());
        converters.add(new StringToBillingTypeConverter());
        converters.add(new ChargeStatusToStringConverter());
        converters.add(new StringToChargeStatusConverter());
        converters.add(new MapToJsonConverter(objectMapper));
        converters.add(new JsonToMapConverter(objectMapper));
        return new JdbcCustomConversions(converters);
    }
    
    // Converters para BillingType
    @WritingConverter
    static class BillingTypeToStringConverter implements Converter<BillingType, String> {
        @Override
        public String convert(BillingType source) {
            return source.name();
        }
    }
    
    @ReadingConverter
    static class StringToBillingTypeConverter implements Converter<String, BillingType> {
        @Override
        public BillingType convert(String source) {
            return BillingType.valueOf(source);
        }
    }
    
    // Converters para ChargeStatus
    @WritingConverter
    static class ChargeStatusToStringConverter implements Converter<ChargeStatus, String> {
        @Override
        public String convert(ChargeStatus source) {
            return source.name();
        }
    }
    
    @ReadingConverter
    static class StringToChargeStatusConverter implements Converter<String, ChargeStatus> {
        @Override
        public ChargeStatus convert(String source) {
            return ChargeStatus.valueOf(source);
        }
    }
    
    // Converters para Map <-> JSON
    @WritingConverter
    static class MapToJsonConverter implements Converter<Map<String, Object>, String> {
        private final ObjectMapper objectMapper;
        
        MapToJsonConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }
        
        @Override
        public String convert(Map<String, Object> source) {
            try {
                return objectMapper.writeValueAsString(source);
            } catch (Exception e) {
                throw new RuntimeException("Error converting Map to JSON", e);
            }
        }
    }
    
    @ReadingConverter
    static class JsonToMapConverter implements Converter<String, Map<String, Object>> {
        private final ObjectMapper objectMapper;
        
        JsonToMapConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> convert(String source) {
            try {
                return objectMapper.readValue(source, Map.class);
            } catch (Exception e) {
                throw new RuntimeException("Error converting JSON to Map", e);
            }
        }
    }
}
