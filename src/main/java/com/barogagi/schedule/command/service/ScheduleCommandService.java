package com.barogagi.schedule.command.service;

import com.barogagi.approval.mapper.ApprovalMapper;
import com.barogagi.kakaoplace.client.KakaoPlaceClient;
import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.member.join.controller.JoinController;
import com.barogagi.plan.dto.PlanRegistReqDTO;
import com.barogagi.plan.query.mapper.CategoryMapper;
import com.barogagi.region.dto.RegionRegistReqDTO;
import com.barogagi.schedule.dto.KakaoPlaceReqDTO;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.schedule.query.service.ScheduleQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleCommandService.class);

    private final CategoryMapper categoryMapper;
    private final KakaoPlaceClient kakaoPlaceClient;


    @Autowired
    public ScheduleCommandService(CategoryMapper categoryMapper,
                                  KakaoPlaceClient kakaoPlaceClient){
        this.categoryMapper = categoryMapper;
        this.kakaoPlaceClient = kakaoPlaceClient;
    }

    public void registSchedule(ScheduleRegistReqDTO scheduleRegistReqDTO) {

        List<PlanRegistReqDTO> planRegistReqDTOList = scheduleRegistReqDTO.getPlanRegistReqDTOList();
        List<List<KakaoPlaceResDTO>> allKakaoPlaceResults = new ArrayList<>();

        // NaverBlogReqDTO naverBlogReqDTO;

        int radious = scheduleRegistReqDTO.getRadius();
        int resultSize = 10;


        // 1. 카카오 API로 추천 장소 리스트 조회
        for (PlanRegistReqDTO plan : planRegistReqDTOList) {
            String queryString = categoryMapper.selectCategoryNmBy(plan.getCategoryNum()); // TODO. 검색 쿼리 고려 필요
            if (plan.getRegionRegistReqDTOList() != null && !plan.getRegionRegistReqDTOList().isEmpty()) {

                // 후보지역 수에 따라 각 지역의 후보장소 수를 조정
                int limitPlace = calLimitPlace(plan.getRegionRegistReqDTOList().size());

                for (RegionRegistReqDTO region : plan.getRegionRegistReqDTOList()) {
                    String x = region.getX();
                    String y = region.getY();
                    List<KakaoPlaceResDTO> kakaoPlaceResDTOList = kakaoPlaceClient.searchKakaoPlace(queryString, x, y, radious, limitPlace);
                    logger.info("kakaoPlaceResDTOList: {}", kakaoPlaceResDTOList);
                }
            }
        }

        // 2. 네이버 블로그 API로 추천 장소에 대한 정보 조회


        // 3. AI API로 최종 추천 장소 조회

    }

    // 후보지역 수에 따라 각 지역의 후보장소 수를 리턴
    // 후보장소 수만큼 네이버 블로그 API를 호출해야 하기 때문에 제한 필요
    private int calLimitPlace(int regionCount) {
        int perRegionLimit;
        if (regionCount == 1) {
            perRegionLimit = 5;
        } else if (regionCount == 2) {
            perRegionLimit = 3;
        } else {
            perRegionLimit = 2;
        }
        return perRegionLimit;
    }
}
