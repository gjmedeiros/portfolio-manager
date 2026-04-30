package com.portfolio.manager.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.manager.enums.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de um membro")
public class MemberCreateRequest {

		@JsonProperty("nome")
		@NotBlank(message = "Nome é obrigatório")
		@Schema(description = "Nome do membro", example = "João Silva")
		private String name;

		@JsonProperty("atribuicao")
		@NotNull(message = "Atribuição é obrigatória")
		@Schema(description = "Atribuição do membro", example = "FUNCIONARIO")
		private MemberRole role;
}
