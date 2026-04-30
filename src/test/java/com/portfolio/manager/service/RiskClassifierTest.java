package com.portfolio.manager.service;

import com.portfolio.manager.domain.RiskClassifier;
import com.portfolio.manager.enums.RiskClassification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RiskClassifier - Testes de Classificação de Risco")
class RiskClassifierTest {

		private static final LocalDate BASE = LocalDate.of(2024, 1, 1);

		@Test
		@DisplayName("Baixo: orçamento = 100k e prazo = 3 meses exatos")
		void lowRisk_exactBoundary() {
				assertThat(classify("100000", BASE, BASE.plusMonths(3))).isEqualTo(RiskClassification.BAIXO);
		}

		@Test
		@DisplayName("Baixo: orçamento < 100k e prazo < 3 meses")
		void lowRisk_belowBothThresholds() {
				assertThat(classify("50000", BASE, BASE.plusMonths(2))).isEqualTo(RiskClassification.BAIXO);
		}

		@Test
		@DisplayName("Médio: orçamento = 100.001 (acima do limite baixo)")
		void mediumRisk_budgetJustAboveLow() {
				assertThat(classify("100001", BASE, BASE.plusMonths(2))).isEqualTo(RiskClassification.MEDIO);
		}

		@Test
		@DisplayName("Médio: prazo = 4 meses (acima de 3, dentro de 6)")
		void mediumRisk_durationBetween3And6() {
				assertThat(classify("50000", BASE, BASE.plusMonths(4))).isEqualTo(RiskClassification.MEDIO);
		}

		@Test
		@DisplayName("Médio: orçamento = 500k exatos")
		void mediumRisk_exactUpperBudgetBoundary() {
				assertThat(classify("500000", BASE, BASE.plusMonths(2))).isEqualTo(RiskClassification.MEDIO);
		}

		@Test
		@DisplayName("Alto: orçamento = 500.001 (acima do limite médio)")
		void highRisk_budgetJustAboveMedium() {
				assertThat(classify("500001", BASE, BASE.plusMonths(2))).isEqualTo(RiskClassification.ALTO);
		}

		@Test
		@DisplayName("Alto: prazo = 7 meses (acima de 6)")
		void highRisk_durationAbove6Months() {
				assertThat(classify("50000", BASE, BASE.plusMonths(7))).isEqualTo(RiskClassification.ALTO);
		}

		@Test
		@DisplayName("Alto: ambos orçamento e prazo são altos")
		void highRisk_bothCriteria() {
				assertThat(classify("1000000", BASE, BASE.plusMonths(12))).isEqualTo(RiskClassification.ALTO);
		}

		@Test
		@DisplayName("Alto: quando parâmetros são nulos")
		void highRisk_nullParameters() {
				assertThat(RiskClassifier.classify(null, BASE, BASE.plusMonths(2))).isEqualTo(RiskClassification.ALTO);
				assertThat(RiskClassifier.classify(new BigDecimal("50000"), null, BASE.plusMonths(2))).isEqualTo(RiskClassification.ALTO);
				assertThat(RiskClassifier.classify(new BigDecimal("50000"), BASE, null)).isEqualTo(RiskClassification.ALTO);
		}

		@Test
		@DisplayName("Médio: prazo exatamente 6 meses")
		void mediumRisk_exactly6Months() {
				assertThat(classify("50000", BASE, BASE.plusMonths(6))).isEqualTo(RiskClassification.MEDIO);
		}

		private RiskClassification classify(String budget, LocalDate start, LocalDate end) {
				return RiskClassifier.classify(new BigDecimal(budget), start, end);
		}
}
