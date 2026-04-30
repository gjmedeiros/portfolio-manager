package com.portfolio.manager.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de um projeto")
public class ProjectCreateRequest {

		@JsonProperty("nome")
		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
		@Schema(description = "Nome do projeto", example = "Sistema ERP")
		private String name;

		@JsonProperty("dataInicio")
		@NotNull(message = "Data de início é obrigatória")
		@Schema(description = "Data de início", example = "2024-01-15")
		private LocalDate startDate;

		@JsonProperty("dataTerminoEsperada")
		@NotNull(message = "Previsão de término é obrigatória")
		@Schema(description = "Previsão de término", example = "2024-06-30")
		private LocalDate expectedEndDate;

		@JsonProperty("orçamentoTotal")
		@NotNull(message = "Orçamento total é obrigatório")
		@DecimalMin(value = "0.01", message = "Orçamento deve ser maior que zero")
		@Digits(integer = 13, fraction = 2, message = "Orçamento inválido")
		@Schema(description = "Orçamento total em R$", example = "250000.00")
		private BigDecimal totalBudget;

		@JsonProperty("descricao")
		@Schema(description = "Descrição do projeto")
		private String description;

		@JsonProperty("idGerente")
		@NotNull(message = "ID do gerente é obrigatório")
		@Schema(description = "ID do gerente responsável", example = "1")
		private Long managerId;
}
