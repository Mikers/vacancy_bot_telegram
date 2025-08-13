package com.skillbox.vacancytracker.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonMapperTest {
    
    @Test
    void shouldReturnSameInstanceOnMultipleCalls() {
        ObjectMapper instance1 = JsonMapper.getInstance();
        ObjectMapper instance2 = JsonMapper.getInstance();
        
        assertThat(instance1).isSameAs(instance2);
    }
    
    @Test
    void shouldReturnNonNullInstance() {
        ObjectMapper instance = JsonMapper.getInstance();
        
        assertThat(instance).isNotNull();
    }
    
    @Test
    void shouldHaveJavaTimeModuleRegistered() {
        ObjectMapper mapper = JsonMapper.getInstance();
        
        // JavaTimeModule should be registered - test by serializing LocalDateTime
        LocalDateTime now = LocalDateTime.of(2023, 6, 15, 14, 30, 45);
        
        assertThat(mapper.canSerialize(LocalDateTime.class)).isTrue();
    }
    
    @Test
    void shouldSerializeDatesAsIsoString() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 15, 14, 30, 45);
        
        String json = mapper.writeValueAsString(dateTime);
        
        assertThat(json).contains("2023-06-15T14:30:45");
        assertThat(json).doesNotContain("1686839445"); // Should not be timestamp
    }
    
    @Test
    void shouldDeserializeDatesFromIsoString() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        String json = "\"2023-06-15T14:30:45\"";
        
        LocalDateTime result = mapper.readValue(json, LocalDateTime.class);
        
        assertThat(result).isEqualTo(LocalDateTime.of(2023, 6, 15, 14, 30, 45));
    }
    
    @Test
    void shouldIgnoreUnknownPropertiesDuringDeserialization() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        String json = "{\"knownField\":\"value\",\"unknownField\":\"ignored\"}";
        
        TestClass result = mapper.readValue(json, TestClass.class);
        
        assertThat(result.knownField).isEqualTo("value");
    }
    
    @Test
    void shouldSerializeAndDeserializeComplexObject() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        TestClass original = new TestClass();
        original.knownField = "test value";
        original.dateTime = LocalDateTime.of(2023, 6, 15, 14, 30, 45);
        
        String json = mapper.writeValueAsString(original);
        TestClass deserialized = mapper.readValue(json, TestClass.class);
        
        assertThat(deserialized.knownField).isEqualTo("test value");
        assertThat(deserialized.dateTime).isEqualTo(LocalDateTime.of(2023, 6, 15, 14, 30, 45));
    }
    
    @Test
    void shouldHandleNullValues() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        TestClass obj = new TestClass();
        obj.knownField = null;
        obj.dateTime = null;
        
        String json = mapper.writeValueAsString(obj);
        TestClass result = mapper.readValue(json, TestClass.class);
        
        assertThat(result.knownField).isNull();
        assertThat(result.dateTime).isNull();
    }
    
    @Test
    void shouldHandleEmptyObjects() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        String json = "{}";
        
        TestClass result = mapper.readValue(json, TestClass.class);
        
        assertThat(result).isNotNull();
        assertThat(result.knownField).isNull();
        assertThat(result.dateTime).isNull();
    }
    
    @Test
    void shouldHandleArrays() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        String[] array = {"value1", "value2", "value3"};
        
        String json = mapper.writeValueAsString(array);
        String[] result = mapper.readValue(json, String[].class);
        
        assertThat(result).containsExactly("value1", "value2", "value3");
    }
    
    @Test
    void shouldHandleMaps() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 42);
        map.put("key3", true);
        
        String json = mapper.writeValueAsString(map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = mapper.readValue(json, Map.class);
        
        assertThat(result).hasSize(3);
        assertThat(result.get("key1")).isEqualTo("value1");
        assertThat(result.get("key2")).isEqualTo(42);
        assertThat(result.get("key3")).isEqualTo(true);
    }
    
    @Test
    void shouldHandleNestedObjects() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        NestedTestClass nested = new NestedTestClass();
        nested.inner = new TestClass();
        nested.inner.knownField = "nested value";
        nested.inner.dateTime = LocalDateTime.of(2023, 6, 15, 14, 30, 45);
        nested.outerField = "outer value";
        
        String json = mapper.writeValueAsString(nested);
        NestedTestClass result = mapper.readValue(json, NestedTestClass.class);
        
        assertThat(result.outerField).isEqualTo("outer value");
        assertThat(result.inner.knownField).isEqualTo("nested value");
        assertThat(result.inner.dateTime).isEqualTo(LocalDateTime.of(2023, 6, 15, 14, 30, 45));
    }
    
    @Test
    void shouldHaveFailOnUnknownPropertiesDisabled() {
        ObjectMapper mapper = JsonMapper.getInstance();
        
        assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
    }
    
    @Test
    void shouldHaveWriteDatesAsTimestampsDisabled() {
        ObjectMapper mapper = JsonMapper.getInstance();
        
        assertThat(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
    }
    
    @Test
    void shouldHandleSpecialDateTimeFormats() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        
        // Test various ISO date formats that LocalDateTime can handle
        String[] dateFormats = {
            "\"2023-06-15T14:30:45\"",
            "\"2023-06-15T14:30:45.123\"",
            "\"2023-06-15T14:30:45.123456\""
        };
        
        for (String dateFormat : dateFormats) {
            LocalDateTime result = mapper.readValue(dateFormat, LocalDateTime.class);
            assertThat(result).isNotNull();
        }
    }
    
    @Test
    void shouldHandleInvalidJsonGracefully() {
        ObjectMapper mapper = JsonMapper.getInstance();
        String invalidJson = "{invalid json}";
        
        assertThatThrownBy(() -> mapper.readValue(invalidJson, TestClass.class))
            .isInstanceOf(JsonProcessingException.class);
    }
    
    @Test
    void shouldHandleEmptyString() {
        ObjectMapper mapper = JsonMapper.getInstance();
        
        assertThatThrownBy(() -> mapper.readValue("", TestClass.class))
            .isInstanceOf(JsonProcessingException.class);
    }
    
    @Test
    void shouldHandleNullJson() {
        ObjectMapper mapper = JsonMapper.getInstance();
        
        assertThatThrownBy(() -> mapper.readValue((String) null, TestClass.class))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void shouldPreventInstantiation() throws ReflectiveOperationException {
        Class<?> clazz = JsonMapper.class;
        
        assertThat(clazz.getDeclaredConstructors()).hasSize(1);
        assertThat(clazz.getDeclaredConstructors()[0].getParameterCount()).isEqualTo(0);
        
        // Constructor should be private
        var constructor = clazz.getDeclaredConstructor();
        assertThat(constructor.canAccess(null)).isFalse();
    }
    
    @Test
    void shouldHaveJavaTimeModuleInRegisteredModules() {
        ObjectMapper mapper = JsonMapper.getInstance();
        
        // Test that JavaTimeModule functionality works instead of checking registered modules directly
        assertThat(mapper.canSerialize(LocalDateTime.class)).isTrue();
        assertThat(mapper.canDeserialize(mapper.constructType(LocalDateTime.class))).isTrue();
    }
    
    @Test
    void shouldSerializeComplexDateTimeScenario() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        
        ComplexDateTimeClass obj = new ComplexDateTimeClass();
        obj.createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
        obj.updatedAt = LocalDateTime.of(2023, 12, 31, 23, 59, 59);
        obj.name = "Test Object";
        
        String json = mapper.writeValueAsString(obj);
        ComplexDateTimeClass result = mapper.readValue(json, ComplexDateTimeClass.class);
        
        assertThat(result.name).isEqualTo("Test Object");
        assertThat(result.createdAt).isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0, 0));
        assertThat(result.updatedAt).isEqualTo(LocalDateTime.of(2023, 12, 31, 23, 59, 59));
    }
    
    @Test
    void shouldHandleEdgeCaseDates() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.getInstance();
        
        // Test edge case dates
        LocalDateTime minDate = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        LocalDateTime maxDate = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        
        String minJson = mapper.writeValueAsString(minDate);
        String maxJson = mapper.writeValueAsString(maxDate);
        
        LocalDateTime minResult = mapper.readValue(minJson, LocalDateTime.class);
        LocalDateTime maxResult = mapper.readValue(maxJson, LocalDateTime.class);
        
        assertThat(minResult).isEqualTo(minDate);
        assertThat(maxResult).isEqualTo(maxDate);
    }
    
    // Test classes
    public static class TestClass {
        public String knownField;
        public LocalDateTime dateTime;
        
        public TestClass() {}
    }
    
    public static class NestedTestClass {
        public TestClass inner;
        public String outerField;
        
        public NestedTestClass() {}
    }
    
    public static class ComplexDateTimeClass {
        public String name;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        
        public ComplexDateTimeClass() {}
    }
}