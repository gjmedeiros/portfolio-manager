package com.portfolio.manager.service;

import com.portfolio.manager.client.MemberApiClient;
import com.portfolio.manager.dto.request.ProjectCreateRequest;
import com.portfolio.manager.dto.request.ProjectStatusUpdateRequest;
import com.portfolio.manager.dto.response.MemberResponse;
import com.portfolio.manager.dto.response.PortfolioReportResponse;
import com.portfolio.manager.dto.response.ProjectResponse;
import com.portfolio.manager.entity.Project;
import com.portfolio.manager.enums.MemberRole;
import com.portfolio.manager.enums.ProjectStatus;
import com.portfolio.manager.enums.RiskClassification;
import com.portfolio.manager.exception.BusinessException;
import com.portfolio.manager.exception.InvalidStatusTransitionException;
import com.portfolio.manager.exception.ResourceNotFoundException;
import com.portfolio.manager.mapper.ProjectMapper;
import com.portfolio.manager.repository.ProjectRepository;
import com.portfolio.manager.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectServiceImpl - Testes de Regras de Negócio")
class ProjectServiceImplTest {

		@Mock private ProjectRepository projectRepository;
		@Mock private ProjectMapper projectMapper;
		@Mock private MemberApiClient memberApiClient;

		@InjectMocks
		private ProjectServiceImpl projectService;

		private Project baseProject;
		private MemberResponse funcionario;
		private MemberResponse gerente;

		@BeforeEach
		void setUp() {
				baseProject = Project.builder()
						.id(1L)
						.name("Projeto Teste")
						.startDate(LocalDate.of(2024, 1, 1))
						.expectedEndDate(LocalDate.of(2024, 4, 30))
						.totalBudget(new BigDecimal("80000"))
						.status(ProjectStatus.EM_ANALISE)
						.managerId(10L)
						.memberIds(new HashSet<>())
						.build();

				funcionario = MemberResponse.builder()
						.id(2L).name("Ana Funcionária").role(MemberRole.FUNCIONARIO).build();

				gerente = MemberResponse.builder()
						.id(10L).name("Carlos Gerente").role(MemberRole.GERENTE).build();
		}

		// ── CREATE ────────────────────────────────────────────────────────────────────

		@Nested
		@DisplayName("Criação de Projeto")
		class CreateProject {

				@Test
				@DisplayName("Deve criar projeto com sucesso")
				void shouldCreateProjectSuccessfully() {
						ProjectCreateRequest request = buildCreateRequest(
								LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 30), new BigDecimal("80000"), 10L
						);
						ProjectResponse expectedResponse = buildProjectResponse(ProjectStatus.EM_ANALISE);

						when(memberApiClient.findMemberById(10L)).thenReturn(Optional.of(gerente));
						when(projectMapper.toEntity(request)).thenReturn(baseProject);
						when(projectRepository.save(baseProject)).thenReturn(baseProject);
						when(projectMapper.toResponse(baseProject)).thenReturn(expectedResponse);
						when(memberApiClient.findMemberById(baseProject.getManagerId())).thenReturn(Optional.of(gerente));

						ProjectResponse result = projectService.createProject(request);

						assertThat(result).isNotNull();
						assertThat(result.getStatus()).isEqualTo(ProjectStatus.EM_ANALISE);
						verify(projectRepository).save(any(Project.class));
				}

				@Test
				@DisplayName("Deve lançar exceção quando gerente não existe")
				void shouldThrowWhenManagerNotFound() {
						ProjectCreateRequest request = buildCreateRequest(
								LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 30), new BigDecimal("80000"), 99L
						);
						when(memberApiClient.findMemberById(99L)).thenReturn(Optional.empty());

