package com.barogagi.member.login.controller;

import com.barogagi.member.login.repository.UserMembershipRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {

    private final UserMembershipRepository userRepo;

    public MeController(UserMembershipRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/me")
    public Map<String,Object> me(Authentication auth) {
        // JwtAuthFilter에서 principal.username = membershipNo 문자열로 넣었음
        Long no = Long.parseLong(auth.getName());
        var u = userRepo.findById(no).orElseThrow();
        return Map.of(
                "membershipNo", u.getMembershipNo(),
                "userId", u.getUserId(),
                "joinType", u.getJoinType()
        );
    }
}


