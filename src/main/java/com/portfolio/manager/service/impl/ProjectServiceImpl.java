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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

		private static final int MAX_MEMBERS_PER_PROJECT = 10;
		private static final int MIN_MEMBERS_PER_PROJECT = 1;
		private static final int MAX_ACTIVE_PROJECTS_PER_MEMBER = 3;

		private static final List<ProjectStatus> EXCLUDED_FROM_MEMBER_LIMIT =
				List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO);

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

				String name = filter.getName();

				if (name != null && !name.isBlank()) {
						name = "%" + name + "%";
				} else {
						name = null;
				}

				return projectRepository.findWithFilters(
						filter.getStatus(),
						name,
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
				log.info("Atualizando o status do projeto com ID {} \u200B\u200Bpara {}", id, request.getNewStatus());

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
						throw new BusinessException(
								"Projeto com status '" + project.getStatus().getDisplayName() +
										"' não pode ser excluído. Apenas projetos nos status: " +
										"em análise, análise realizada, análise aprovada, planejado, cancelado podem ser excluídos."
						);
				}

				projectRepository.delete(project);
				log.info("ID do projeto {} excluído com sucesso", id);
		}

		@Override
		public ProjectResponse addMemberToProject(Long projectId, Long memberId) {
				log.info("Adicionando o ID do membro {} ao ID do projeto {}", memberId, projectId);

				Project project = findProjectOrThrow(projectId);

				if (project.getMemberIds().contains(memberId)) {
						throw new BusinessException("Membro com ID " + memberId + " já está alocado neste projeto.");
				}

				if (project.getMemberIds().size() >= MAX_MEMBERS_PER_PROJECT) {
						throw new BusinessException(
								"Projeto já atingiu o limite máximo de " + MAX_MEMBERS_PER_PROJECT + " membros."
						);
				}

				MemberResponse member = memberApiClient.findMemberById(memberId)
						.orElseThrow(() -> new ResourceNotFoundException("Membro", memberId));

				if (member.getRole() != MemberRole.FUNCIONARIO) {
						throw new BusinessException(
								"Apenas membros com atribuição 'funcionário' podem ser alocados em projetos. " +
										"O membro '" + member.getName() + "' possui atribuição: " + member.getRole().getDisplayName()
						);
				}

				long activeProjectCount = projectRepository.countActiveProjectsByMemberId(
						memberId, EXCLUDED_FROM_MEMBER_LIMIT
				);

				if (activeProjectCount >= MAX_ACTIVE_PROJECTS_PER_MEMBER) {
						throw new BusinessException(
								"Membro '" + member.getName() + "' já está alocado no máximo de " +
										MAX_ACTIVE_PROJECTS_PER_MEMBER + " projetos ativos simultaneamente."
						);
				}

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
						throw new BusinessException("Membro com ID " + memberId + " não está alocado neste projeto.");
				}

				if (project.getMemberIds().size() <= MIN_MEMBERS_PER_PROJECT) {
						throw new BusinessException(
								"Não é possível remover o membro. O projeto deve ter no mínimo " +
										MIN_MEMBERS_PER_PROJECT + " membro(s) alocado(s)."
						);
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

		private Project findProjectOrThrow(Long id) {
				return projectRepository.findById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
		}

		private void validateManagerExists(Long managerId) {
				memberApiClient.findMemberById(managerId)
						.orElseThrow(() -> new ResourceNotFoundException(
								"Gerente (membro) com ID " + managerId + " não encontrado."
						));
		}

		private void validateDates(java.time.LocalDate startDate, java.time.LocalDate expectedEndDate) {
				if (expectedEndDate.isBefore(startDate) || expectedEndDate.isEqual(startDate)) {
						throw new BusinessException(
								"A previsão de término deve ser posterior à data de início do projeto."
						);
				}
		}

		private ProjectResponse buildProjectResponse(Project project) {
				ProjectResponse response = projectMapper.toResponse(project);

				memberApiClient.findMemberById(project.getManagerId())
						.ifPresent(m -> response.setManagerName(m.getName()));

				return response;
		}
}
