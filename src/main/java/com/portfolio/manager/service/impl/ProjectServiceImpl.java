package com.portfolio.manager.service.impl;

import com.portfolio.manager.client.MemberApiClient;
import com.portfolio.manager.dto.request.ProjectCreateRequest;
import com.portfolio.manager.dto.request.ProjectFilterRequest;
import com.portfolio.manager.dto.request.ProjectStatusUpdateRequest;
import com.portfolio.manager.dto.request.ProjectUpdateRequest;
import com.portfolio.manager.dto.response.MemberResponse;
import com.portfolio.manager.dto.response.PortfolioReportResponse;
import com.portfolio.manager.dto.response.ProjectResponse;
import com.portfolio.manager.entity.Project;
import com.portfolio.manager.enums.MemberRole;
import com.portfolio.manager.enums.ProjectStatus;
import com.portfolio.manager.exception.BusinessException;
import com.portfolio.manager.exception.InvalidRequestException;
import com.portfolio.manager.exception.InvalidStatusTransitionException;
import com.portfolio.manager.exception.ResourceNotFoundException;
import com.portfolio.manager.mapper.ProjectMapper;
import com.portfolio.manager.repository.ProjectRepository;
import com.portfolio.manager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

		private static final int MAX_MEMBERS_PER_PROJECT = 10;
		private static final int MIN_MEMBERS_PER_PROJECT = 1;
		private static final int MAX_ACTIVE_PROJECTS_PER_MEMBER = 3;

		private static final Set<ProjectStatus> INACTIVE_STATUSES = Set.of(
				ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO
		);

		private final ProjectRepository projectRepository;
		private final ProjectMapper projectMapper;
		private final MemberApiClient memberApiClient;

		@Override
		public ProjectResponse createProject(ProjectCreateRequest request) {
				log.info("Criando projeto: {}", request.getName());

				validateManagerExists(request.getManagerId());
				validateDates(request.getStartDate(), request.getExpectedEndDate());

				Project project = projectMapper.toEntity(request);
				project = projectRepository.save(project);

				log.info("Projeto criado com o ID: {}", project.getId());
				return buildProjectResponse(project);
		}

		@Override
		@Transactional(readOnly = true)
		public ProjectResponse getProjectById(Long id) {
				Project project = findProjectOrThrow(id);
				return buildProjectResponse(project);
		}

		@Override
		@Transactional(readOnly = true)
		public Page<ProjectResponse> listProjects(ProjectFilterRequest filter, Pageable pageable) {
				log.debug("Listar projetos com filtros: {}", filter);

				return projectRepository.findWithFilters(
						filter.getStatus(),
						toLikePattern(filter.getName()),
						filter.getStartDateFrom(),
						filter.getStartDateTo(),
						pageable
				).map(this::buildProjectResponse);
		}

		@Override
		public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
				log.info("Atualizando o ID do projeto: {}", id);

				Project project = findProjectOrThrow(id);
				validateManagerExists(request.getManagerId());
				validateDates(request.getStartDate(), request.getExpectedEndDate());

				projectMapper.updateFromRequest(request, project);
				project = projectRepository.save(project);

				log.info("ID do projeto {} atualizado com sucesso", id);
				return buildProjectResponse(project);
		}

		@Override
		public ProjectResponse updateProjectStatus(Long id, ProjectStatusUpdateRequest request) {
				log.info("Atualizando o status do projeto com ID {} para {}", id, request.getNewStatus());

				Project project = findProjectOrThrow(id);
				ProjectStatus currentStatus = project.getStatus();
				ProjectStatus newStatus = request.getNewStatus();

				if (!currentStatus.canTransitionTo(newStatus)) {
						throw new InvalidStatusTransitionException(currentStatus, newStatus);
				}

				project.setStatus(newStatus);
				project = projectRepository.save(project);

				log.info("O status do projeto com ID {} foi alterado de {} para {}", id, currentStatus, newStatus);
				return buildProjectResponse(project);
		}

		@Override
		public void deleteProject(Long id) {
				log.info("Tentativa de excluir o projeto com o ID: {}", id);

				Project project = findProjectOrThrow(id);

				if (!project.getStatus().isDeletable()) {

						throw new BusinessException(String.format(
								"Projeto com status '%s' não pode ser excluído. " +
										"Apenas projetos nos status: em análise, análise realizada, análise aprovada, planejado, cancelado podem ser excluídos",
								project.getStatus().getDisplayName()));
				}

				projectRepository.delete(project);
				log.info("ID do projeto {} excluído com sucesso", id);
		}

		@Override
		public ProjectResponse addMemberToProject(Long projectId, Long memberId) {
				log.info("Adicionando o ID do membro {} ao ID do projeto {}", memberId, projectId);

				Project project = findProjectOrThrow(projectId);
				MemberResponse member = findMemberOrThrow(memberId);

				validateProjectAcceptsMembers(project);
				validateMemberNotAlreadyInProject(project, memberId);
				validateProjectMemberLimit(project);
				validateMemberIsFuncionario(member);
				validateMemberActiveProjectLimit(member, memberId);

				project.getMemberIds().add(memberId);
				project = projectRepository.save(project);

				log.info("Membro com ID {} adicionado ao projeto com ID {}", memberId, projectId);
				return buildProjectResponse(project);
		}

		@Override
		public ProjectResponse removeMemberFromProject(Long projectId, Long memberId) {
				log.info("Removendo o ID do membro {} do ID do projeto {}", memberId, projectId);

				Project project = findProjectOrThrow(projectId);

				if (!project.getMemberIds().contains(memberId)) {
						throw new BusinessException(String.format("Membro com ID %s não está alocado neste projeto.", memberId));
				}

				if (project.getMemberIds().size() <= MIN_MEMBERS_PER_PROJECT) {
						throw new BusinessException(
								String.format("Não é possível remover o membro. O projeto deve ter no mínimo %s membro(s) alocado(s).",
										MIN_MEMBERS_PER_PROJECT));
				}

				project.getMemberIds().remove(memberId);
				project = projectRepository.save(project);

				log.info("ID do membro {} removido do projeto ID {}", memberId, projectId);
				return buildProjectResponse(project);
		}

		@Override
		@Transactional(readOnly = true)
		public PortfolioReportResponse generatePortfolioReport() {
				log.info("Geração de relatório de portfólio");

				List<Object[]> statusSummary = projectRepository.findStatusSummary();

				Map<String, Long> countByStatus = new LinkedHashMap<>();
				Map<String, BigDecimal> budgetByStatus = new LinkedHashMap<>();

				for (Object[] row : statusSummary) {
						ProjectStatus status = (ProjectStatus) row[0];
						Long count = (Long) row[1];
						BigDecimal totalBudget = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
						countByStatus.put(status.getDisplayName(), count);
						budgetByStatus.put(status.getDisplayName(), totalBudget);
				}

				Double avgDuration = projectRepository.findAverageDurationOfClosedProjects();

				List<Long> uniqueMemberIds = projectRepository.findUniqueMemberIdsByStatusIn(
						List.of(ProjectStatus.values())
				);

				return PortfolioReportResponse.builder()
						.projectCountByStatus(countByStatus)
						.totalBudgetByStatus(budgetByStatus)
						.averageDurationOfClosedProjects(avgDuration != null ? avgDuration : 0.0)
						.totalUniqueMembers(uniqueMemberIds.size())
						.build();
		}

		private String toLikePattern(String value) {
				return (value != null && !value.isBlank()) ? "%" + value + "%" : null;
		}

		private Project findProjectOrThrow(Long id) {
				return projectRepository.findById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
		}

		private ProjectResponse buildProjectResponse(Project project) {
				ProjectResponse response = projectMapper.toResponse(project);

				memberApiClient.findMemberById(project.getManagerId())
						.ifPresent(m -> response.setManagerName(m.getName()));

				return response;
		}

		private MemberResponse findMemberOrThrow(Long memberId) {
				return memberApiClient.findMemberById(memberId)
						.orElseThrow(() -> new ResourceNotFoundException(
								String.format("Membro %s não encontrado.", memberId)));
		}

		private void validateManagerExists(Long managerId) {
				MemberResponse manager = memberApiClient.findMemberById(managerId)
						.orElseThrow(
								() -> new ResourceNotFoundException(String.format("Gerente (membro) com ID %s não encontrado.", managerId)));

				if (!manager.getRole().equals(MemberRole.GERENTE)) {
						throw new InvalidRequestException(String.format("Membro com ID %s não é um gerente", managerId));
				}
		}

		private void validateDates(java.time.LocalDate startDate, java.time.LocalDate expectedEndDate) {
				if (expectedEndDate.isBefore(startDate) || expectedEndDate.isEqual(startDate)) {
						throw new BusinessException(
								"A previsão de término deve ser posterior à data de início do projeto."
						);
				}
		}

		private void validateProjectAcceptsMembers(Project project) {
				if (INACTIVE_STATUSES.contains(project.getStatus())) {
						throw new BusinessException(String.format(
								"Não é possível alocar membros em projetos com o status '%s'",
								project.getStatus().getDisplayName()));
				}
		}

		private void validateMemberNotAlreadyInProject(Project project, Long memberId) {
				if (project.getMemberIds().contains(memberId)) {
						throw new BusinessException(
								String.format("Membro com ID %s já está alocado neste projeto", memberId));
				}
		}

		private void validateProjectMemberLimit(Project project) {
				if (project.getMemberIds().size() >= MAX_MEMBERS_PER_PROJECT) {
						throw new BusinessException(
								String.format("Projeto já atingiu o limite máximo de %s membros", MAX_MEMBERS_PER_PROJECT));
				}
		}

		private void validateMemberIsFuncionario(MemberResponse member) {
				if (member.getRole() != MemberRole.FUNCIONARIO) {
						throw new BusinessException(String.format(
								"Apenas membros com atribuição 'funcionário' podem ser alocados. " +
										"O membro '%s' possui atribuição: %s",
								member.getName(), member.getRole().getDisplayName()));
				}
		}

		private void validateMemberActiveProjectLimit(MemberResponse member, Long memberId) {
				long activeProjects = projectRepository.countActiveProjectsByMemberId(memberId, INACTIVE_STATUSES);
				if (activeProjects >= MAX_ACTIVE_PROJECTS_PER_MEMBER) {
						throw new BusinessException(String.format(
								"Membro '%s' já está alocado no máximo de %s projetos ativos simultaneamente.",
								member.getName(), MAX_ACTIVE_PROJECTS_PER_MEMBER));
				}
		}
}
