package com.portfolio.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.manager.enums.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de um membro")
public class MemberResponse {

    @Schema(description = "ID do membro")
    private Long id;

    @JsonProperty("nome")
    @Schema(description = "Nome do membro")
    private String name;

    @JsonProperty("atribuicao")
    @Schema(description = "Atribuição do membro")
    private MemberRole role;
}
