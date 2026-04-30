package com.portfolio.manager.enums;

public enum MemberRole {
		FUNCIONARIO("funcionário"),
		GERENTE("gerente");

		private final String displayName;

		MemberRole(String displayName) {
				this.displayName = displayName;
		}

		public String getDisplayName() {
				return displayName;
		}
}
