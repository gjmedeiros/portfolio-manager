package com.portfolio.manager.service.impl;

import com.portfolio.manager.client.MemberApiClient;
import com.portfolio.manager.dto.request.MemberCreateRequest;
import com.portfolio.manager.dto.response.MemberResponse;
import com.portfolio.manager.exception.ResourceNotFoundException;
import com.portfolio.manager.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

		private final MemberApiClient memberApiClient;

		@Override
		public MemberResponse createMember(MemberCreateRequest request) {
				log.info("Criando novo membro: {}", request.getName());
				return memberApiClient.createMember(request);
		}

		@Override
		public MemberResponse getMemberById(Long id) {
				return memberApiClient.findMemberById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Membro", id));
		}

		@Override
		public List<MemberResponse> getAllMembers() {
				return memberApiClient.findAllMembers();
		}
}
