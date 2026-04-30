package com.portfolio.manager.controller;

import com.portfolio.manager.dto.request.MemberCreateRequest;
import com.portfolio.manager.dto.response.MemberResponse;
import com.portfolio.manager.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Membros", description = "Gerenciamento de membros via API externa (mockada)")
@SecurityRequirement(name = "basicAuth")
public class MemberController {

		private final MemberService memberService;

		@PostMapping
		@Operation(
				summary = "Criar membro",
				description = "Cria um novo membro delegando à API externa. Atribuições disponíveis: FUNCIONARIO, GERENTE"
		)
		@ApiResponses({
				@ApiResponse(responseCode = "201", description = "Membro criado com sucesso"),
				@ApiResponse(responseCode = "400", description = "Dados inválidos"),
				@ApiResponse(responseCode = "503", description = "Serviço externo indisponível")
		})
		public ResponseEntity<MemberResponse> createMember(
				@Valid @RequestBody MemberCreateRequest request) {
				return ResponseEntity.status(HttpStatus.CREATED)
						.body(memberService.createMember(request));
		}

		@GetMapping("/{id}")
		@Operation(summary = "Buscar membro por ID")
		@ApiResponses({
				@ApiResponse(responseCode = "200", description = "Membro encontrado"),
				@ApiResponse(responseCode = "404", description = "Membro não encontrado")
		})
		public ResponseEntity<MemberResponse> getMemberById(
				@Parameter(description = "ID do membro") @PathVariable Long id) {
				return ResponseEntity.ok(memberService.getMemberById(id));
		}

		@GetMapping
		@Operation(summary = "Listar todos os membros")
		@ApiResponse(responseCode = "200", description = "Lista de membros retornada")
		public ResponseEntity<List<MemberResponse>> getAllMembers() {
				return ResponseEntity.ok(memberService.getAllMembers());
		}
}
