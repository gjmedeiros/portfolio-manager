package com.portfolio.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.manager.config.TestSecurityConfig;
import com.portfolio.manager.dto.request.ProjectCreateRequest;
import com.portfolio.manager.dto.request.ProjectStatusUpdateRequest;
import com.portfolio.manager.dto.response.ProjectResponse;
import com.portfolio.manager.enums.ProjectStatus;
import com.portfolio.manager.exception.BusinessException;
import com.portfolio.manager.exception.InvalidStatusTransitionException;
import com.portfolio.manager.exception.ResourceNotFoundException;
import com.portfolio.manager.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ProjectController - Testes de Endpoints")
class ProjectControllerTest {

		@Autowired MockMvc mockMvc;
		@Autowired ObjectMapper objectMapper;
		@MockBean ProjectService projectService;

		private static final String BASE_URL = "/api/v1/projects";

		// ── POST /projects ────────────────────────────────────────────────────────────

		@Test
		@WithMockUser
		@DisplayName("POST /projects - 201 quando dados válidos")
		void shouldReturn201WhenCreatingValidProject() throws Exception {
				ProjectCreateRequest request = validCreateRequest();
				ProjectResponse response = buildResponse(1L, ProjectStatus.EM_ANALISE);

				when(projectService.createProject(any())).thenReturn(response);

				mockMvc.perform(post(BASE_URL)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isCreated())
						.andExpect(jsonPath("$.id").value(1))
						.andExpect(jsonPath("$.status").value("EM_ANALISE"));
		}

		@Test
		@WithMockUser
		@DisplayName("POST /projects - 400 quando nome está em branco")
		void shouldReturn400WhenNameIsBlank() throws Exception {
				ProjectCreateRequest request = validCreateRequest();
				request.setName("");

				mockMvc.perform(post(BASE_URL)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.fieldErrors.name").exists());
		}

		// ── GET /projects ─────────────────────────────────────────────────────────────

		@Test
		@WithMockUser
		@DisplayName("GET /projects - 200 com lista paginada")
		void shouldReturn200WhenListingProjects() throws Exception {
				ProjectResponse response = buildResponse(1L, ProjectStatus.EM_ANALISE);
				when(projectService.listProjects(any(), any()))
						.thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

				mockMvc.perform(get(BASE_URL))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.content").isArray())
						.andExpect(jsonPath("$.content[0].id").value(1));
		}

		@Test
		@DisplayName("GET /projects - 401 quando não autenticado")
		void shouldReturn401WhenNotAuthenticated() throws Exception {
				mockMvc.perform(get(BASE_URL))
						.andExpect(status().isUnauthorized());
		}

		// ── GET /projects/{id} ────────────────────────────────────────────────────────

		@Test
		@WithMockUser
		@DisplayName("GET /projects/{id} - 200 quando projeto existe")
		void shouldReturn200WhenProjectFound() throws Exception {
				when(projectService.getProjectById(1L)).thenReturn(buildResponse(1L, ProjectStatus.EM_ANALISE));

				mockMvc.perform(get(BASE_URL + "/1"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.id").value(1));
		}

		@Test
		@WithMockUser
		@DisplayName("GET /projects/{id} - 404 quando projeto não existe")
		void shouldReturn404WhenProjectNotFound() throws Exception {
				when(projectService.getProjectById(99L))
						.thenThrow(new ResourceNotFoundException("Projeto", 99L));

				mockMvc.perform(get(BASE_URL + "/99"))
						.andExpect(status().isNotFound())
						.andExpect(jsonPath("$.message").exists());
		}

		// ── PATCH /projects/{id}/status ───────────────────────────────────────────────

		@Test
		@WithMockUser
		@DisplayName("PATCH /projects/{id}/status - 200 em transição válida")
		void shouldReturn200OnValidStatusTransition() throws Exception {
				ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.ANALISE_REALIZADA);
				ProjectResponse response = buildResponse(1L, ProjectStatus.ANALISE_REALIZADA);

				when(projectService.updateProjectStatus(eq(1L), any())).thenReturn(response);

				mockMvc.perform(patch(BASE_URL + "/1/status")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.status").value("ANALISE_REALIZADA"));
		}

		@Test
		@WithMockUser
		@DisplayName("PATCH /projects/{id}/status - 422 em transição inválida")
		void shouldReturn422OnInvalidStatusTransition() throws Exception {
				ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.ENCERRADO);

				when(projectService.updateProjectStatus(eq(1L), any()))
						.thenThrow(new InvalidStatusTransitionException(ProjectStatus.EM_ANALISE, ProjectStatus.ENCERRADO));

				mockMvc.perform(patch(BASE_URL + "/1/status")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isUnprocessableEntity())
						.andExpect(jsonPath("$.message").exists());
		}

		// ── DELETE /projects/{id} ─────────────────────────────────────────────────────

		@Test
		@WithMockUser
		@DisplayName("DELETE /projects/{id} - 204 quando exclusão é permitida")
		void shouldReturn204OnSuccessfulDelete() throws Exception {
				doNothing().when(projectService).deleteProject(1L);

				mockMvc.perform(delete(BASE_URL + "/1"))
						.andExpect(status().isNoContent());
		}

		@Test
		@WithMockUser
		@DisplayName("DELETE /projects/{id} - 422 quando projeto não pode ser excluído")
		void shouldReturn422WhenProjectCannotBeDeleted() throws Exception {
				doThrow(new BusinessException("Projeto não pode ser excluído"))
						.when(projectService).deleteProject(1L);

				mockMvc.perform(delete(BASE_URL + "/1"))
						.andExpect(status().isUnprocessableEntity());
		}

		// ── Members ───────────────────────────────────────────────────────────────────

		@Test
		@WithMockUser
		@DisplayName("POST /projects/{id}/members/{memberId} - 200 ao adicionar membro válido")
		void shouldReturn200WhenAddingValidMember() throws Exception {
				ProjectResponse response = buildResponse(1L, ProjectStatus.EM_ANALISE);
				when(projectService.addMemberToProject(1L, 2L)).thenReturn(response);

				mockMvc.perform(post(BASE_URL + "/1/members/2"))
						.andExpect(status().isOk());
		}

		@Test
		@WithMockUser
		@DisplayName("DELETE /projects/{id}/members/{memberId} - 200 ao remover membro")
		void shouldReturn200WhenRemovingMember() throws Exception {
				ProjectResponse response = buildResponse(1L, ProjectStatus.EM_ANALISE);
				when(projectService.removeMemberFromProject(1L, 2L)).thenReturn(response);

				mockMvc.perform(delete(BASE_URL + "/1/members/2"))
						.andExpect(status().isOk());
		}

		private ProjectCreateRequest validCreateRequest() {
				return ProjectCreateRequest.builder()
						.name("Projeto Teste")
						.startDate(LocalDate.of(2024, 1, 1))
						.expectedEndDate(LocalDate.of(2024, 6, 30))
						.totalBudget(new BigDecimal("200000"))
						.managerId(1L)
						.build();
		}

		private ProjectResponse buildResponse(Long id, ProjectStatus status) {
				return ProjectResponse.builder()
						.id(id).name("Projeto Teste").status(status)
						.totalBudget(new BigDecimal("200000"))
						.startDate(LocalDate.of(2024, 1, 1))
						.expectedEndDate(LocalDate.of(2024, 6, 30))
						.memberIds(new HashSet<>()).memberCount(0).build();
		}
}
