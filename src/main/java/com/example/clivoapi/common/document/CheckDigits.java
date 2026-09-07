package com.example.clivoapi.common.document;

import java.util.stream.IntStream;

public record CheckDigits(Digits digits, int bodyLength, int highestWeight) {

    private static final int MODULUS = 11;

    public boolean confirmTheDocument() {
        return confirms(bodyLength) && confirms(bodyLength + 1);
    }

    private boolean confirms(int position) {
        return digits.at(position) == expectedAt(position);
    }

    private int expectedAt(int position) {
        return digitForRemainder(weightedSumUpTo(position) % MODULUS);
    }

    private int weightedSumUpTo(int position) {
        return IntStream.range(0, position)
                .map(index -> digits.at(index) * weightAtDistance(position - index))
                .sum();
    }

    private int weightAtDistance(int distanceFromCheckDigit) {
        return 2 + (distanceFromCheckDigit - 1) % (highestWeight - 1);
    }

    private static int digitForRemainder(int remainder) {
        return remainder < 2 ? 0 : MODULUS - remainder;
    }
}
