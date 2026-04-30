package com.portfolio.manager.service;

import com.portfolio.manager.dto.request.ProjectCreateRequest;
import com.portfolio.manager.dto.request.ProjectFilterRequest;
import com.portfolio.manager.dto.request.ProjectStatusUpdateRequest;
import com.portfolio.manager.dto.request.ProjectUpdateRequest;
import com.portfolio.manager.dto.response.PortfolioReportResponse;
import com.portfolio.manager.dto.response.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

		ProjectResponse createProject(ProjectCreateRequest request);

		ProjectResponse getProjectById(Long id);

		Page<ProjectResponse> listProjects(ProjectFilterRequest filter, Pageable pageable);

		ProjectResponse updateProject(Long id, ProjectUpdateRequest request);

		ProjectResponse updateProjectStatus(Long id, ProjectStatusUpdateRequest request);

		void deleteProject(Long id);

		ProjectResponse addMemberToProject(Long projectId, Long memberId);

		ProjectResponse removeMemberFromProject(Long projectId, Long memberId);

		PortfolioReportResponse generatePortfolioReport();
}
