package com.portfolio.manager.domain;

import com.portfolio.manager.enums.RiskClassification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class RiskClassifier {

		private static final BigDecimal LOW_BUDGET_LIMIT = new BigDecimal("100000");
		private static final BigDecimal MEDIUM_BUDGET_LIMIT = new BigDecimal("500000");
		private static final long LOW_MONTH_LIMIT = 3;
		private static final long MEDIUM_MONTH_LIMIT = 6;

		private RiskClassifier() {
		}

		public static RiskClassification classify(BigDecimal budget, LocalDate startDate, LocalDate expectedEndDate) {
				if (budget == null || startDate == null || expectedEndDate == null) {
						return RiskClassification.ALTO;
				}

				long months = ChronoUnit.MONTHS.between(startDate, expectedEndDate);

				boolean highBudget = budget.compareTo(MEDIUM_BUDGET_LIMIT) > 0;
				boolean highMonths = months > MEDIUM_MONTH_LIMIT;

				if (highBudget || highMonths) {
						return RiskClassification.ALTO;
				}

				boolean mediumBudget = budget.compareTo(LOW_BUDGET_LIMIT) > 0;
				boolean mediumMonths = months > LOW_MONTH_LIMIT;

				if (mediumBudget || mediumMonths) {
						return RiskClassification.MEDIO;
				}

				return RiskClassification.BAIXO;
		}
}
