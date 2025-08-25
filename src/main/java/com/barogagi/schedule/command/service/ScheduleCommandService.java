package com.barogagi.schedule.command.service;

import com.barogagi.ai.client.AIClient;
import com.barogagi.ai.dto.AIReqDTO;
import com.barogagi.ai.dto.AIReqWrapper;
import com.barogagi.ai.dto.AIResDTO;
import com.barogagi.kakaoplace.client.KakaoPlaceClient;
import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.naverbolg.client.NaverBlogClient;
import com.barogagi.naverbolg.dto.NaverBlogResDTO;
import com.barogagi.plan.dto.PlanRegistReqDTO;
import com.barogagi.plan.query.mapper.CategoryMapper;
import com.barogagi.region.dto.RegionRegistReqDTO;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.tag.query.service.TagQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import static com.barogagi.util.HtmlUtils.stripHtml;

@Service
public class ScheduleCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleCommandService.class);

    private final CategoryMapper categoryMapper;
    private final KakaoPlaceClient kakaoPlaceClient;
    private final NaverBlogClient naverBlogClient;
    private final AIClient aiClient;

    private final TagQueryService tagQueryService;



    @Value("${naver.display}")
    private int naverBlogDisplay;

    @Autowired
    public ScheduleCommandService(CategoryMapper categoryMapper,
                                  KakaoPlaceClient kakaoPlaceClient, NaverBlogClient naverBlogClient,
                                  AIClient aiClient, TagQueryService tagQueryService) {
        this.categoryMapper = categoryMapper;
        this.kakaoPlaceClient = kakaoPlaceClient;
        this.naverBlogClient = naverBlogClient;
        this.aiClient = aiClient;
        this.tagQueryService = tagQueryService;
    }

    public void registSchedule(ScheduleRegistReqDTO scheduleRegistReqDTO) {

        List<PlanRegistReqDTO> planRegistReqDTOList = scheduleRegistReqDTO.getPlanRegistReqDTOList();
        List<List<KakaoPlaceResDTO>> allKakaoPlaceResults = new ArrayList<>();

        List<List<NaverBlogResDTO>> allNaverBlogResults = new ArrayList<>();


        int radius = scheduleRegistReqDTO.getRadius();


        for (PlanRegistReqDTO plan : planRegistReqDTOList) {
            // 1. 카카오 API로 추천 장소 리스트 조회
            String queryString = categoryMapper.selectCategoryNmBy(plan.getCategoryNum()); // TODO. 검색 쿼리 고려 필요
            List<KakaoPlaceResDTO> kakaoPlaceResDTOList = new ArrayList<>();

            if (plan.getRegionRegistReqDTOList() != null && !plan.getRegionRegistReqDTOList().isEmpty()) {

                // 후보지역 수에 따라 각 지역의 후보장소 수를 조정
                int limitPlace = calLimitPlace(plan.getRegionRegistReqDTOList().size());

                for (RegionRegistReqDTO region : plan.getRegionRegistReqDTOList()) {
                    String x = region.getX();
                    String y = region.getY();
                    kakaoPlaceResDTOList = kakaoPlaceClient.searchKakaoPlace(queryString, x, y, radius, limitPlace);
                    logger.info("kakaoPlaceResDTOList: {}", kakaoPlaceResDTOList);
                }
            }
            // 2. 네이버 블로그 API로 추천 장소에 대한 정보 조회
            List<NaverBlogResDTO> naverBlogResDTO = new ArrayList<>();
            for(KakaoPlaceResDTO kakaoPlace : kakaoPlaceResDTOList) {
                String query = kakaoPlace.getPlaceName() + " " + kakaoPlace.getRoadAddressName();  // TODO. 검색 쿼리 고려 필요
                naverBlogResDTO = naverBlogClient.searchNaverBlog(query, naverBlogDisplay);
                logger.info("naverBlogResDTO: {}", naverBlogResDTO);
                allNaverBlogResults.add(naverBlogResDTO);
            }

            // 3. AI API로 최종 추천 장소 조회

            // 3-1. 태그 이름 조회
            List<Integer> tagNums = scheduleRegistReqDTO.getPlanRegistReqDTOList().stream()
                    .filter(Objects::nonNull)
                    .flatMap(p -> p.getTagList().stream())
                    .collect(Collectors.toList());
            logger.info("#$# tagNums size={}, values={}", tagNums.size(), tagNums);
            List<String> tagNames = tagQueryService.findTagNmByTagNum(tagNums);
            logger.info("#$# tagNames size={}, values={}", tagNames.size(), tagNames);

            // 3-2. 네이버 블로그 결과를 title/description으로 변환
            List<AIReqDTO> placeList = allNaverBlogResults.stream()
                    .flatMap(List::stream)
                    .map(b -> AIReqDTO.builder()
                            .title(stripHtml(b.getTitle()))
                            .description(stripHtml(b.getDescription()))
                            .build())
                    .collect(Collectors.toList());
            logger.info("#$# placeList size={}, sample={}", placeList.size(),
                    placeList.stream().limit(3).collect(Collectors.toList()));

            // 3-3. AI 요청 래퍼 구성
            AIReqWrapper aiReqWrapper = AIReqWrapper.builder()
                    .tags(tagNames)
                    .comment(Optional.ofNullable(scheduleRegistReqDTO.getComment()).orElse(""))
                    .placeList(placeList)
                    .build();

            logger.info("#$# AIReqWrapper ready: tags={}, comment.len={}, placeList.size={}",
                    aiReqWrapper.getTags(),
                    aiReqWrapper.getComment() == null ? 0 : aiReqWrapper.getComment().length(),
                    aiReqWrapper.getPlaceList().size());

            // 4) AI 호출
            AIResDTO aiRes = aiClient.recommandPlace(aiReqWrapper);
            logger.info("#$# AI Recommendation result obj={}", aiRes);
            if (aiRes != null) {
                logger.info("#$# AI result fields: recommandPlaceNum={}, aiDescription={}",
                        aiRes.getRecommandPlaceNum(), aiRes.getAiDescription());
            }



        }
        logger.info("allNaverBlogResults: {}", allNaverBlogResults);




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
