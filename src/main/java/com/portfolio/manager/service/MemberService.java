package com.portfolio.manager.service;

import com.portfolio.manager.dto.request.MemberCreateRequest;
import com.portfolio.manager.dto.response.MemberResponse;

import java.util.List;

public interface MemberService {

		MemberResponse createMember(MemberCreateRequest request);

		MemberResponse getMemberById(Long id);

		List<MemberResponse> getAllMembers();
}
