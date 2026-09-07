package com.example.clivoapi.common.document;

public record Digits(String value) {

    public static Digits of(String text) {
        return new Digits(text == null ? "" : text.replaceAll("\\D", ""));
    }

    public boolean hasLength(int expected) {
        return value.length() == expected;
    }

    public boolean isSingleRepeatedDigit() {
        return value.chars().distinct().count() == 1;
    }

    public int at(int position) {
        return Character.getNumericValue(value.charAt(position));
    }

    public String asText() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
