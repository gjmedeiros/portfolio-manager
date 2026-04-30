package com.portfolio.manager.mapper;

import com.portfolio.manager.dto.request.ProjectCreateRequest;
import com.portfolio.manager.dto.request.ProjectUpdateRequest;
import com.portfolio.manager.dto.response.ProjectResponse;
import com.portfolio.manager.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {

		@Mapping(target = "id", ignore = true)
		@Mapping(target = "status", expression = "java(com.portfolio.manager.enums.ProjectStatus.EM_ANALISE)")
		@Mapping(target = "memberIds", expression = "java(new java.util.HashSet<>())")
		@Mapping(target = "actualEndDate", ignore = true)
		Project toEntity(ProjectCreateRequest request);

		@Mapping(target = "riskClassification", expression = "java(project.getRiskClassification())")
		@Mapping(target = "memberCount", expression = "java(project.getMemberIds().size())")
		@Mapping(target = "managerName", ignore = true)
		ProjectResponse toResponse(Project project);

		@Mapping(target = "id", ignore = true)
		@Mapping(target = "status", ignore = true)
		@Mapping(target = "memberIds", ignore = true)
		void updateFromRequest(ProjectUpdateRequest request, @MappingTarget Project project);
}
