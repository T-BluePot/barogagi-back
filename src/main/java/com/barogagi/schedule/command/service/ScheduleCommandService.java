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
import com.barogagi.plan.dto.PlanRegistResDTO;
import com.barogagi.plan.query.mapper.CategoryMapper;
import com.barogagi.region.dto.RegionRegistReqDTO;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.schedule.dto.ScheduleRegistResDTO;
import com.barogagi.tag.query.service.TagQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
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

    @Transactional
    public ScheduleRegistResDTO registSchedule(ScheduleRegistReqDTO scheduleRegistReqDTO) {

        List<PlanRegistResDTO> planResList = new ArrayList<>();

        // 스케줄 공통 정보
        String scheduleNm = scheduleRegistReqDTO.getScheduleNm();
        String startDate  = scheduleRegistReqDTO.getStartDate();
        String endDate    = scheduleRegistReqDTO.getEndDate();

        for (PlanRegistReqDTO plan : scheduleRegistReqDTO.getPlanRegistReqDTOList()) {
            int radius = scheduleRegistReqDTO.getRadius();

            // ---------- 1) Kakao 후보장소 수집(평탄화) ----------
            List<List<KakaoPlaceResDTO>> allKakaoPlaceResults = new ArrayList<>();
            String queryString = categoryMapper.selectCategoryNmBy(plan.getCategoryNum()); // 검색어

            if (plan.getRegionRegistReqDTOList() == null || plan.getRegionRegistReqDTOList().isEmpty()) {
                // 지역이 없으면 스킵
                logger.info("#$# skip: plan has no regions. plan={}", plan);
                continue;
            }

            int limitPlace = calLimitPlace(plan.getRegionRegistReqDTOList().size());
            for (RegionRegistReqDTO region : plan.getRegionRegistReqDTOList()) {
                List<KakaoPlaceResDTO> oneRegionPlaces =
                        kakaoPlaceClient.searchKakaoPlace(queryString, region.getX(), region.getY(), radius, limitPlace);
                allKakaoPlaceResults.add(oneRegionPlaces);
            }
            logger.info("allKakaoPlaceResults: {}", allKakaoPlaceResults);

            // Kakao 평탄화(이 순서를 기준으로 이후 Naver/AI도 동일하게 맞춤)
            List<KakaoPlaceResDTO> flatKakao = allKakaoPlaceResults.stream()
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            if (flatKakao.isEmpty()) {
                logger.info("#$# no kakao results. plan={}", plan);
                continue;
            }

            // ---------- 2) Naver 블로그로 title/description 만들기 ----------
            List<NaverBlogResDTO> allBlogsFlat = new ArrayList<>();
            for (KakaoPlaceResDTO k : flatKakao) {
                String query = k.getPlaceName() + " " + k.getRoadAddressName();
                List<NaverBlogResDTO> blogs = naverBlogClient.searchNaverBlog(query, naverBlogDisplay);
                if (blogs != null) {
                    allBlogsFlat.addAll(blogs);
                }
            }
            logger.info("allNaverBlogResults.size={}", allBlogsFlat.size());

            // AI placeList: Kakao 후보 1:1이 가장 안전하지만 현재는 blog기반으로 작성
            // 블로그가 없을 때를 대비해서 Kakao 기본 설명을 fallback으로 변경
            List<AIReqDTO> placeList = new ArrayList<>();
            for (int i = 0; i < flatKakao.size(); i++) {
                KakaoPlaceResDTO k = flatKakao.get(i);

                // 대응되는 블로그가 없다면 간단한 설명을 생성(fallback)
                String title = k.getPlaceName();
                String desc  = Optional.ofNullable(k.getCategoryGroupName()).orElse("카테고리 정보 없음")
                        + " · " + Optional.ofNullable(k.getRoadAddressName()).orElse(k.getAddressName());

                // 블로그 결과가 있다면 맨 앞 하나만 사용(원한다면 점수화/요약 로직 확장)
                if (i < allBlogsFlat.size()) {
                    NaverBlogResDTO b = allBlogsFlat.get(i);
                    title = stripHtml(b.getTitle());
                    desc  = stripHtml(b.getDescription());
                }

                placeList.add(AIReqDTO.builder()
                        .title(title)
                        .description(desc)
                        .build());
            }

            // ---------- 3) AI 호출 ----------
            List<Integer> tagNums = Optional.ofNullable(plan.getTagList()).orElseGet(List::of);
            List<String> tagNames = tagQueryService.findTagNmByTagNum(tagNums);

            AIReqWrapper aiReqWrapper = AIReqWrapper.builder()
                    .tags(tagNames)
                    .comment(Optional.ofNullable(scheduleRegistReqDTO.getComment()).orElse(""))
                    .placeList(placeList)
                    .build();

            AIResDTO aiRes = aiClient.recommandPlace(aiReqWrapper);
            logger.info("#$# AI Recommendation result obj={}", aiRes);

            // ---------- 4) AI가 고른 index → Kakao place 선택 ----------
            Integer idx = (aiRes != null) ? aiRes.getRecommandPlaceIndex() : null;
            if (idx == null || idx < 0 || idx >= flatKakao.size()) {
                logger.warn("#$# invalid recommandPlaceNum={}, fallback to 0", idx);
                idx = 0; // fallback
            }
            KakaoPlaceResDTO chosen = flatKakao.get(idx);

            // ---------- 5) DB insert (예시) ----------
            // Plan 엔티티는 프로젝트 엔티티에 맞게 매핑 필요
            // Plan planEntity = Plan.builder()
            //         .planNm(chosen.getPlaceName())
            //         .planLink(chosen.getPlaceUrl())
            //         .planDescription(aiRes != null ? aiRes.getAiDescription() : null)
            //         .planAddress(Optional.ofNullable(chosen.getRoadAddressName()).orElse(chosen.getAddressName()))
            //         .itemNum(plan.getItemNum())
            //         .categoryNum(plan.getCategoryNum())
            //         .startTime(plan.getStartTime())
            //         .endTime(plan.getEndTime())
            //         .build();
            // planRepository.save(planEntity);

            // ---------- 6) PlanRegistResDTO 구성 ----------
            PlanRegistResDTO planRes = PlanRegistResDTO.builder()
                    .planNum(null) // TODO. DB 저장 후 세팅
                    .startTime(plan.getStartTime())
                    .endTime(plan.getEndTime())
                    .itemNum(plan.getItemNum())
                    .itemNm(null) // TODO. itemNm(plan.getItemNm())
                    .categoryNum(plan.getCategoryNum())
                    .categoryNm(null)// TODO. categoryNm(plan.getCategoryNm())
                    .planNm(chosen.getPlaceName())
                    .planLink(chosen.getPlaceUrl())
                    .planDescription(aiRes != null ? aiRes.getAiDescription() : null)
                    .planAddress(Optional.ofNullable(chosen.getRoadAddressName()).orElse(chosen.getAddressName()))
                    .regionName(null) // TODO. 지역 세팅 - firstRegionName(plan.getRegionRegistReqDTOList())
                    .tagList(plan.getTagList())
                    .build();

            planResList.add(planRes);

        }

        // ---------- 7) ScheduleRegistResDTO 묶어서 리턴 ----------
        return ScheduleRegistResDTO.builder()
                .scheduleNm(scheduleNm)
                .startDate(startDate)
                .endDate(endDate)
                .planRegistResDTOList(planResList)
                .build();
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

//    private String firstRegionName(List<RegionRegistReqDTO> regions) {
//        if (regions == null || regions.isEmpty()) return null;
//        // RegionRegistReqDTO에 regionName 같은 필드가 있다면 그걸 반환
//        // 예시로 cityName+district 조합 등을 사용
//        return Optional.ofNullable(regions.get(0).getRegionName()).orElse(null);
//    }
}