						assertThatThrownBy(() -> projectService.createProject(request))
								.isInstanceOf(ResourceNotFoundException.class)
								.hasMessageContaining("99");
				}

				@Test
				@DisplayName("Deve lançar exceção quando data de término é anterior à data de início")
				void shouldThrowWhenEndDateBeforeStartDate() {
						ProjectCreateRequest request = buildCreateRequest(
								LocalDate.of(2024, 6, 1), LocalDate.of(2024, 3, 1), new BigDecimal("80000"), 10L
						);
						when(memberApiClient.findMemberById(10L)).thenReturn(Optional.of(gerente));

						assertThatThrownBy(() -> projectService.createProject(request))
								.isInstanceOf(BusinessException.class)
								.hasMessageContaining("posterior");
				}

				@Test
				@DisplayName("Deve lançar exceção quando data de término é igual à data de início")
				void shouldThrowWhenEndDateEqualsStartDate() {
						LocalDate same = LocalDate.of(2024, 1, 1);
						ProjectCreateRequest request = buildCreateRequest(same, same, new BigDecimal("80000"), 10L);
						when(memberApiClient.findMemberById(10L)).thenReturn(Optional.of(gerente));

						assertThatThrownBy(() -> projectService.createProject(request))
								.isInstanceOf(BusinessException.class);
				}
		}

		// ── STATUS TRANSITION ─────────────────────────────────────────────────────────

		@Nested
		@DisplayName("Transição de Status")
		class StatusTransition {

				@Test
				@DisplayName("Deve avançar status na sequência correta: EM_ANALISE → ANALISE_REALIZADA")
				void shouldAdvanceStatusInOrder() {
						ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.ANALISE_REALIZADA);
						ProjectResponse expectedResponse = buildProjectResponse(ProjectStatus.ANALISE_REALIZADA);

						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));
						when(projectRepository.save(baseProject)).thenReturn(baseProject);
						when(projectMapper.toResponse(baseProject)).thenReturn(expectedResponse);
						when(memberApiClient.findMemberById(anyLong())).thenReturn(Optional.of(gerente));

						ProjectResponse result = projectService.updateProjectStatus(1L, request);

						assertThat(result.getStatus()).isEqualTo(ProjectStatus.ANALISE_REALIZADA);
				}

				@Test
				@DisplayName("Deve lançar exceção ao pular etapas (EM_ANALISE → INICIADO)")
				void shouldThrowWhenSkippingStatus() {
						ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.INICIADO);
						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));

						assertThatThrownBy(() -> projectService.updateProjectStatus(1L, request))
								.isInstanceOf(InvalidStatusTransitionException.class)
								.hasMessageContaining("inválida");
				}

				@Test
				@DisplayName("Deve aplicar CANCELADO a partir de qualquer status (exceto ENCERRADO/CANCELADO)")
				void shouldAllowCancelFromAnyStatus() {
						List<ProjectStatus> statusesAllowingCancel = List.of(
								ProjectStatus.EM_ANALISE, ProjectStatus.ANALISE_REALIZADA,
								ProjectStatus.ANALISE_APROVADA, ProjectStatus.INICIADO,
								ProjectStatus.PLANEJADO, ProjectStatus.EM_ANDAMENTO
						);

						for (ProjectStatus from : statusesAllowingCancel) {
								Project project = buildProjectWithStatus(from);
								ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.CANCELADO);
								ProjectResponse resp = buildProjectResponse(ProjectStatus.CANCELADO);

								when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
								when(projectRepository.save(project)).thenReturn(project);
								when(projectMapper.toResponse(project)).thenReturn(resp);
								when(memberApiClient.findMemberById(anyLong())).thenReturn(Optional.of(gerente));

								assertThatNoException()
										.isThrownBy(() -> projectService.updateProjectStatus(1L, request));
						}
				}

				@Test
				@DisplayName("Não deve permitir cancelar projeto já ENCERRADO")
				void shouldNotAllowCancelWhenEncerrado() {
						Project encerrado = buildProjectWithStatus(ProjectStatus.ENCERRADO);
						when(projectRepository.findById(1L)).thenReturn(Optional.of(encerrado));

						assertThatThrownBy(() -> projectService.updateProjectStatus(
								1L, new ProjectStatusUpdateRequest(ProjectStatus.CANCELADO)))
								.isInstanceOf(InvalidStatusTransitionException.class);
				}

				@Test
				@DisplayName("Deve percorrer toda a sequência de status sem erros")
				void shouldTraverseFullStatusSequence() {
						ProjectStatus[] sequence = {
								ProjectStatus.ANALISE_REALIZADA,
								ProjectStatus.ANALISE_APROVADA,
								ProjectStatus.INICIADO,
								ProjectStatus.PLANEJADO,
								ProjectStatus.EM_ANDAMENTO,
								ProjectStatus.ENCERRADO
						};

						for (ProjectStatus next : sequence) {
								assertThat(baseProject.getStatus().canTransitionTo(next))
										.as("Deve permitir transição de %s para %s", baseProject.getStatus(), next)
										.isTrue();

								baseProject.setStatus(next);
						}
				}
		}

		// ── DELETE ────────────────────────────────────────────────────────────────────

		@Nested
		@DisplayName("Exclusão de Projeto")
		class DeleteProject {

				@ParameterizedTest
				@EnumSource(value = ProjectStatus.class, names = { "INICIADO", "EM_ANDAMENTO", "ENCERRADO" })
				@DisplayName("Deve lançar exceção ao tentar excluir projeto em status não permitido")
				void shouldThrowWhenDeletingNonDeletableStatus(ProjectStatus status) {
						Project project = buildProjectWithStatus(status);
						when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

						assertThatThrownBy(() -> projectService.deleteProject(1L))
								.isInstanceOf(BusinessException.class)
								.hasMessageContaining("não pode ser excluído");
				}

				@ParameterizedTest
				@EnumSource(value = ProjectStatus.class, names = { "EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA", "PLANEJADO",
						"CANCELADO" })
				@DisplayName("Deve excluir projeto com sucesso nos status permitidos")
				void shouldDeleteWhenStatusIsDeletable(ProjectStatus status) {
						Project project = buildProjectWithStatus(status);
						when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
						doNothing().when(projectRepository).delete(project);

						assertThatNoException().isThrownBy(() -> projectService.deleteProject(1L));
						verify(projectRepository).delete(project);
				}

				@Test
				@DisplayName("Deve lançar exceção quando projeto não existe")
				void shouldThrowWhenProjectNotFound() {
						when(projectRepository.findById(99L)).thenReturn(Optional.empty());

						assertThatThrownBy(() -> projectService.deleteProject(99L))
								.isInstanceOf(ResourceNotFoundException.class);
				}
		}

		// ── MEMBER ALLOCATION ─────────────────────────────────────────────────────────

		@Nested
		@DisplayName("Alocação de Membros")
		class MemberAllocation {

				@Test
				@DisplayName("Deve adicionar membro funcionário com sucesso")
				void shouldAddFuncionarioMember() {
						ProjectResponse expectedResponse = buildProjectResponse(ProjectStatus.EM_ANALISE);

						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));
						when(memberApiClient.findMemberById(2L)).thenReturn(Optional.of(funcionario));
						when(projectRepository.countActiveProjectsByMemberId(eq(2L), anySet())).thenReturn(0L);
						when(projectRepository.save(baseProject)).thenReturn(baseProject);
						when(projectMapper.toResponse(baseProject)).thenReturn(expectedResponse);
						when(memberApiClient.findMemberById(baseProject.getManagerId())).thenReturn(Optional.of(gerente));

						ProjectResponse result = projectService.addMemberToProject(1L, 2L);

						assertThat(result).isNotNull();
						assertThat(baseProject.getMemberIds()).contains(2L);
				}

				@Test
				@DisplayName("Deve lançar exceção ao adicionar gerente como membro")
				void shouldThrowWhenAddingGerenteAsMember() {
						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));
						when(memberApiClient.findMemberById(10L)).thenReturn(Optional.of(gerente));

						assertThatThrownBy(() -> projectService.addMemberToProject(1L, 10L))
								.isInstanceOf(BusinessException.class)
								.hasMessageContaining("funcionário");
				}

				@Test
				@DisplayName("Deve lançar exceção ao adicionar membro já alocado no projeto")
				void shouldThrowWhenMemberAlreadyInProject() {
						baseProject.getMemberIds().add(2L);
						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));
						when(memberApiClient.findMemberById(2L)).thenReturn(Optional.of(funcionario));

						assertThatThrownBy(() -> projectService.addMemberToProject(1L, 2L))
								.isInstanceOf(BusinessException.class)
								.hasMessageContaining("já está alocado");

						verify(projectRepository, never()).save(any());
				}

				@Test
				@DisplayName("Deve lançar exceção quando projeto atingiu máximo de 10 membros")
				void shouldThrowWhenProjectAtMaxMembers() {
						for (long i = 1; i <= 10; i++) baseProject.getMemberIds().add(i);
						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));
						when(memberApiClient.findMemberById(99L)).thenReturn(Optional.of(
								MemberResponse.builder().id(99L).name("Novo").role(MemberRole.FUNCIONARIO).build()
						));

						assertThatThrownBy(() -> projectService.addMemberToProject(1L, 99L))
								.isInstanceOf(BusinessException.class)
								.hasMessageContaining("limite máximo");

						verify(projectRepository, never()).save(any());
				}

				@Test
				@DisplayName("Deve lançar exceção quando membro já está em 3 projetos ativos")
				void shouldThrowWhenMemberAtMaxActiveProjects() {
						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));
						when(memberApiClient.findMemberById(2L)).thenReturn(Optional.of(funcionario));
						when(projectRepository.countActiveProjectsByMemberId(eq(2L), anySet())).thenReturn(3L);

						assertThatThrownBy(() -> projectService.addMemberToProject(1L, 2L))
								.isInstanceOf(BusinessException.class)
								.hasMessageContaining("3 projetos ativos");
				}

				@Test
				@DisplayName("Deve remover membro com sucesso quando há mais de 1 membro")
				void shouldRemoveMemberSuccessfully() {
						baseProject.getMemberIds().add(2L);
						baseProject.getMemberIds().add(3L);
						ProjectResponse expectedResponse = buildProjectResponse(ProjectStatus.EM_ANALISE);

						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));
						when(projectRepository.save(baseProject)).thenReturn(baseProject);
						when(projectMapper.toResponse(baseProject)).thenReturn(expectedResponse);
						when(memberApiClient.findMemberById(baseProject.getManagerId())).thenReturn(Optional.of(gerente));

						ProjectResponse result = projectService.removeMemberFromProject(1L, 2L);

						assertThat(result).isNotNull();
						assertThat(baseProject.getMemberIds()).doesNotContain(2L);
				}

				@Test
				@DisplayName("Deve lançar exceção ao remover único membro do projeto")
				void shouldThrowWhenRemovingLastMember() {
						baseProject.getMemberIds().add(2L);
						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));

						assertThatThrownBy(() -> projectService.removeMemberFromProject(1L, 2L))
								.isInstanceOf(BusinessException.class)
								.hasMessageContaining("mínimo");
				}

				@Test
				@DisplayName("Deve lançar exceção ao remover membro não alocado")
				void shouldThrowWhenRemovingNonAllocatedMember() {
						when(projectRepository.findById(1L)).thenReturn(Optional.of(baseProject));

						assertThatThrownBy(() -> projectService.removeMemberFromProject(1L, 99L))
								.isInstanceOf(BusinessException.class)
								.hasMessageContaining("não está alocado");
				}
		}

		// ── RISK CLASSIFICATION ───────────────────────────────────────────────────────

		@Nested
		@DisplayName("Classificação de Risco")
		class RiskClassificationTests {

				@Test
				@DisplayName("Deve classificar como BAIXO: orçamento ≤ 100k e prazo ≤ 3 meses")
				void shouldClassifyAsLowRisk() {
						Project project = buildProjectForRisk(
								new BigDecimal("80000"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31)
						);
						assertThat(project.getRiskClassification()).isEqualTo(RiskClassification.BAIXO);
				}

				@Test
				@DisplayName("Deve classificar como MÉDIO: orçamento entre 100k e 500k")
				void shouldClassifyAsMediumRiskByBudget() {
						Project project = buildProjectForRisk(
								new BigDecimal("250000"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 15)
						);
						assertThat(project.getRiskClassification()).isEqualTo(RiskClassification.MEDIO);
				}

				@Test
				@DisplayName("Deve classificar como MÉDIO: prazo entre 3 e 6 meses")
				void shouldClassifyAsMediumRiskByDuration() {
						Project project = buildProjectForRisk(
								new BigDecimal("50000"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 5, 1)
						);
						assertThat(project.getRiskClassification()).isEqualTo(RiskClassification.MEDIO);
				}

				@Test
				@DisplayName("Deve classificar como ALTO: orçamento > 500k")
				void shouldClassifyAsHighRiskByBudget() {
						Project project = buildProjectForRisk(
								new BigDecimal("600000"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1)
						);
						assertThat(project.getRiskClassification()).isEqualTo(RiskClassification.ALTO);
				}

				@Test
				@DisplayName("Deve classificar como ALTO: prazo > 6 meses")
				void shouldClassifyAsHighRiskByDuration() {
						Project project = buildProjectForRisk(
								new BigDecimal("50000"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 8, 1)
						);
						assertThat(project.getRiskClassification()).isEqualTo(RiskClassification.ALTO);
				}

				@Test
				@DisplayName("Deve classificar como ALTO: orçamento exatamente 500k com prazo > 6 meses")
				void shouldClassifyAsHighRiskWhenBothHigh() {
						Project project = buildProjectForRisk(
								new BigDecimal("500001"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 10, 1)
						);
						assertThat(project.getRiskClassification()).isEqualTo(RiskClassification.ALTO);
				}
		}

		// ── PORTFOLIO REPORT ──────────────────────────────────────────────────────────

		@Nested
		@DisplayName("Relatório de Portfólio")
		class PortfolioReport {

				@Test
				@DisplayName("Deve gerar relatório com dados agregados corretamente")
				void shouldGenerateReportSuccessfully() {
						List<Object[]> summary = List.of(
								new Object[] { ProjectStatus.EM_ANALISE, 2L, new BigDecimal("160000") },
								new Object[] { ProjectStatus.EM_ANDAMENTO, 1L, new BigDecimal("300000") }
						);

						when(projectRepository.findStatusSummary()).thenReturn(summary);
						when(projectRepository.findAverageDurationOfClosedProjects()).thenReturn(90.0);
						when(projectRepository.findUniqueMemberIdsByStatusIn(anyList()))
								.thenReturn(List.of(1L, 2L, 3L));

						PortfolioReportResponse report = projectService.generatePortfolioReport();

						assertThat(report).isNotNull();
						assertThat(report.getProjectCountByStatus()).hasSize(2);
						assertThat(report.getTotalBudgetByStatus()).hasSize(2);
						assertThat(report.getAverageDurationOfClosedProjects()).isEqualTo(90.0);
						assertThat(report.getTotalUniqueMembers()).isEqualTo(3);
				}

				@Test
				@DisplayName("Deve retornar 0.0 quando não há projetos encerrados")
				void shouldReturnZeroAvgWhenNoClosedProjects() {
						when(projectRepository.findStatusSummary()).thenReturn(List.of());
						when(projectRepository.findAverageDurationOfClosedProjects()).thenReturn(null);
						when(projectRepository.findUniqueMemberIdsByStatusIn(anyList())).thenReturn(List.of());

						PortfolioReportResponse report = projectService.generatePortfolioReport();

						assertThat(report.getAverageDurationOfClosedProjects()).isEqualTo(0.0);
						assertThat(report.getTotalUniqueMembers()).isZero();
				}
		}

		// ── helpers ───────────────────────────────────────────────────────────────────

		private ProjectCreateRequest buildCreateRequest(
				LocalDate start, LocalDate end, BigDecimal budget, Long managerId) {
				return ProjectCreateRequest.builder()
						.name("Projeto Teste").startDate(start).expectedEndDate(end)
						.totalBudget(budget).managerId(managerId).build();
		}

		private ProjectResponse buildProjectResponse(ProjectStatus status) {
				return ProjectResponse.builder()
						.id(1L).name("Projeto Teste").status(status)
						.totalBudget(new BigDecimal("80000"))
						.startDate(LocalDate.of(2024, 1, 1))
						.expectedEndDate(LocalDate.of(2024, 4, 30))
						.memberIds(new HashSet<>()).memberCount(0).build();
		}

		private Project buildProjectWithStatus(ProjectStatus status) {
				return Project.builder()
						.id(1L).name("Projeto").status(status)
						.startDate(LocalDate.of(2024, 1, 1))
						.expectedEndDate(LocalDate.of(2024, 4, 30))
						.totalBudget(new BigDecimal("80000"))
						.managerId(10L).memberIds(new HashSet<>()).build();
		}

		private Project buildProjectForRisk(BigDecimal budget, LocalDate start, LocalDate end) {
				return Project.builder()
						.totalBudget(budget).startDate(start).expectedEndDate(end)
						.status(ProjectStatus.EM_ANALISE).memberIds(new HashSet<>()).build();
		}
}
