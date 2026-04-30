package com.portfolio.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.manager.enums.ProjectStatus;
import com.portfolio.manager.enums.RiskClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de um projeto")
public class ProjectResponse {

    @Schema(description = "ID do projeto")
    private Long id;

    @JsonProperty("nome")
    @Schema(description = "Nome do projeto")
    private String name;

    @JsonProperty("dataInicio")
    @Schema(description = "Data de início")
    private LocalDate startDate;

    @JsonProperty("dataTerminoEsperada")
    @Schema(description = "Previsão de término")
    private LocalDate expectedEndDate;

    @JsonProperty("dataTerminoReal")
    @Schema(description = "Data real de término")
    private LocalDate actualEndDate;

    @JsonProperty("orçamentoTotal")
    @Schema(description = "Orçamento total")
    private BigDecimal totalBudget;

    @JsonProperty("descricao")
    @Schema(description = "Descrição")
    private String description;

    @JsonProperty("idGerente")
    @Schema(description = "ID do gerente responsável")
    private Long managerId;

    @JsonProperty("nomeGerente")
    @Schema(description = "Nome do gerente responsável")
    private String managerName;

    @JsonProperty("status")
    @Schema(description = "Status atual")
    private ProjectStatus status;

    @JsonProperty("classificacaoDeRisco")
    @Schema(description = "Classificação de risco calculada")
    private RiskClassification riskClassification;

    @JsonProperty("idMembro")
    @Schema(description = "IDs dos membros alocados")
    private Set<Long> memberIds;

    @JsonProperty("quantidadeMembros")
    @Schema(description = "Quantidade de membros alocados")
    private int memberCount;
}
