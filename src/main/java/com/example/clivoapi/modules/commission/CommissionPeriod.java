package com.example.clivoapi.modules.commission;

import com.example.clivoapi.common.exception.BusinessException;
import java.time.LocalDate;
import java.time.YearMonth;

public record CommissionPeriod(LocalDate firstDay) {

    private static final int EARLIEST_YEAR = 2000;
    private static final int LATEST_YEAR = 2999;
    private static final int FIRST_MONTH = 1;
    private static final int LAST_MONTH = 12;

    public CommissionPeriod {
        firstDay = startOfMonth(firstDay);
    }

    public static CommissionPeriod of(int year, int month) {
        return new CommissionPeriod(firstDayOf(year, month));
    }

    public static CommissionPeriod covering(LocalDate day) {
        return new CommissionPeriod(day);
    }

    public LocalDate lastDay() {
        return YearMonth.from(firstDay).atEndOfMonth();
    }

    private static LocalDate firstDayOf(int year, int month) {
        if (year < EARLIEST_YEAR || year > LATEST_YEAR) {
            throw new BusinessException("year lies between %d and %d".formatted(EARLIEST_YEAR, LATEST_YEAR));
        }
        if (month < FIRST_MONTH || month > LAST_MONTH) {
            throw new BusinessException("month lies between %d and %d".formatted(FIRST_MONTH, LAST_MONTH));
        }
        return LocalDate.of(year, month, 1);
    }

    private static LocalDate startOfMonth(LocalDate day) {
        if (day == null) {
            throw new BusinessException("a settlement covers one month");
        }
        return day.withDayOfMonth(1);
    }

    @Override
    public String toString() {
        return YearMonth.from(firstDay).toString();
    }
}
