package com.barogagi.member.service.impl;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.join.oauth.dto.OAuth2UserDTO;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.member.repository.spec.UserMembershipSpec;
import com.barogagi.member.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMembershipServiceImpl implements UserMembershipService {

    private final UserMembershipRepository userMembershipRepository;

    @Override
    public UserMembershipInfo findByOAuthSub(OAuth2UserDTO oAuth2UserDTO) {
        Specification<UserMembershipInfo> spec = UserMembershipSpec.userIdEq(oAuth2UserDTO.getSub())
                        .and(UserMembershipSpec.emailEq(oAuth2UserDTO.getEmail()))
                        .and(UserMembershipSpec.joinTypeEq(oAuth2UserDTO.getJoinType()));

        return userMembershipRepository.findOne(spec).orElse(null);
    }
}
