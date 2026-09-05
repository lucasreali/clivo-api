package com.example.clivoapi.modules.commission;

import com.example.clivoapi.common.exception.BusinessException;
import java.time.LocalDate;
import java.time.YearMonth;

public record CommissionPeriod(LocalDate firstDay) {

    public CommissionPeriod {
        firstDay = startOfMonth(firstDay);
    }

    public static CommissionPeriod of(int year, int month) {
        return new CommissionPeriod(LocalDate.of(year, month, 1));
    }

    public static CommissionPeriod covering(LocalDate day) {
        return new CommissionPeriod(day);
    }

    public LocalDate lastDay() {
        return YearMonth.from(firstDay).atEndOfMonth();
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
