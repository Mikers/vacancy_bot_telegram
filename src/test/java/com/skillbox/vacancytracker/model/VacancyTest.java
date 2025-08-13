package com.skillbox.vacancytracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VacancyTest {

    private Vacancy vacancy;

    @BeforeEach
    void setUp() {
        vacancy = new Vacancy();
    }

    @Test
    void shouldCreateEmptyVacancy() {
        assertThat(vacancy.getId()).isNull();
        assertThat(vacancy.getTitle()).isNull();
        assertThat(vacancy.getCompanyName()).isNull();
        assertThat(vacancy.getSalaryFrom()).isNull();
        assertThat(vacancy.getSalaryTo()).isNull();
        assertThat(vacancy.getCurrency()).isNull();
        assertThat(vacancy.getExperienceRequired()).isNull();
        assertThat(vacancy.getRegion()).isNull();
        assertThat(vacancy.getRegionCode()).isNull();
        assertThat(vacancy.getDescription()).isNull();
        assertThat(vacancy.getUrl()).isNull();
        assertThat(vacancy.getCreatedDate()).isNull();
        assertThat(vacancy.getModifiedDate()).isNull();
    }

    @Test
    void shouldSetAndGetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        
        vacancy.setId("123");
        vacancy.setTitle("Java Developer");
        vacancy.setCompanyName("TechCorp");
        vacancy.setSalaryFrom(100000);
        vacancy.setSalaryTo(150000);
        vacancy.setCurrency("RUB");
        vacancy.setExperienceRequired(3);
        vacancy.setRegion("Moscow");
        vacancy.setRegionCode(77);
        vacancy.setDescription("Develop Java applications");
        vacancy.setUrl("https://example.com/job/123");
        vacancy.setCreatedDate(now);
        vacancy.setModifiedDate(now);

        assertThat(vacancy.getId()).isEqualTo("123");
        assertThat(vacancy.getTitle()).isEqualTo("Java Developer");
        assertThat(vacancy.getCompanyName()).isEqualTo("TechCorp");
        assertThat(vacancy.getSalaryFrom()).isEqualTo(100000);
        assertThat(vacancy.getSalaryTo()).isEqualTo(150000);
        assertThat(vacancy.getCurrency()).isEqualTo("RUB");
        assertThat(vacancy.getExperienceRequired()).isEqualTo(3);
        assertThat(vacancy.getRegion()).isEqualTo("Moscow");
        assertThat(vacancy.getRegionCode()).isEqualTo(77);
        assertThat(vacancy.getDescription()).isEqualTo("Develop Java applications");
        assertThat(vacancy.getUrl()).isEqualTo("https://example.com/job/123");
        assertThat(vacancy.getCreatedDate()).isEqualTo(now);
        assertThat(vacancy.getModifiedDate()).isEqualTo(now);
    }

    @Test
    void shouldFormatSalaryWhenBothFromAndToAreNull() {
        vacancy.setSalaryFrom(null);
        vacancy.setSalaryTo(null);
        vacancy.setCurrency(null);
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("Не указана");
    }

    @Test
    void shouldFormatSalaryWithFromAndTo() {
        vacancy.setSalaryFrom(100000);
        vacancy.setSalaryTo(150000);
        vacancy.setCurrency("RUB");
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("100000-150000 RUB");
    }

    @Test
    void shouldFormatSalaryWithFromAndToAndNullCurrency() {
        vacancy.setSalaryFrom(100000);
        vacancy.setSalaryTo(150000);
        vacancy.setCurrency(null);
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("100000-150000 руб.");
    }

    @Test
    void shouldFormatSalaryWithFromAndToAndEmptyCurrency() {
        vacancy.setSalaryFrom(100000);
        vacancy.setSalaryTo(150000);
        vacancy.setCurrency("");
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("100000-150000 руб.");
    }

    @Test
    void shouldFormatSalaryWithFromOnly() {
        vacancy.setSalaryFrom(80000);
        vacancy.setSalaryTo(null);
        vacancy.setCurrency("USD");
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("От 80000 USD");
    }

    @Test
    void shouldFormatSalaryWithFromOnlyAndNullCurrency() {
        vacancy.setSalaryFrom(80000);
        vacancy.setSalaryTo(null);
        vacancy.setCurrency(null);
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("От 80000 руб.");
    }

    @Test
    void shouldFormatSalaryWithToOnly() {
        vacancy.setSalaryFrom(null);
        vacancy.setSalaryTo(120000);
        vacancy.setCurrency("EUR");
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("До 120000 EUR");
    }

    @Test
    void shouldFormatSalaryWithToOnlyAndNullCurrency() {
        vacancy.setSalaryFrom(null);
        vacancy.setSalaryTo(120000);
        vacancy.setCurrency(null);
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("До 120000 руб.");
    }

    @Test
    void shouldFormatSalaryWithZeroValues() {
        vacancy.setSalaryFrom(0);
        vacancy.setSalaryTo(0);
        vacancy.setCurrency("RUB");
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("0-0 RUB");
    }

    @Test
    void shouldFormatSalaryWithNegativeValues() {
        vacancy.setSalaryFrom(-1000);
        vacancy.setSalaryTo(-500);
        vacancy.setCurrency("RUB");
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo("-1000--500 RUB");
    }

    @Test
    void shouldFormatSalaryWithVeryLargeValues() {
        vacancy.setSalaryFrom(Integer.MAX_VALUE);
        vacancy.setSalaryTo(Integer.MAX_VALUE);
        vacancy.setCurrency("RUB");
        
        assertThat(vacancy.getFormattedSalary()).isEqualTo(Integer.MAX_VALUE + "-" + Integer.MAX_VALUE + " RUB");
    }

    @Test
    void shouldTestEqualsWithSameObject() {
        assertThat(vacancy).isEqualTo(vacancy);
    }

    @Test
    void shouldTestEqualsWithNull() {
        assertThat(vacancy).isNotEqualTo(null);
    }

    @Test
    void shouldTestEqualsWithDifferentClass() {
        assertThat(vacancy).isNotEqualTo("not a vacancy");
    }

    @Test
    void shouldTestEqualsWithSameId() {
        vacancy.setId("123");
        
        Vacancy other = new Vacancy();
        other.setId("123");
        
        assertThat(vacancy).isEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithDifferentIds() {
        vacancy.setId("123");
        
        Vacancy other = new Vacancy();
        other.setId("456");
        
        assertThat(vacancy).isNotEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithNullIds() {
        vacancy.setId(null);
        
        Vacancy other = new Vacancy();
        other.setId(null);
        
        assertThat(vacancy).isEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithOneNullId() {
        vacancy.setId("123");
        
        Vacancy other = new Vacancy();
        other.setId(null);
        
        assertThat(vacancy).isNotEqualTo(other);
    }

    @Test
    void shouldTestHashCodeConsistency() {
        vacancy.setId("123");
        
        int hashCode1 = vacancy.hashCode();
        int hashCode2 = vacancy.hashCode();
        
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void shouldTestHashCodeForEqualObjects() {
        vacancy.setId("123");
        
        Vacancy other = new Vacancy();
        other.setId("123");
        
        assertThat(vacancy.hashCode()).isEqualTo(other.hashCode());
    }

    @Test
    void shouldTestHashCodeWithNullId() {
        vacancy.setId(null);
        
        int hashCode = vacancy.hashCode();
        
        // Should not throw exception
        assertThat(hashCode).isEqualTo(0);
    }

    @Test
    void shouldTestToStringWithEmptyVacancy() {
        String toString = vacancy.toString();
        
        assertThat(toString).contains("Vacancy{");
        assertThat(toString).contains("id='null'");
        assertThat(toString).contains("title='null'");
        assertThat(toString).contains("companyName='null'");
        assertThat(toString).contains("salaryFrom=null");
        assertThat(toString).contains("salaryTo=null");
        assertThat(toString).contains("experienceRequired=null");
        assertThat(toString).contains("region='null'");
    }

    @Test
    void shouldTestToStringWithAllFields() {
        vacancy.setId("123");
        vacancy.setTitle("Java Developer");
        vacancy.setCompanyName("TechCorp");
        vacancy.setSalaryFrom(100000);
        vacancy.setSalaryTo(150000);
        vacancy.setExperienceRequired(3);
        vacancy.setRegion("Moscow");
        
        String toString = vacancy.toString();
        
        assertThat(toString).contains("Vacancy{");
        assertThat(toString).contains("id='123'");
        assertThat(toString).contains("title='Java Developer'");
        assertThat(toString).contains("companyName='TechCorp'");
        assertThat(toString).contains("salaryFrom=100000");
        assertThat(toString).contains("salaryTo=150000");
        assertThat(toString).contains("experienceRequired=3");
        assertThat(toString).contains("region='Moscow'");
    }

    @Test
    void shouldTestToStringWithSpecialCharacters() {
        vacancy.setId("test-id-123");
        vacancy.setTitle("C++ Developer & Team Lead");
        vacancy.setCompanyName("Company \"Name\" Ltd.");
        vacancy.setRegion("Saint-Petersburg");
        
        String toString = vacancy.toString();
        
        assertThat(toString).contains("id='test-id-123'");
        assertThat(toString).contains("title='C++ Developer & Team Lead'");
        assertThat(toString).contains("companyName='Company \"Name\" Ltd.'");
        assertThat(toString).contains("region='Saint-Petersburg'");
    }

    @Test
    void shouldHandleNullFieldsIndividually() {
        vacancy.setId(null);
        assertThat(vacancy.getId()).isNull();
        
        vacancy.setTitle(null);
        assertThat(vacancy.getTitle()).isNull();
        
        vacancy.setCompanyName(null);
        assertThat(vacancy.getCompanyName()).isNull();
        
        vacancy.setSalaryFrom(null);
        assertThat(vacancy.getSalaryFrom()).isNull();
        
        vacancy.setSalaryTo(null);
        assertThat(vacancy.getSalaryTo()).isNull();
        
        vacancy.setCurrency(null);
        assertThat(vacancy.getCurrency()).isNull();
        
        vacancy.setExperienceRequired(null);
        assertThat(vacancy.getExperienceRequired()).isNull();
        
        vacancy.setRegion(null);
        assertThat(vacancy.getRegion()).isNull();
        
        vacancy.setRegionCode(null);
        assertThat(vacancy.getRegionCode()).isNull();
        
        vacancy.setDescription(null);
        assertThat(vacancy.getDescription()).isNull();
        
        vacancy.setUrl(null);
        assertThat(vacancy.getUrl()).isNull();
        
        vacancy.setCreatedDate(null);
        assertThat(vacancy.getCreatedDate()).isNull();
        
        vacancy.setModifiedDate(null);
        assertThat(vacancy.getModifiedDate()).isNull();
    }

    @Test
    void shouldHandleEmptyStringFields() {
        vacancy.setId("");
        vacancy.setTitle("");
        vacancy.setCompanyName("");
        vacancy.setCurrency("");
        vacancy.setRegion("");
        vacancy.setDescription("");
        vacancy.setUrl("");
        
        assertThat(vacancy.getId()).isEqualTo("");
        assertThat(vacancy.getTitle()).isEqualTo("");
        assertThat(vacancy.getCompanyName()).isEqualTo("");
        assertThat(vacancy.getCurrency()).isEqualTo("");
        assertThat(vacancy.getRegion()).isEqualTo("");
        assertThat(vacancy.getDescription()).isEqualTo("");
        assertThat(vacancy.getUrl()).isEqualTo("");
    }

    @Test
    void shouldHandleWhitespaceStringFields() {
        vacancy.setId("   ");
        vacancy.setTitle("   ");
        vacancy.setCompanyName("   ");
        
        assertThat(vacancy.getId()).isEqualTo("   ");
        assertThat(vacancy.getTitle()).isEqualTo("   ");
        assertThat(vacancy.getCompanyName()).isEqualTo("   ");
    }

    @Test
    void shouldHandleVeryLongStringFields() {
        String longString = "A".repeat(1000);
        
        vacancy.setId(longString);
        vacancy.setTitle(longString);
        vacancy.setDescription(longString);
        
        assertThat(vacancy.getId()).isEqualTo(longString);
        assertThat(vacancy.getTitle()).isEqualTo(longString);
        assertThat(vacancy.getDescription()).isEqualTo(longString);
    }

    @Test
    void shouldHandleDateTimeFields() {
        LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 10, 30, 0);
        LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59, 59);
        
        vacancy.setCreatedDate(pastDate);
        vacancy.setModifiedDate(futureDate);
        
        assertThat(vacancy.getCreatedDate()).isEqualTo(pastDate);
        assertThat(vacancy.getModifiedDate()).isEqualTo(futureDate);
    }
}