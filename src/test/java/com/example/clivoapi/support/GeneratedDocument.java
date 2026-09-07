package com.example.clivoapi.support;

import com.example.clivoapi.core.customer.NationalId;

public final class GeneratedDocument {

    private static final int BODY_DIGITS = 9;

    private static final int BODY_LIMIT = 1_000_000_000;

    private GeneratedDocument() {
    }

    public static NationalId nationalIdFor(String subject) {
        return new NationalId(nationalIdTextFor(subject));
    }

    public static String nationalIdTextFor(String subject) {
        String body = bodyFor(subject);
        String withFirstCheckDigit = body + checkDigitOf(body);
        return withFirstCheckDigit + checkDigitOf(withFirstCheckDigit);
    }

    private static String bodyFor(String subject) {
        String body = "%0" + BODY_DIGITS + "d";
        String candidate = body.formatted(Math.abs(subject.hashCode() % BODY_LIMIT));
        return repeatsOneDigit(candidate) ? bodyFor(subject + "-") : candidate;
    }

    private static boolean repeatsOneDigit(String candidate) {
        return candidate.chars().distinct().count() == 1;
    }

    private static int checkDigitOf(String digits) {
        int weightedSum = 0;
        for (int position = 0; position < digits.length(); position++) {
            weightedSum += Character.getNumericValue(digits.charAt(position)) * (digits.length() + 1 - position);
        }
        int remainder = weightedSum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
