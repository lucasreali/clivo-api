package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.exception.BusinessException;
import java.time.LocalDate;

public record ReportPeriod(LocalDate from, LocalDate to) {

    public ReportPeriod {
        requireOrdered(from, to);
    }

    public static ReportPeriod ofMonth(LocalDate anyDay) {
        return new ReportPeriod(anyDay.withDayOfMonth(1), anyDay.withDayOfMonth(anyDay.lengthOfMonth()));
    }

    private static void requireOrdered(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessException("a report covers a period with a start and an end");
        }
        if (from.isAfter(to)) {
            throw new BusinessException("a report period ends after it starts");
        }
    }
}
