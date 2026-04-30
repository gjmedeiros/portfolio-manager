package com.portfolio.manager.controller;

import com.portfolio.manager.dto.request.ProjectCreateRequest;
import com.portfolio.manager.dto.request.ProjectFilterRequest;
import com.portfolio.manager.dto.request.ProjectStatusUpdateRequest;
import com.portfolio.manager.dto.request.ProjectUpdateRequest;
import com.portfolio.manager.dto.response.PortfolioReportResponse;
import com.portfolio.manager.dto.response.ProjectResponse;
import com.portfolio.manager.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "Gerenciamento completo do ciclo de vida dos projetos")
@SecurityRequirement(name = "basicAuth")
public class ProjectController {

		private final ProjectService projectService;

		// ── CRUD ─────────────────────────────────────────────────────────────────────

		@PostMapping
		@Operation(summary = "Criar projeto", description = "Cria um novo projeto com status inicial 'em análise'")
		@ApiResponses({
				@ApiResponse(responseCode = "201", description = "Projeto criado com sucesso"),
				@ApiResponse(responseCode = "400", description = "Dados inválidos"),
				@ApiResponse(responseCode = "422", description = "Violação de regra de negócio")
		})
		public ResponseEntity<ProjectResponse> createProject(
				@Valid @RequestBody ProjectCreateRequest request) {
				return ResponseEntity.status(HttpStatus.CREATED)
						.body(projectService.createProject(request));
		}

		@GetMapping("/{id}")
		@Operation(summary = "Buscar projeto por ID")
		@ApiResponses({
				@ApiResponse(responseCode = "200", description = "Projeto encontrado"),
				@ApiResponse(responseCode = "404", description = "Projeto não encontrado")
		})
		public ResponseEntity<ProjectResponse> getProjectById(
				@Parameter(description = "ID do projeto") @PathVariable Long id) {
				return ResponseEntity.ok(projectService.getProjectById(id));
		}

		@GetMapping
		@Operation(
				summary = "Listar projetos",
				description = "Lista projetos com paginação e filtros opcionais por status, nome e datas"
		)
		@ApiResponse(responseCode = "200", description = "Lista paginada de projetos")
		public ResponseEntity<Page<ProjectResponse>> listProjects(
				@ParameterObject ProjectFilterRequest filter,
				@ParameterObject
				@PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
				return ResponseEntity.ok(projectService.listProjects(filter, pageable));
		}

		@PutMapping("/{id}")
		@Operation(summary = "Atualizar projeto", description = "Atualiza os dados cadastrais de um projeto")
		@ApiResponses({
				@ApiResponse(responseCode = "200", description = "Projeto atualizado"),
				@ApiResponse(responseCode = "400", description = "Dados inválidos"),
				@ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
				@ApiResponse(responseCode = "422", description = "Violação de regra de negócio")
		})
		public ResponseEntity<ProjectResponse> updateProject(
				@PathVariable Long id,
				@Valid @RequestBody ProjectUpdateRequest request) {
				return ResponseEntity.ok(projectService.updateProject(id, request));
		}

		@DeleteMapping("/{id}")
		@Operation(
				summary = "Excluir projeto",
				description = "Exclui um projeto. Não é permitido excluir projetos nos status: iniciado, em andamento ou encerrado"
		)
		@ApiResponses({
				@ApiResponse(responseCode = "204", description = "Projeto excluído com sucesso"),
				@ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
				@ApiResponse(responseCode = "422", description = "Projeto não pode ser excluído no status atual")
		})
		public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
				projectService.deleteProject(id);
				return ResponseEntity.noContent().build();
		}

		// ── Status ───────────────────────────────────────────────────────────────────

		@PatchMapping("/{id}/status")
		@Operation(
				summary = "Atualizar status do projeto",
				description = "Avança o status do projeto seguindo a sequência lógica obrigatória. " +
						"Sequência: em análise → análise realizada → análise aprovada → iniciado → " +
						"planejado → em andamento → encerrado. Cancelado pode ser aplicado a qualquer momento."
		)
		@ApiResponses({
				@ApiResponse(responseCode = "200", description = "Status atualizado"),
				@ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
				@ApiResponse(responseCode = "422", description = "Transição de status inválida")
		})
		public ResponseEntity<ProjectResponse> updateProjectStatus(
				@PathVariable Long id,
				@Valid @RequestBody ProjectStatusUpdateRequest request) {
				return ResponseEntity.ok(projectService.updateProjectStatus(id, request));
		}

		// ── Members ──────────────────────────────────────────────────────────────────

		@PostMapping("/{projectId}/members/{memberId}")
		@Operation(
				summary = "Adicionar membro ao projeto",
				description = "Associa um membro ao projeto. Apenas membros com atribuição 'funcionário' " +
						"podem ser adicionados. Limite: mínimo 1, máximo 10 membros por projeto. " +
						"Um membro não pode estar em mais de 3 projetos ativos simultaneamente."
		)
		@ApiResponses({
				@ApiResponse(responseCode = "200", description = "Membro adicionado"),
				@ApiResponse(responseCode = "404", description = "Projeto ou membro não encontrado"),
				@ApiResponse(responseCode = "422", description = "Regra de negócio violada")
		})
		public ResponseEntity<ProjectResponse> addMember(
				@Parameter(description = "ID do projeto") @PathVariable Long projectId,
				@Parameter(description = "ID do membro") @PathVariable Long memberId) {
				return ResponseEntity.ok(projectService.addMemberToProject(projectId, memberId));
		}

		@DeleteMapping("/{projectId}/members/{memberId}")
		@Operation(
				summary = "Remover membro do projeto",
				description = "Remove a alocação de um membro do projeto. O projeto deve ter ao menos 1 membro."
		)
		@ApiResponses({
				@ApiResponse(responseCode = "200", description = "Membro removido"),
				@ApiResponse(responseCode = "404", description = "Projeto ou membro não encontrado"),
				@ApiResponse(responseCode = "422", description = "Projeto ficaria sem membros")
		})
		public ResponseEntity<ProjectResponse> removeMember(
				@PathVariable Long projectId,
				@PathVariable Long memberId) {
				return ResponseEntity.ok(projectService.removeMemberFromProject(projectId, memberId));
		}

		// ── Report ───────────────────────────────────────────────────────────────────

		@GetMapping("/report/portfolio")
		@Operation(
				summary = "Relatório do portfólio",
				description = "Gera um relatório resumido contendo: quantidade de projetos por status, " +
						"total orçado por status, média de duração dos projetos encerrados e " +
						"total de membros únicos alocados."
		)
		@ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
		public ResponseEntity<PortfolioReportResponse> getPortfolioReport() {
				return ResponseEntity.ok(projectService.generatePortfolioReport());
		}
}
