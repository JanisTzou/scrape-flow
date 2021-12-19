package com.github.web.scraping.lib.dom.data.parsing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Log4j2
public class JsonUtils {

    // ObjectMapper is thread safe, unless it gets reconfigured on the fly
    // http://stackoverflow.com/questions/3907929/should-i-declare-jacksons-objectmapper-as-a-static-field
    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static <T> Optional<T> parse(String data, Class<T> type) {
        try {
            T result = mapper.readValue(data, type);
            if (result != null) {
                return Optional.of(result);
            }
        } catch (Exception e) {
            log.error("Failed to parse json from string {}", data, e);
        }
        return Optional.empty();
    }

    public static <T> Optional<T> parse(byte[] bytes, Class<T> type) {
        try {
            T result = mapper.readValue(bytes, type);
            if (result == null) {
                return Optional.empty();
            }

            return  Optional.of(result);
        } catch (Exception e) {
            log.error("Failed to parse json from byte[]", e);
            return Optional.empty();
        }
    }

    public static Optional<String> write(Object object) {
        try {
            return Optional.of(mapper.writeValueAsString(object));
        } catch (Exception e) {
            log.error("Failed to serialize object to Json string", e);
            return Optional.empty();
        }
    }

}
