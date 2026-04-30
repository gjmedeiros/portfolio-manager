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
@Schema(description = "Dados para atualização de um projeto")
public class ProjectUpdateRequest {

		@JsonProperty("nome")
		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 200)
		@Schema(description = "Nome do projeto")
		private String name;

		@JsonProperty("dataInicio")
		@NotNull(message = "Data de início é obrigatória")
		@Schema(description = "Data de início")
		private LocalDate startDate;

		@JsonProperty("dataTerminoEsperada")
		@NotNull(message = "Previsão de término é obrigatória")
		@Schema(description = "Previsão de término")
		private LocalDate expectedEndDate;

		@JsonProperty("dataTerminoReal")
		@Schema(description = "Data real de término")
		private LocalDate actualEndDate;

		@JsonProperty("orçamentoTotal")
		@NotNull(message = "Orçamento total é obrigatório")
		@DecimalMin(value = "0.01")
		@Digits(integer = 13, fraction = 2)
		@Schema(description = "Orçamento total em R$")
		private BigDecimal totalBudget;

		@JsonProperty("descricao")
		@Schema(description = "Descrição do projeto")
		private String description;

		@JsonProperty("idGerente")
		@NotNull(message = "ID do gerente é obrigatório")
		@Schema(description = "ID do gerente responsável")
		private Long managerId;
}
