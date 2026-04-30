package com.portfolio.manager.enums;

import java.util.List;
import java.util.Map;

public enum ProjectStatus {
		EM_ANALISE("em análise"),
		ANALISE_REALIZADA("análise realizada"),
		ANALISE_APROVADA("análise aprovada"),
		INICIADO("iniciado"),
		PLANEJADO("planejado"),
		EM_ANDAMENTO("em andamento"),
		ENCERRADO("encerrado"),
		CANCELADO("cancelado");

		private final String displayName;

		private static final Map<ProjectStatus, ProjectStatus> NEXT_STATUS = Map.of(
				EM_ANALISE, ANALISE_REALIZADA,
				ANALISE_REALIZADA, ANALISE_APROVADA,
				ANALISE_APROVADA, INICIADO,
				INICIADO, PLANEJADO,
				PLANEJADO, EM_ANDAMENTO,
				EM_ANDAMENTO, ENCERRADO
		);

		public static final List<ProjectStatus> NON_DELETABLE = List.of(INICIADO, EM_ANDAMENTO, ENCERRADO);
		public static final List<ProjectStatus> ACTIVE_FOR_MEMBER_LIMIT = List.of(
				EM_ANALISE, ANALISE_REALIZADA, ANALISE_APROVADA, INICIADO, PLANEJADO, EM_ANDAMENTO
		);

		ProjectStatus(String displayName) {
				this.displayName = displayName;
		}

		public String getDisplayName() {
				return displayName;
		}

		public boolean canTransitionTo(ProjectStatus next) {
				if (next == CANCELADO) {
						return this != CANCELADO && this != ENCERRADO;
				}
				ProjectStatus expectedNext = NEXT_STATUS.get(this);
				return next.equals(expectedNext);
		}

		public boolean isDeletable() {
				return !NON_DELETABLE.contains(this);
		}
}
