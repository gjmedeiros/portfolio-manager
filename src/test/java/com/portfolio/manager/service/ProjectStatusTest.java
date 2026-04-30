package com.portfolio.manager.service;

import com.portfolio.manager.enums.ProjectStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjectStatus - Testes de Transição e Deleção")
class ProjectStatusTest {

		@ParameterizedTest(name = "{0} → {1} deve ser {2}")
		@CsvSource({
				"EM_ANALISE,        ANALISE_REALIZADA, true",
				"ANALISE_REALIZADA, ANALISE_APROVADA,  true",
				"ANALISE_APROVADA,  INICIADO,          true",
				"INICIADO,          PLANEJADO,         true",
				"PLANEJADO,         EM_ANDAMENTO,      true",
				"EM_ANDAMENTO,      ENCERRADO,         true",
				"EM_ANALISE,        INICIADO,          false",
				"EM_ANALISE,        ENCERRADO,         false",
				"ANALISE_REALIZADA, EM_ANDAMENTO,      false",
				"ENCERRADO,         EM_ANDAMENTO,      false",
		})
		@DisplayName("Transições de status sequenciais")
		void shouldValidateStatusTransition(ProjectStatus from, ProjectStatus to, boolean expected) {
				assertThat(from.canTransitionTo(to)).isEqualTo(expected);
		}

		@Test
		@DisplayName("Cancelado pode ser aplicado a qualquer status ativo")
		void shouldAllowCancelFromActiveStatuses() {
				ProjectStatus[] active = {
						ProjectStatus.EM_ANALISE, ProjectStatus.ANALISE_REALIZADA,
						ProjectStatus.ANALISE_APROVADA, ProjectStatus.INICIADO,
						ProjectStatus.PLANEJADO, ProjectStatus.EM_ANDAMENTO
				};
				for (ProjectStatus s : active) {
						assertThat(s.canTransitionTo(ProjectStatus.CANCELADO))
								.as("Status %s deve permitir cancelamento", s).isTrue();
				}
		}

		@Test
		@DisplayName("Cancelado não pode ser aplicado a ENCERRADO ou CANCELADO")
		void shouldNotAllowCancelFromTerminalStatuses() {
				assertThat(ProjectStatus.ENCERRADO.canTransitionTo(ProjectStatus.CANCELADO)).isFalse();
				assertThat(ProjectStatus.CANCELADO.canTransitionTo(ProjectStatus.CANCELADO)).isFalse();
		}

		@ParameterizedTest(name = "{0} isDeletable deve ser {1}")
		@CsvSource({
				"EM_ANALISE,        true",
				"ANALISE_REALIZADA, true",
				"ANALISE_APROVADA,  true",
				"PLANEJADO,         true",
				"CANCELADO,         true",
				"INICIADO,          false",
				"EM_ANDAMENTO,      false",
				"ENCERRADO,         false",
		})
		@DisplayName("Regra de deleção por status")
		void shouldValidateDeletableStatus(ProjectStatus status, boolean expected) {
				assertThat(status.isDeletable()).isEqualTo(expected);
		}
}
