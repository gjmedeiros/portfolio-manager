package com.portfolio.manager.client;

import com.portfolio.manager.dto.request.MemberCreateRequest;
import com.portfolio.manager.dto.response.MemberResponse;
import com.portfolio.manager.enums.MemberRole;
import com.portfolio.manager.exception.MemberServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class MemberApiClient {

		private final Map<Long, MemberResponse> memberStore = new ConcurrentHashMap<>();
		private final AtomicLong idSequence = new AtomicLong(1);

		@Value("${member.api.base-url}")
		private String baseUrl;

		public MemberApiClient() {
				memberStore.put(1L, MemberResponse.builder().id(1L).name("Carlos").role(MemberRole.GERENTE).build());
				memberStore.put(2L, MemberResponse.builder().id(2L).name("Ana").role(MemberRole.FUNCIONARIO).build());
				memberStore.put(3L, MemberResponse.builder().id(3L).name("Pedro").role(MemberRole.FUNCIONARIO).build());
				memberStore.put(4L, MemberResponse.builder().id(4L).name("Maria").role(MemberRole.FUNCIONARIO).build());
				idSequence.set(5L);
		}

		public MemberResponse createMember(MemberCreateRequest request) {
				log.info("Criação de membro via API externa: {}", request.getName());
				try {
						long newId = idSequence.getAndIncrement();
						MemberResponse member = MemberResponse.builder()
								.id(newId)
								.name(request.getName())
								.role(request.getRole())
								.build();
						memberStore.put(newId, member);
						log.info("Membro criado com o ID: {}", newId);
						return member;
				} catch (Exception e) {
						throw new MemberServiceException("Erro ao criar membro no serviço externo: " + e.getMessage(), e);
				}
		}

		public Optional<MemberResponse> findMemberById(Long memberId) {
				log.debug("Busca membro {} da API externa", memberId);
				return Optional.ofNullable(memberStore.get(memberId));
		}

		public List<MemberResponse> findAllMembers() {
				log.debug("Busca todos os membros da API externa");
				return new ArrayList<>(memberStore.values());
		}

		public List<MemberResponse> findMembersByIds(List<Long> ids) {
				return ids.stream()
						.map(memberStore::get)
						.filter(m -> !Objects.isNull(m))
						.toList();
		}
}
