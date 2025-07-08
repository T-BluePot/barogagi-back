package com.barogagi.schedule.command.service;

import com.barogagi.approval.mapper.ApprovalMapper;
//import com.barogagi.kakaoplace.client.KakaoPlaceClient;
//import com.barogagi.plan.dto.PlanRegistReqDTO;
//import com.barogagi.plan.query.mapper.CategoryMapper;
//import com.barogagi.schedule.dto.KakaoPlaceReqDTO;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.schedule.query.service.ScheduleQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleCommandService {
//    private final CategoryMapper categoryMapper;
//    private final KakaoPlaceClient kakaoPlaceClient;

//    @Autowired
//    public ScheduleCommandService(CategoryMapper categoryMapper,
//                                  KakaoPlaceClient kakaoPlaceClient){
//        this.categoryMapper = categoryMapper;
//        this.kakaoPlaceClient = kakaoPlaceClient;
//    }

    public void registSchedule(ScheduleRegistReqDTO scheduleRegistReqDTO) {

//        List<PlanRegistReqDTO> planRegistReqDTOList = scheduleRegistReqDTO.getPlanRegistReqDTOList();
//        KakaoPlaceReqDTO kakaoPlaceReqDTO;
//
//        // NaverBlogReqDTO naverBlogReqDTO;
//
//        int radious = scheduleRegistReqDTO.getRadius();
//        int resultSize = 10;
//
//        for (PlanRegistReqDTO plan : planRegistReqDTOList) {
//            String queryString = categoryMapper.selectCategoryNmBy(plan.getCategoryNum()); // TODO. 검색 쿼리 고려 필요
//            String x = plan.getRegionRegistReqDTO().getX();
//            String y = plan.getRegionRegistReqDTO().getY();
//
//            kakaoPlaceClient.searchKakaoPlace(queryString, x, y, radious);
//        }
    }
}
