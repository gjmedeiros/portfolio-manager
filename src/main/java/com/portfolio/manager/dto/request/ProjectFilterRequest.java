package com.portfolio.manager.dto.request;

import com.portfolio.manager.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros para listagem de projetos")
public class ProjectFilterRequest {

		@Schema(description = "Filtro por status", example = "EM_ANDAMENTO")
		private ProjectStatus status;

		@Schema(description = "Filtro por nome (parcial)", example = "Sistema")
		private String name;

		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@Schema(description = "Data de início (de)", example = "2024-01-01")
		private LocalDate startDateFrom;

		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@Schema(description = "Data de início (até)", example = "2024-12-31")
		private LocalDate startDateTo;
}
