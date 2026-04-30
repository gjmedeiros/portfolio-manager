package com.portfolio.manager.service;

import com.portfolio.manager.client.MemberApiClient;
import com.portfolio.manager.dto.request.MemberCreateRequest;
import com.portfolio.manager.dto.response.MemberResponse;
import com.portfolio.manager.enums.MemberRole;
import com.portfolio.manager.exception.ResourceNotFoundException;
import com.portfolio.manager.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl - Testes")
class MemberServiceImplTest {

		@Mock private MemberApiClient memberApiClient;
		@InjectMocks private MemberServiceImpl memberService;

		@Test
		@DisplayName("Deve criar membro com sucesso")
		void shouldCreateMemberSuccessfully() {
				MemberCreateRequest request = new MemberCreateRequest("João", MemberRole.FUNCIONARIO);
				MemberResponse expected = MemberResponse.builder().id(1L).name("João").role(MemberRole.FUNCIONARIO).build();

				when(memberApiClient.createMember(request)).thenReturn(expected);

				MemberResponse result = memberService.createMember(request);

				assertThat(result.getId()).isEqualTo(1L);
				assertThat(result.getRole()).isEqualTo(MemberRole.FUNCIONARIO);
		}

		@Test
		@DisplayName("Deve retornar membro quando ID existe")
		void shouldReturnMemberWhenFound() {
				MemberResponse member = MemberResponse.builder().id(1L).name("Ana").role(MemberRole.FUNCIONARIO).build();
				when(memberApiClient.findMemberById(1L)).thenReturn(Optional.of(member));

				MemberResponse result = memberService.getMemberById(1L);

				assertThat(result.getName()).isEqualTo("Ana");
		}

		@Test
		@DisplayName("Deve lançar exceção quando membro não encontrado")
		void shouldThrowWhenMemberNotFound() {
				when(memberApiClient.findMemberById(99L)).thenReturn(Optional.empty());

				assertThatThrownBy(() -> memberService.getMemberById(99L))
						.isInstanceOf(ResourceNotFoundException.class)
						.hasMessageContaining("99");
		}

		@Test
		@DisplayName("Deve retornar lista de todos os membros")
		void shouldReturnAllMembers() {
				List<MemberResponse> members = List.of(
						MemberResponse.builder().id(1L).name("A").role(MemberRole.FUNCIONARIO).build(),
						MemberResponse.builder().id(2L).name("B").role(MemberRole.GERENTE).build()
				);
				when(memberApiClient.findAllMembers()).thenReturn(members);

				List<MemberResponse> result = memberService.getAllMembers();

				assertThat(result).hasSize(2);
		}
}
