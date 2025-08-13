package com.skillbox.vacancytracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchCriteriaTest {

    private SearchCriteria searchCriteria;

    @BeforeEach
    void setUp() {
        searchCriteria = new SearchCriteria();
    }

    @Test
    void shouldCreateEmptySearchCriteria() {
        assertThat(searchCriteria.getRegionCode()).isNull();
        assertThat(searchCriteria.getMinimumExperience()).isNull();
        assertThat(searchCriteria.getMinimumSalary()).isNull();
        assertThat(searchCriteria.getKeyword()).isNull();
        assertThat(searchCriteria.isEmpty()).isTrue();
    }

    @Test
    void shouldSetAndGetRegionCode() {
        searchCriteria.setRegionCode(77);
        assertThat(searchCriteria.getRegionCode()).isEqualTo(77);
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldSetAndGetMinimumExperience() {
        searchCriteria.setMinimumExperience(3);
        assertThat(searchCriteria.getMinimumExperience()).isEqualTo(3);
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldSetAndGetMinimumSalary() {
        searchCriteria.setMinimumSalary(100000);
        assertThat(searchCriteria.getMinimumSalary()).isEqualTo(100000);
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldSetAndGetKeyword() {
        searchCriteria.setKeyword("Java Developer");
        assertThat(searchCriteria.getKeyword()).isEqualTo("Java Developer");
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldSetAndGetNullValues() {
        searchCriteria.setRegionCode(77);
        searchCriteria.setMinimumExperience(3);
        searchCriteria.setMinimumSalary(100000);
        searchCriteria.setKeyword("Java");

        searchCriteria.setRegionCode(null);
        searchCriteria.setMinimumExperience(null);
        searchCriteria.setMinimumSalary(null);
        searchCriteria.setKeyword(null);

        assertThat(searchCriteria.getRegionCode()).isNull();
        assertThat(searchCriteria.getMinimumExperience()).isNull();
        assertThat(searchCriteria.getMinimumSalary()).isNull();
        assertThat(searchCriteria.getKeyword()).isNull();
        assertThat(searchCriteria.isEmpty()).isTrue();
    }

    @Test
    void shouldDetectEmptyWithNullKeyword() {
        assertThat(searchCriteria.isEmpty()).isTrue();
        
        searchCriteria.setKeyword(null);
        assertThat(searchCriteria.isEmpty()).isTrue();
    }

    @Test
    void shouldDetectEmptyWithEmptyKeyword() {
        searchCriteria.setKeyword("");
        assertThat(searchCriteria.isEmpty()).isTrue();
    }

    @Test
    void shouldDetectEmptyWithWhitespaceKeyword() {
        searchCriteria.setKeyword("   ");
        assertThat(searchCriteria.isEmpty()).isTrue();
    }

    @Test
    void shouldDetectNotEmptyWithValidKeyword() {
        searchCriteria.setKeyword("Java");
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldDetectNotEmptyWithKeywordContainingWhitespace() {
        searchCriteria.setKeyword("  Java Developer  ");
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldDetectNotEmptyWithAnyNonNullField() {
        // Test each field individually
        SearchCriteria criteria1 = new SearchCriteria();
        criteria1.setRegionCode(77);
        assertThat(criteria1.isEmpty()).isFalse();

        SearchCriteria criteria2 = new SearchCriteria();
        criteria2.setMinimumExperience(2);
        assertThat(criteria2.isEmpty()).isFalse();

        SearchCriteria criteria3 = new SearchCriteria();
        criteria3.setMinimumSalary(50000);
        assertThat(criteria3.isEmpty()).isFalse();

        SearchCriteria criteria4 = new SearchCriteria();
        criteria4.setKeyword("Python");
        assertThat(criteria4.isEmpty()).isFalse();
    }

    @Test
    void shouldTestEqualsWithSameObject() {
        assertThat(searchCriteria).isEqualTo(searchCriteria);
    }

    @Test
    void shouldTestEqualsWithNull() {
        assertThat(searchCriteria).isNotEqualTo(null);
    }

    @Test
    void shouldTestEqualsWithDifferentClass() {
        assertThat(searchCriteria).isNotEqualTo("not a SearchCriteria");
    }

    @Test
    void shouldTestEqualsWithEmptyCriteria() {
        SearchCriteria other = new SearchCriteria();
        assertThat(searchCriteria).isEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithIdenticalCriteria() {
        searchCriteria.setRegionCode(77);
        searchCriteria.setMinimumExperience(3);
        searchCriteria.setMinimumSalary(100000);
        searchCriteria.setKeyword("Java");

        SearchCriteria other = new SearchCriteria();
        other.setRegionCode(77);
        other.setMinimumExperience(3);
        other.setMinimumSalary(100000);
        other.setKeyword("Java");

        assertThat(searchCriteria).isEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithDifferentRegionCode() {
        searchCriteria.setRegionCode(77);
        SearchCriteria other = new SearchCriteria();
        other.setRegionCode(78);

        assertThat(searchCriteria).isNotEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithDifferentExperience() {
        searchCriteria.setMinimumExperience(3);
        SearchCriteria other = new SearchCriteria();
        other.setMinimumExperience(5);

        assertThat(searchCriteria).isNotEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithDifferentSalary() {
        searchCriteria.setMinimumSalary(100000);
        SearchCriteria other = new SearchCriteria();
        other.setMinimumSalary(150000);

        assertThat(searchCriteria).isNotEqualTo(other);
    }

    @Test
    void shouldTestEqualsWithDifferentKeyword() {
        searchCriteria.setKeyword("Java");
        SearchCriteria other = new SearchCriteria();
        other.setKeyword("Python");

        assertThat(searchCriteria).isNotEqualTo(other);
    }

    @Test
    void shouldTestHashCodeConsistency() {
        int hashCode1 = searchCriteria.hashCode();
        int hashCode2 = searchCriteria.hashCode();
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void shouldTestHashCodeForEqualObjects() {
        searchCriteria.setRegionCode(77);
        searchCriteria.setKeyword("Java");

        SearchCriteria other = new SearchCriteria();
        other.setRegionCode(77);
        other.setKeyword("Java");

        assertThat(searchCriteria.hashCode()).isEqualTo(other.hashCode());
    }

    @Test
    void shouldTestHashCodeForDifferentObjects() {
        searchCriteria.setRegionCode(77);
        SearchCriteria other = new SearchCriteria();
        other.setRegionCode(78);

        // Hash codes may be different (not guaranteed, but likely)
        assertThat(searchCriteria.hashCode()).isNotEqualTo(other.hashCode());
    }

    @Test
    void shouldTestToStringWithEmptyCriteria() {
        String toString = searchCriteria.toString();
        assertThat(toString).contains("SearchCriteria{");
        assertThat(toString).contains("regionCode=null");
        assertThat(toString).contains("minimumExperience=null");
        assertThat(toString).contains("minimumSalary=null");
        assertThat(toString).contains("keyword='null'");
    }

    @Test
    void shouldTestToStringWithAllFields() {
        searchCriteria.setRegionCode(77);
        searchCriteria.setMinimumExperience(3);
        searchCriteria.setMinimumSalary(100000);
        searchCriteria.setKeyword("Java Developer");

        String toString = searchCriteria.toString();
        assertThat(toString).contains("SearchCriteria{");
        assertThat(toString).contains("regionCode=77");
        assertThat(toString).contains("minimumExperience=3");
        assertThat(toString).contains("minimumSalary=100000");
        assertThat(toString).contains("keyword='Java Developer'");
    }

    @Test
    void shouldTestToStringWithPartialFields() {
        searchCriteria.setRegionCode(78);
        searchCriteria.setKeyword("Python");

        String toString = searchCriteria.toString();
        assertThat(toString).contains("regionCode=78");
        assertThat(toString).contains("minimumExperience=null");
        assertThat(toString).contains("minimumSalary=null");
        assertThat(toString).contains("keyword='Python'");
    }

    @Test
    void shouldHandleNegativeValues() {
        searchCriteria.setRegionCode(-1);
        searchCriteria.setMinimumExperience(-5);
        searchCriteria.setMinimumSalary(-10000);

        assertThat(searchCriteria.getRegionCode()).isEqualTo(-1);
        assertThat(searchCriteria.getMinimumExperience()).isEqualTo(-5);
        assertThat(searchCriteria.getMinimumSalary()).isEqualTo(-10000);
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldHandleZeroValues() {
        searchCriteria.setRegionCode(0);
        searchCriteria.setMinimumExperience(0);
        searchCriteria.setMinimumSalary(0);

        assertThat(searchCriteria.getRegionCode()).isEqualTo(0);
        assertThat(searchCriteria.getMinimumExperience()).isEqualTo(0);
        assertThat(searchCriteria.getMinimumSalary()).isEqualTo(0);
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldHandleVeryLargeValues() {
        searchCriteria.setRegionCode(Integer.MAX_VALUE);
        searchCriteria.setMinimumExperience(Integer.MAX_VALUE);
        searchCriteria.setMinimumSalary(Integer.MAX_VALUE);

        assertThat(searchCriteria.getRegionCode()).isEqualTo(Integer.MAX_VALUE);
        assertThat(searchCriteria.getMinimumExperience()).isEqualTo(Integer.MAX_VALUE);
        assertThat(searchCriteria.getMinimumSalary()).isEqualTo(Integer.MAX_VALUE);
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldHandleSpecialCharactersInKeyword() {
        String specialKeyword = "C++ / C# Developer!@#$%^&*()";
        searchCriteria.setKeyword(specialKeyword);

        assertThat(searchCriteria.getKeyword()).isEqualTo(specialKeyword);
        assertThat(searchCriteria.isEmpty()).isFalse();
    }

    @Test
    void shouldHandleUnicodeCharactersInKeyword() {
        String unicodeKeyword = "Разработчик Java 程序员 🚀";
        searchCriteria.setKeyword(unicodeKeyword);

        assertThat(searchCriteria.getKeyword()).isEqualTo(unicodeKeyword);
        assertThat(searchCriteria.isEmpty()).isFalse();
    }
}