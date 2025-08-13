package com.skillbox.vacancytracker.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlBuilderTest {
    
    @Test
    void shouldCreateUrlBuilderWithBaseUrl() {
        String baseUrl = "https://api.example.com";
        
        UrlBuilder builder = new UrlBuilder(baseUrl);
        
        assertThat(builder.build()).isEqualTo(baseUrl);
    }
    
    @Test
    void shouldBuildSimpleUrlWithoutParams() {
        String url = new UrlBuilder("https://api.example.com/v1")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com/v1");
    }
    
    @Test
    void shouldAppendPathWithLeadingSlash() {
        String url = new UrlBuilder("https://api.example.com")
            .appendPath("/vacancies")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com/vacancies");
    }
    
    @Test
    void shouldAppendPathWithoutLeadingSlash() {
        String url = new UrlBuilder("https://api.example.com")
            .appendPath("vacancies")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com/vacancies");
    }
    
    @Test
    void shouldAppendMultiplePaths() {
        String url = new UrlBuilder("https://api.example.com")
            .appendPath("/v1")
            .appendPath("vacancies")
            .appendPath("/search")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com/v1/vacancies/search");
    }
    
    @Test
    void shouldAddStringParameter() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("keyword", "java")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?keyword=java");
    }
    
    @Test
    void shouldAddIntegerParameter() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("page", 1)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?page=1");
    }
    
    @Test
    void shouldAddMultipleParameters() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("keyword", "java")
            .addParam("page", 1)
            .addParam("limit", 10)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?keyword=java&page=1&limit=10");
    }
    
    @Test
    void shouldAddEncodedParameter() {
        String url = new UrlBuilder("https://api.example.com")
            .addEncodedParam("keyword", "java developer")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?keyword=java+developer");
    }
    
    @Test
    void shouldEncodeSpecialCharacters() {
        String url = new UrlBuilder("https://api.example.com")
            .addEncodedParam("keyword", "C++ & Java")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?keyword=C%2B%2B+%26+Java");
    }
    
    @Test
    void shouldEncodeUnicodeCharacters() {
        String url = new UrlBuilder("https://api.example.com")
            .addEncodedParam("city", "Москва")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?city=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0");
    }
    
    @Test
    void shouldIgnoreNullStringKey() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam(null, "value")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldIgnoreNullStringValue() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("key", (String) null)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldIgnoreNullIntegerKey() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam(null, 123)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldIgnoreNullIntegerValue() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("key", (Integer) null)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldIgnoreNullEncodedKey() {
        String url = new UrlBuilder("https://api.example.com")
            .addEncodedParam(null, "value")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldIgnoreNullEncodedValue() {
        String url = new UrlBuilder("https://api.example.com")
            .addEncodedParam("key", null)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldIgnoreEmptyEncodedValue() {
        String url = new UrlBuilder("https://api.example.com")
            .addEncodedParam("key", "")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldIgnoreWhitespaceOnlyEncodedValue() {
        String url = new UrlBuilder("https://api.example.com")
            .addEncodedParam("key", "   ")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldHandleEmptyPath() {
        String url = new UrlBuilder("https://api.example.com")
            .appendPath("")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldHandleNullPath() {
        String url = new UrlBuilder("https://api.example.com")
            .appendPath(null)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com");
    }
    
    @Test
    void shouldChainMethodCalls() {
        String url = new UrlBuilder("https://api.example.com")
            .appendPath("/v1")
            .appendPath("vacancies")
            .addParam("keyword", "java")
            .addParam("page", 1)
            .addEncodedParam("location", "New York")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com/v1/vacancies?keyword=java&page=1&location=New+York");
    }
    
    @Test
    void shouldOverrideParameterWithSameKey() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("page", 1)
            .addParam("page", 2)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?page=2");
    }
    
    @Test
    void shouldMixStringAndIntegerParameters() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("keyword", "java")
            .addParam("page", 1)
            .addParam("region", "moscow")
            .addParam("salary", 50000)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?keyword=java&page=1&region=moscow&salary=50000");
    }
    
    @Test
    void shouldHandleZeroAsIntegerValue() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("page", 0)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?page=0");
    }
    
    @Test
    void shouldHandleNegativeIntegerValue() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("offset", -1)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?offset=-1");
    }
    
    @Test
    void shouldHandleVeryLargeIntegerValue() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("id", Integer.MAX_VALUE)
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?id=" + Integer.MAX_VALUE);
    }
    
    @Test
    void shouldHandleEmptyStringParameter() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("keyword", "")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?keyword=");
    }
    
    @Test
    void shouldHandleWhitespaceStringParameter() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("keyword", "   ")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?keyword=   ");
    }
    
    @Test
    void shouldPreserveParameterOrder() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("z", "last")
            .addParam("a", "first")
            .addParam("m", "middle")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?z=last&a=first&m=middle");
    }
    
    @Test
    void shouldHandleComplexRealWorldExample() {
        String url = new UrlBuilder("https://opendata.trudvsem.ru")
            .appendPath("/api/v1")
            .appendPath("vacancies")
            .addParam("limit", 100)
            .addParam("offset", 0)
            .addEncodedParam("text", "Java разработчик")
            .addParam("regionCode", 77)
            .addParam("salary", 100000)
            .build();
        
        assertThat(url).startsWith("https://opendata.trudvsem.ru/api/v1/vacancies?");
        assertThat(url).contains("limit=100");
        assertThat(url).contains("offset=0");
        assertThat(url).contains("text=Java+%D1%80%D0%B0%D0%B7%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%87%D0%B8%D0%BA");
        assertThat(url).contains("regionCode=77");
        assertThat(url).contains("salary=100000");
    }
    
    @Test
    void shouldHandleBaseUrlWithExistingPath() {
        String url = new UrlBuilder("https://api.example.com/existing/path")
            .appendPath("/additional")
            .addParam("key", "value")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com/existing/path/additional?key=value");
    }
    
    @Test
    void shouldHandleBaseUrlWithTrailingSlash() {
        String url = new UrlBuilder("https://api.example.com/")
            .appendPath("path")
            .build();
        
        // The current implementation doesn't handle trailing slash removal
        // This test documents the actual behavior
        assertThat(url).isEqualTo("https://api.example.com//path");
    }
    
    @Test
    void shouldHandleOnlyQueryParameters() {
        String url = new UrlBuilder("https://api.example.com")
            .addParam("param1", "value1")
            .addParam("param2", "value2")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com?param1=value1&param2=value2");
    }
    
    @Test
    void shouldHandleOnlyPathAppends() {
        String url = new UrlBuilder("https://api.example.com")
            .appendPath("/level1")
            .appendPath("/level2")
            .appendPath("level3")
            .build();
        
        assertThat(url).isEqualTo("https://api.example.com/level1/level2/level3");
    }
    
    @Test
    void shouldReturnBuilderInstanceForMethodChaining() {
        UrlBuilder builder = new UrlBuilder("https://api.example.com");
        
        UrlBuilder result1 = builder.appendPath("/path");
        UrlBuilder result2 = builder.addParam("key", "value");
        UrlBuilder result3 = builder.addParam("num", 42);
        UrlBuilder result4 = builder.addEncodedParam("encoded", "test value");
        
        assertThat(result1).isSameAs(builder);
        assertThat(result2).isSameAs(builder);
        assertThat(result3).isSameAs(builder);
        assertThat(result4).isSameAs(builder);
    }
}