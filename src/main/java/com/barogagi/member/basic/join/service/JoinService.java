package com.barogagi.member.basic.join.service;

import com.barogagi.member.basic.join.dto.JoinRequestDTO;
import com.barogagi.member.basic.join.entity.UserMembershipInfo;
import com.barogagi.member.basic.join.repository.BasicJoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class JoinService {

    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final BasicJoinRepository basicJoinRepository;

    // 회원번호 랜덤값 생성
    public String createRandomStr() {
        String randomStr = "";

        try {

            List<Character> chars = new ArrayList<>();

            // 영문 3개 생성
            for (int i = 0; i < 3; i++) {
                chars.add(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }

            // 숫자 9개 생성
            for (int i = 0; i < 9; i++) {
                chars.add((char) ('0' + random.nextInt(10)));
            }

            // 섞기
            Collections.shuffle(chars, random);

            // String으로 변환
            StringBuilder stringBuilder = new StringBuilder();
            for (char c : chars) {
                stringBuilder.append(c);
            }

            randomStr = stringBuilder.toString();

        } catch (Exception e) {
            randomStr = "";
        }

        return randomStr;
    }

    // 회원번호 중복 체크
    public boolean checkDuplicateMemberNo(String membershipNo) {
        boolean duplicateFlag = false;

        Optional<UserMembershipInfo> userMembershipInfo = basicJoinRepository.findById(membershipNo);
        if(userMembershipInfo.isPresent()) {
            // 중복 발생
            duplicateFlag = true;
        }

        return duplicateFlag;
    }

    // 회원번호 구하기
    public String generateMemberNo() {

        String membershipNo = "";

        while(true) {
            // 랜덤 회원번호 생성
            membershipNo = this.createRandomStr();

            if(membershipNo.isEmpty()) {
                break;
            }
            // 회원번호 중복 체크
            boolean checkDuplicateMembershipNo = this.checkDuplicateMemberNo(membershipNo);
            if(!checkDuplicateMembershipNo) {
                break;
            }
        }
        return membershipNo;
    }

    // 회원가입 정보 저장 기능
    public String signUp(JoinRequestDTO joinRequestDTO) {
        UserMembershipInfo userMembershipInfo = UserMembershipInfo.builder()
                .membershipNo(this.generateMemberNo())
                .userId(joinRequestDTO.getUserId())
                .password(joinRequestDTO.getPassword())
                .email(joinRequestDTO.getEmail())
                .birth(joinRequestDTO.getBirth())
                .tel(joinRequestDTO.getTel())
                .gender(joinRequestDTO.getGender())
                .nickName(joinRequestDTO.getNickName())
                .joinType(joinRequestDTO.getJoinType())
                .build();

        basicJoinRepository.save(userMembershipInfo);
        return userMembershipInfo.getMembershipNo();
    }

    // 아이디 중복 체크
    @Transactional(readOnly = true)
    public boolean existsByUserId(String userId) {
        return basicJoinRepository.existsByUserId(userId);
    }

    // 닉네임 중복 체크
    public boolean existsByNickName(String nickname) {
        return basicJoinRepository.existsByNickName(nickname);
    }
}
