package com.portfolio.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Relatório resumido do portfólio de projetos")
public class PortfolioReportResponse {

		@JsonProperty("projetosPorStatus")
		@Schema(description = "Quantidade de projetos por status")
		private Map<String, Long> projectCountByStatus;

		@JsonProperty("orçamentoTotalPorStatus")
		@Schema(description = "Total orçado por status")
		private Map<String, BigDecimal> totalBudgetByStatus;

		@JsonProperty("duracaoMediaProjetosConcluidos")
		@Schema(description = "Média de duração (em dias) dos projetos encerrados")
		private Double averageDurationOfClosedProjects;

		@JsonProperty("totalMembrosUnicos")
		@Schema(description = "Total de membros únicos alocados")
		private int totalUniqueMembers;
}
