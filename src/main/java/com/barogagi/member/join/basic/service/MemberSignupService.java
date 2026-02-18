package com.barogagi.member.join.basic.service;

import com.barogagi.config.PasswordConfig;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.join.basic.dto.JoinRequestDTO;
import com.barogagi.member.join.basic.exception.JoinException;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberSignupService {

    private final Validator validator;
    private final InputValidate inputValidate;
    private final EncryptUtil encryptUtil;
    private final PasswordConfig passwordConfig;

    private final UserMembershipRepository userMembershipRepository;

    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Transactional
    public ApiResponse signupBasic(String apiSecretKey, JoinRequestDTO joinRequestDTO) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new JoinException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        // 필수 입력값(아이디, 비밀번호, 휴대전화번호 값이 빈 값이 아닌지 확인)
        // 선택 입력값(이메일, 생년월일, 성별, 닉네임)
        if(inputValidate.isEmpty(joinRequestDTO.getUserId())
                || inputValidate.isEmpty(joinRequestDTO.getPassword())
                || inputValidate.isEmpty(joinRequestDTO.getTel())) {
            throw new JoinException(ErrorCode.EMPTY_DATA);
        }

        // 3. 적합한 아이디인지 확인
        // 아이디, 비밀번호 적합성 검사
        if(!(validator.isValidId(joinRequestDTO.getUserId())
                && validator.isValidPassword(joinRequestDTO.getPassword()))) {
            throw new JoinException(ErrorCode.INVALID_SIGN_UP);
        }

        boolean existsByUserId = userMembershipRepository.existsByUserId(joinRequestDTO.getUserId().trim());
        if(existsByUserId) {
            throw new JoinException(ErrorCode.UNAVAILABLE_USER_ID);
        }

        boolean existsNickname = userMembershipRepository.existsByNickName(joinRequestDTO.getNickName());
        if(existsNickname) {
            throw new JoinException(ErrorCode.UNAVAILABLE_NICKNAME);
        }

        // 4. 암호화
        // 휴대전화번호, 비밀번호 암호화
        joinRequestDTO.setTel(encryptUtil.encrypt(joinRequestDTO.getTel().replaceAll("[^0-9]", "")));
        String encodedPassword = passwordConfig.passwordEncoder().encode(joinRequestDTO.getPassword());
        joinRequestDTO.setPassword(encodedPassword);

        // 이메일 값이 넘어오면 암호화
        if(!inputValidate.isEmpty(joinRequestDTO.getEmail())){
            joinRequestDTO.setEmail(encryptUtil.encrypt(joinRequestDTO.getEmail()));
        }

        // 생년월일 데이터 처리
        if(null != joinRequestDTO.getBirth()) {
            joinRequestDTO.setBirth(joinRequestDTO.getBirth().replaceAll("[^0-9]", ""));
        }

        joinRequestDTO.setJoinType("BASIC");

        // 6. 회원 정보 저장
        String membershipNo = this.signUp(joinRequestDTO);
        if(membershipNo.isEmpty()){
            throw new JoinException(ErrorCode.FAIL_SIGN_UP);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_SIGN_UP);
    }

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

        Optional<UserMembershipInfo> userMembershipInfo = userMembershipRepository.findById(membershipNo);
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

        userMembershipRepository.save(userMembershipInfo);
        return userMembershipInfo.getMembershipNo();
    }
}
