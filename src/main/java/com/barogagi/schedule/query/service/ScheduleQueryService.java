package com.barogagi.schedule.query.service;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.mainPage.response.MainPageResponse;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.join.oauth.enums.Environment;
import com.barogagi.plan.query.service.PlanQueryService;
import com.barogagi.plan.query.vo.PlanDetailVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.dto.ScheduleDetailResDTO;
import com.barogagi.schedule.dto.ScheduleListGroupResDTO;
import com.barogagi.schedule.dto.ScheduleListResDTO;
import com.barogagi.schedule.entity.ScheduleShare;
import com.barogagi.schedule.exception.ScheduleException;
import com.barogagi.schedule.query.mapper.ScheduleMapper;
import com.barogagi.schedule.query.vo.ScheduleDetailVO;
import com.barogagi.schedule.query.vo.ScheduleListVO;
import com.barogagi.schedule.query.vo.ScheduleMembershipNoVO;
import com.barogagi.schedule.repository.ScheduleShareRepository;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.BasicException;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleQueryService.class);

    private final MembershipUtil membershipUtil;

    private final ScheduleMapper scheduleMapper;

    private final PlanQueryService planQueryService;

    private final Validator validator;

    private final ScheduleShareRepository scheduleShareRepository;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Autowired
    public ScheduleQueryService (MembershipUtil membershipUtil,
                                 ScheduleMapper scheduleMapper,
                                 PlanQueryService planQueryService,
                                 Validator validator,
                                 ScheduleShareRepository scheduleShareRepository) {
        this.membershipUtil = membershipUtil;
        this.scheduleMapper = scheduleMapper;
        this.planQueryService = planQueryService;
        this.validator = validator;
        this.scheduleShareRepository = scheduleShareRepository;
    }

    public ApiResponse getScheduleList(HttpServletRequest request) {
        try {
            // 1. API SECRET KEY 검증
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(), ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            // 2. 회원번호 조회
            Map<String, Object> membershipInfo = membershipUtil.membershipNoService(request);
            if (!membershipInfo.get("resultCode").equals("A200")) {
                return ApiResponse.error(ErrorCode.NOT_EXIST_ACCESS_AUTH.getCode(), ErrorCode.NOT_EXIST_ACCESS_AUTH.getMessage());
            }

            String membershipNo = String.valueOf(membershipInfo.get("membershipNo"));

            // 3. 일정 목록 조회
            List<ScheduleListVO> scheduleListVOList = scheduleMapper.selectScheduleList(membershipNo);

            // 4. DTO 변환 및 그룹핑
            ScheduleListGroupResDTO result = groupSchedulesByDate(scheduleListVOList);

            return ApiResponse.resultData(result, ErrorCode.FOUND_INFO_SCHEDULE.getCode(), ErrorCode.FOUND_INFO_SCHEDULE.getMessage());

        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }

    // 일정 그룹핑 로직 분리
    private ScheduleListGroupResDTO groupSchedulesByDate(List<ScheduleListVO> scheduleListVOList) {
        LocalDate today = LocalDate.now();

        List<ScheduleListResDTO> scheduleList = scheduleListVOList.stream()
                .map(this::convertToDTO)
                .toList();

        List<ScheduleListResDTO> pastSchedules = scheduleList.stream()
                .filter(s -> LocalDate.parse(s.getEndDate()).isBefore(today))
                .toList();

        List<ScheduleListResDTO> upcomingSchedules = scheduleList.stream()
                .filter(s -> !LocalDate.parse(s.getEndDate()).isBefore(today))
                .toList();

        return ScheduleListGroupResDTO.builder()
                .pastSchedules(pastSchedules)
                .upcomingSchedules(upcomingSchedules)
                .build();
    }

    // VO -> DTO 변환 로직 분리
    private ScheduleListResDTO convertToDTO(ScheduleListVO vo) {
        return ScheduleListResDTO.builder()
                .scheduleNum(vo.getScheduleNum())
                .scheduleNm(vo.getScheduleNm())
                .startDate(vo.getStartDate())
                .endDate(vo.getEndDate())
                .scheduleTagRegistResDTOList(vo.getScheduleTagRegistResDTOList())
                .build();
    }


    public ApiResponse getScheduleDetail(int scheduleNum, HttpServletRequest request) {

        try {
            // 1. API SECRET KEY 검증
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(), ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            // 2. 회원번호 조회
            Map<String, Object> membershipInfo = membershipUtil.membershipNoService(request);
            if (!membershipInfo.get("resultCode").equals("A200")) {
                return ApiResponse.error(ErrorCode.NOT_EXIST_ACCESS_AUTH.getCode(), ErrorCode.NOT_EXIST_ACCESS_AUTH.getMessage());
            }

            // 일정 정보 조회
            ScheduleMembershipNoVO scheduleMembershipNoVO = new ScheduleMembershipNoVO(scheduleNum, String.valueOf(membershipInfo.get("membershipNo")));
            ScheduleDetailVO scheduleDetailVO = scheduleMapper.selectScheduleDetail(scheduleMembershipNoVO);
            if (null == scheduleDetailVO) throw new BasicException(ErrorCode.NOT_FOUND_SCHEDULE);
            else if (scheduleDetailVO.getDelYn().equals("Y"))
                throw new BasicException(ErrorCode.ALREADY_DELETED_SCHEDULE);

            // 계획 정보 조회 (리스트)
            logger.info("계획 조회 시작");
            List<PlanDetailVO> planDetailVOList = planQueryService.getPlanDetail(scheduleNum);

            // 각 계획의 링크에서 OG 이미지 프록시 URL 세팅
            for (PlanDetailVO plan : planDetailVOList) {
                if (plan.getPlanLink() != null && !plan.getPlanLink().isBlank()) {
                    try {
                        String imageUrl = extractOgImage(plan.getPlanLink());
                        if (imageUrl != null) {
                            plan.setImageLink(imageUrl);                        }
                    } catch (Exception e) {
                        logger.warn("OG 이미지 추출 실패: {}", plan.getPlanLink());
                    }
                }
            }
            String testImageUrl = extractOgImage("https://place.map.kakao.com/850873071");
            logger.info(testImageUrl);

            // DTO에 정보 저장
            ScheduleDetailResDTO result = ScheduleDetailResDTO.builder()
                    .scheduleNum(scheduleDetailVO.getScheduleNum())
                    .scheduleNm(scheduleDetailVO.getScheduleNm())
                    .startDate(scheduleDetailVO.getStartDate())
                    .endDate(scheduleDetailVO.getEndDate())
                    .radius(scheduleDetailVO.getRadius())
                    .planDetailVOList(planDetailVOList)
                    .build();

            logger.info("result={}", result.toString());
            return ApiResponse.resultData(result, ErrorCode.FOUND_INFO_SCHEDULE.getCode(), ErrorCode.FOUND_INFO_SCHEDULE.getMessage());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }

    public String extractOgImage(String link) throws IOException {
        Document doc = Jsoup.connect(link)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept-Language", "ko-KR,ko;q=0.9")
                .timeout(5000)
                .followRedirects(true)
                .get();

        Element ogImage = doc.selectFirst("meta[property=og:image]");

        if (ogImage == null || ogImage.attr("content").isBlank()) {
            return null;
        }

        String imageUrl = ogImage.attr("content");

        if (imageUrl.startsWith("//")) {
            imageUrl = "https:" + imageUrl;
        }

        return imageUrl;
    }

    @Transactional
    public ApiResponse shareScheduleLink(String apiSecretKey, HttpServletRequest request, int scheduleNum, Environment environment) {

        // 1. API SECRET KEY 검증
        if (!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new ScheduleException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 회원번호 조회
        Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
        if(!membershipNoInfo.get("resultCode").equals("A200")) {
            return ApiResponse.result(String.valueOf(membershipNoInfo.get("resultCode")),
                    String.valueOf(membershipNoInfo.get("message")));
        }

        // 3. 일정 정보 조회
        ScheduleMembershipNoVO scheduleMembershipNoVO = new ScheduleMembershipNoVO(scheduleNum, String.valueOf(membershipNoInfo.get("membershipNo")));
        ScheduleDetailVO scheduleDetailVO = scheduleMapper.selectScheduleDetail(scheduleMembershipNoVO);
        if (null == scheduleDetailVO) {
            throw new BasicException(ErrorCode.NOT_FOUND_SCHEDULE);
        } else if (scheduleDetailVO.getDelYn().equals("Y")) {
            throw new ScheduleException(ErrorCode.ALREADY_DELETED_SCHEDULE);
        }

        // 4. 토큰 생성
        String randomToken = "";
        while(true) {
            randomToken = this.createRandomToken();

            // 중복 체크
            boolean checkDuplicateRandomToken = scheduleShareRepository.existsByToken(randomToken);
            if(!checkDuplicateRandomToken) {
                break;
            }
        }

        // 5. 토큰 저장
        ScheduleShare scheduleShare = ScheduleShare.builder()
                .scheduleNum(scheduleNum)
                .shareToken(randomToken)
                .membershipNo(String.valueOf(membershipNoInfo.get("membershipNo")))
                .createdAt(LocalDateTime.now())
                .expireAt(LocalDateTime.now().plusDays(1))
                .build();
        scheduleShareRepository.save(scheduleShare);

        // 6. 공유 링크 생성
        List<String> addresses = Arrays.asList(allowedOrigins.split(","));
        String uri = "/share/schedule/" + scheduleShare.getShareToken();

        String shareLink = "";
        if(environment.equals(Environment.LOCAL)) {  // 로컬 서버
            shareLink = addresses.get(0) + uri;
        } else if(environment.equals(Environment.TEST)) {  // 테스트 서버
            shareLink = addresses.get(1) + uri;
        } else if(environment.equals(Environment.PROD)) {  // 실서버
            shareLink = addresses.get(2) + uri;
        }

        return ApiResponse.resultData(shareLink, "S200", "일정 공유 링크가 생성되었습니다.");
    }

    public String createRandomToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }
}
