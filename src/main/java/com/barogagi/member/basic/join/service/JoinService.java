package com.barogagi.member.basic.join.service;

import com.barogagi.member.basic.join.dto.NickNameDTO;
import com.barogagi.member.basic.join.mapper.JoinMapper;
import com.barogagi.member.basic.join.dto.JoinDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class JoinService {

    private static final Logger logger = LoggerFactory.getLogger(JoinService.class);

    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final JoinMapper joinMapper;

    @Autowired
    public JoinService(JoinMapper joinMapper) {
        this.joinMapper = joinMapper;
    }

    // 닉네임 개수 구하기
    public int selectNicknameCnt(NickNameDTO nickNameDTO) {
        return joinMapper.selectNicknameCnt(nickNameDTO);
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
            logger.error(e.toString());
            randomStr = "";
        }

        return randomStr;
    }

    // 회원번호 중복 체크
    public boolean checkDuplicateMemberNo(String membershipNo) {
        boolean duplicateFlag = false;

        int membershipNoCnt = joinMapper.checkDuplicateMembershipNo(membershipNo);
        logger.info("membershipNoCnt={}", membershipNoCnt);
        if(membershipNoCnt > 0) {
            // 중복 발생
            duplicateFlag = true;
        }

        return duplicateFlag;
    }

    // 회원가입 정보 저장 service
    public int insertMembershipInfo(JoinDTO vo) {

        int result = 0;

        String membershipNo = "";

        while(true) {
            // 랜덤 회원번호 생성
            membershipNo = this.createRandomStr();

            logger.info("membershipNo.isEmpty()={}", membershipNo.isEmpty());
            if(membershipNo.isEmpty()) {
                break;
            }

            // 회원번호 중복 체크
            boolean checkDuplicateMembershipNo = this.checkDuplicateMemberNo(membershipNo);
            logger.info("checkDuplicateMembershipNo={}", checkDuplicateMembershipNo);
            if(!checkDuplicateMembershipNo) {
                break;
            }
        }

        logger.info("membershipNo.isEmpty()={}", membershipNo.isEmpty());
        if(!membershipNo.isEmpty()) {  // 회원번호가 비어있을 경우 저장 X
            vo.setMembershipNo(membershipNo);
            result = this.insertMemberInfo(vo);
        }

        return result;
    }

    // 회원가입 정보 저장 기능
    public int insertMemberInfo(JoinDTO vo) {
        return joinMapper.insertMemberInfo(vo);
    }

    // 아이디 개수 구하기
    public int selectUserIdCnt(JoinDTO vo) {
        return joinMapper.selectUserIdCnt(vo);
    }
}
