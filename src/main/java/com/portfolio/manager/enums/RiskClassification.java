package com.portfolio.manager.enums;

public enum RiskClassification {
		BAIXO("Baixo"),
		MEDIO("Médio"),
		ALTO("Alto");

		private final String displayName;

		RiskClassification(String displayName) {
				this.displayName = displayName;
		}

		public String getDisplayName() {
				return displayName;
		}
}
