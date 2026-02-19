package com.barogagi.schedule.query.service;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.mainPage.response.MainPageResponse;
import com.barogagi.plan.query.service.PlanQueryService;
import com.barogagi.plan.query.vo.PlanDetailVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.dto.ScheduleDetailResDTO;
import com.barogagi.schedule.dto.ScheduleListGroupResDTO;
import com.barogagi.schedule.dto.ScheduleListResDTO;
import com.barogagi.schedule.query.mapper.ScheduleMapper;
import com.barogagi.schedule.query.vo.ScheduleDetailVO;
import com.barogagi.schedule.query.vo.ScheduleListVO;
import com.barogagi.schedule.query.vo.ScheduleMembershipNoVO;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.BasicException;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleQueryService.class);

    private final MembershipUtil membershipUtil;

    private final ScheduleMapper scheduleMapper;

    private final PlanQueryService planQueryService;

    private final Validator validator;


    @Autowired
    public ScheduleQueryService (MembershipUtil membershipUtil,
                                 ScheduleMapper scheduleMapper,
                                 PlanQueryService planQueryService,
                                 Validator validator) {
        this.membershipUtil = membershipUtil;
        this.scheduleMapper = scheduleMapper;
        this.planQueryService = planQueryService;
        this.validator = validator;

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
}
