package com.barogagi.schedule.command.service;

import com.barogagi.ai.client.AIClient;
import com.barogagi.ai.dto.AIReqDTO;
import com.barogagi.ai.dto.AIReqWrapper;
import com.barogagi.ai.dto.AIResDTO;
import com.barogagi.config.exception.BusinessException;
import com.barogagi.kakaoplace.client.KakaoPlaceClient;
import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.plan.command.entity.Item;
import com.barogagi.plan.command.entity.Plan;
import com.barogagi.plan.command.ex_entity.PlanUserMembershipInfo;
import com.barogagi.plan.command.repository.ItemRepository;
import com.barogagi.plan.command.repository.PlanRepository;
import com.barogagi.plan.command.repository.PlanTagRepository;
import com.barogagi.plan.dto.PlanRegistReqDTO;
import com.barogagi.plan.dto.PlanRegistResDTO;
import com.barogagi.plan.dto.UserAddedPlaceDTO;
import com.barogagi.plan.enums.PLAN_SOURCE;
import com.barogagi.category.query.mapper.CategoryMapper;
import com.barogagi.item.query.mapper.ItemMapper;
import com.barogagi.region.command.entity.Place;
import com.barogagi.region.command.entity.PlanRegion;
import com.barogagi.region.command.entity.PlanRegionId;
import com.barogagi.region.command.entity.Region;
import com.barogagi.region.command.repository.PlaceRepository;
import com.barogagi.region.command.repository.PlanRegionRepository;
import com.barogagi.region.command.repository.RegionRepository;
import com.barogagi.region.dto.RegionGeoCodeResDTO;
import com.barogagi.region.dto.RegionRegistReqDTO;
import com.barogagi.region.query.service.RegionGeoCodeService;
import com.barogagi.region.query.service.RegionQueryService;
import com.barogagi.region.query.vo.RegionDetailVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.command.entity.Schedule;
import com.barogagi.schedule.command.repository.ScheduleRepository;
import com.barogagi.schedule.dto.ScheduleRegistReqDTO;
import com.barogagi.schedule.dto.ScheduleRegistResDTO;
import com.barogagi.schedule.exception.ScheduleException;
import com.barogagi.tag.command.entity.*;
import com.barogagi.tag.command.repository.ScheduleTagRepository;
import com.barogagi.tag.command.repository.TagRepository;
import com.barogagi.tag.dto.TagRegistReqDTO;
import com.barogagi.tag.dto.TagRegistResDTO;
import com.barogagi.tag.query.service.TagQueryService;
import com.barogagi.tavily.client.TavilyClient;
import com.barogagi.tavily.dto.TavilyResultDTO;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.BasicException;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ScheduleCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleCommandService.class);

    private final CategoryMapper categoryMapper;
    private final ItemMapper itemMapper;
    private final KakaoPlaceClient kakaoPlaceClient;
    private final TavilyClient tavilyClient;
    private final AIClient aiClient;

    private final TagQueryService tagQueryService;
    private final RegionGeoCodeService regionGeoCodeService;

    private final ScheduleRepository scheduleRepository;
    private final ScheduleTagRepository scheduleTagRepository;
    private final TagRepository tagRepository;
    private final ItemRepository itemRepository;
    private final PlanRepository planRepository;
    private final PlanTagRepository planTagRepository;
    private final RegionRepository regionRepository;
    private final PlanRegionRepository planRegionRepository;
    private final PlaceRepository placeRepository;
    private final RegionQueryService regionQueryService ;

    private final Validator validator;
    private final MembershipUtil membershipUtil;

    @Value("${kakao.radius}")
    private int radius;

    @Value("${naver.display}")
    private int naverBlogDisplay;

    @Autowired
    public ScheduleCommandService(CategoryMapper categoryMapper, ItemMapper itemMapper,
                                  KakaoPlaceClient kakaoPlaceClient, TavilyClient tavilyClient,
                                  AIClient aiClient, TagQueryService tagQueryService, RegionGeoCodeService regionGeoCodeService,
                                  ScheduleRepository scheduleRepository, ScheduleTagRepository scheduleTagRepository,
                                  TagRepository tagRepository, ItemRepository itemRepository,
                                  PlanRepository planRepository, PlanTagRepository planTagRepository,
                                  RegionRepository regionRepository, PlanRegionRepository planRegionRepository,
                                  PlaceRepository placeRepository, RegionQueryService regionQueryService,
                                  Validator validator, MembershipUtil membershipUtil) {
        this.itemMapper = itemMapper;
        this.categoryMapper = categoryMapper;
        this.kakaoPlaceClient = kakaoPlaceClient;
        this.tavilyClient = tavilyClient;
        this.aiClient = aiClient;
        this.tagQueryService = tagQueryService;
        this.regionGeoCodeService = regionGeoCodeService;
        this.scheduleRepository = scheduleRepository;
        this.scheduleTagRepository = scheduleTagRepository;
        this.tagRepository = tagRepository;
        this.itemRepository = itemRepository;
        this.planRepository = planRepository;
        this.planTagRepository = planTagRepository;
        this.regionRepository = regionRepository;
        this.planRegionRepository = planRegionRepository;
        this.placeRepository = placeRepository;
        this.regionQueryService = regionQueryService;
        this.validator = validator;
        this.membershipUtil = membershipUtil;
    }



    public ApiResponse createSchedule(ScheduleRegistReqDTO scheduleRegistReqDTO, HttpServletRequest request) {

        try {
            // 1. API SECRET KEY 일치 여부 확인
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                throw new ScheduleException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
            }

            // 2. 회원번호 조회
            Map<String, Object> membershipInfo = membershipUtil.membershipNoService(request);
            if (!membershipInfo.get("resultCode").equals("A200")) {
                return ApiResponse.error(ErrorCode.NOT_EXIST_ACCESS_AUTH.getCode(), ErrorCode.NOT_EXIST_ACCESS_AUTH.getMessage());
            }

            // 입력값 검증
            // 날짜 형식 검증
            List<PlanRegistResDTO> planResList = new ArrayList<>();

            // 스케줄 공통 정보
            String scheduleNm = scheduleRegistReqDTO.getScheduleNm();
            String startDate = scheduleRegistReqDTO.getStartDate();
            String endDate = scheduleRegistReqDTO.getEndDate();

            for (PlanRegistReqDTO plan : scheduleRegistReqDTO.getPlanRegistReqDTOList()) {

                if (plan.getIsUserAdded().equals("Y")) {
                    // ➜ A. 사용자가 직접 입력한 플랜
                    logger.info("A. 사용자가 직접 입력한 플랜 plan={}", plan);
                    PlanRegistResDTO planRes = handleUserPlan(plan);
                    planResList.add(planRes);

                } else {
                    // ➜ B. AI가 추천해줘야 하는 플랜
                    logger.info("B. AI가 추천해줘야 하는 플랜 plan={}", plan);
                    PlanRegistResDTO planRes = handleAIPlan(scheduleRegistReqDTO, plan);
                    planResList.add(planRes);
                }
            }

            // ---------- 6) ScheduleRegistResDTO 묶어서 리턴 ----------
            ScheduleRegistResDTO result = ScheduleRegistResDTO.builder()
                    .scheduleNm(scheduleNm)
                    .startDate(startDate)
                    .endDate(endDate)
                    .planRegistResDTOList(planResList)
                    .build();

            return ApiResponse.resultData(
                    result,
                    ErrorCode.SUCCESS_SCHEDULE_CREATE.getCode(),
                    ErrorCode.SUCCESS_SCHEDULE_CREATE.getMessage());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }

    /**
     * AI 기반 플랜 생성 처리.
     *
     * Tavily 웹 검색 → AI 장소명 추출 → 카카오 장소 상세 조회 → AI 추천 순서로 진행하여
     * 최종 추천 장소를 선정하고 플랜 응답 DTO를 생성한다.
     *
     * 처리 흐름:
     * 1. 지역 결정 - plan 지역이 있으면 그대로 사용, 없으면 schedule 지역 중 랜덤 1개 선택
     * 2. 카테고리 결정 - 랜덤 카테고리 옵션 적용 및 카테고리명 조회
     * 3. 지역별 Tavily 웹 검색으로 장소 관련 웹 콘텐츠 수집
     * 4. AI 1차 호출 - Tavily content에서 실제 장소명 추출
     * 5. 추출된 장소명으로 Kakao 장소 검색하여 정확한 주소/URL 획득
     * 6. AI 2차 호출 - 카카오 장소 목록에서 추천 장소 선정 + 한줄 소개 생성
     * 7. 추천 장소 기반 응답 DTO 생성
     *
     * @param scheduleRegistReqDTO 일정 등록 요청 DTO (comment, 지역 리스트 등 포함)
     * @param plan                 플랜 등록 요청 DTO (카테고리, 태그, 지역 등 포함)
     * @return 생성된 플랜 응답 DTO
     * @throws ScheduleException ErrorCode.REGION_NOT_FOUND           – plan과 schedule 모두 지역이 없을 때
     * @throws ScheduleException ErrorCode.NOT_FOUND_CATEGORY         – 카테고리 번호 조회 실패 또는 카테고리명이 없을 때
     * @throws ScheduleException ErrorCode.PLACE_SEARCH_EMPTY         – 장소 검색 결과가 없을 때
     * @throws ScheduleException ErrorCode.AI_RECOMMENDATION_FAILED   – AI 추천 응답이 null일 때
     */
    private PlanRegistResDTO handleAIPlan(ScheduleRegistReqDTO scheduleRegistReqDTO, PlanRegistReqDTO plan) {

        // ---------- 0) 지역 결정: plan 지역 우선, 없으면 schedule 지역에서 랜덤 ----------
        List<RegionRegistReqDTO> resolvedRegions = resolveRegions(plan, scheduleRegistReqDTO);

        int limitPlace = calLimitPlace(resolvedRegions.size());

        // ---------- 1) 카테고리 결정 ----------
        if ("Y".equals(plan.getIsRandomCategory())) {
            Integer randomCategoryNum = categoryMapper.selectRandomCategoryNum();
            if (randomCategoryNum == null) {
                throw new ScheduleException(ErrorCode.NOT_FOUND_CATEGORY);
            }
            plan = plan.toBuilder()
                    .categoryNum(randomCategoryNum)
                    .build();
            logger.info("getCategoryNum={}", plan.getCategoryNum());
        }

        String categoryNm = categoryMapper.selectCategoryNmBy(plan.getCategoryNum());
        if (categoryNm == null || categoryNm.isBlank()) {
            throw new ScheduleException(ErrorCode.NOT_FOUND_CATEGORY);
        }

        String itemNm = itemMapper.selectItemNmBy(plan.getItemNum());

        // 태그 정보 미리 조회 (장소명 추출 + 추천에서 모두 사용)
        List<Integer> tagNums = Optional.ofNullable(plan.getPlanTagRegistReqDTOList())
                .orElseGet(List::of)
                .stream()
                .map(TagRegistReqDTO::getTagNum)
                .collect(Collectors.toList());

        List<String> tagNames = tagQueryService.findTagNmByTagNum(tagNums);

        // ---------- 2) Tavily 웹 검색 → AI 장소명 추출 → 카카오 상세 검색 ----------
        List<KakaoPlaceResDTO> flatKakao = new ArrayList<>();

        for (RegionRegistReqDTO region : resolvedRegions) {
            RegionGeoCodeResDTO geo = regionGeoCodeService.getGeocode(region.getRegionNum());
            if (geo == null) {
                logger.warn("regionNum={} 좌표 조회 실패, skip.", region.getRegionNum());
                continue;
            }

            RegionRegistReqDTO updatedRegion = region.toBuilder()
                    .regionLevel1(geo.getRegionLevel1())
                    .regionLevel2(geo.getRegionLevel2())
                    .regionLevel3(geo.getRegionLevel3())
                    .regionLevel4(geo.getRegionLevel4())
                    .build();

            int idx = resolvedRegions.indexOf(region);
            resolvedRegions.set(idx, updatedRegion);

            String regionName = pickRegionName(updatedRegion);

            // Step 1: Tavily 웹 검색 (태그도 검색어에 포함)
            String tagKeyword = tagNames.isEmpty() ? "" : " " + tagNames.get(0);
            String tavilyQuery = categoryNm + tagKeyword + " " + regionName + " 추천 장소";
            List<TavilyResultDTO> tavilyResults = tavilyClient.search(tavilyQuery, limitPlace);

            logger.info("tavily search: query={}, resultSize={}", tavilyQuery, tavilyResults.size());

            if (tavilyResults.isEmpty()) {
                logger.warn("Tavily 검색 결과 없음. query={}", tavilyQuery);
                continue;
            }

            // Step 2: Tavily content를 합쳐서 AI에게 장소명 추출 요청
            String combinedContent = tavilyResults.stream()
                    .map(TavilyResultDTO::getContent)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.joining("\n"));

            List<String> extractedPlaceNames = aiClient.extractPlaceNames(
                    combinedContent, categoryNm, regionName, tagNames, limitPlace);

            logger.info("AI 장소명 추출: category={}, region={}, extracted={}",
                    categoryNm, regionName, extractedPlaceNames);

            if (extractedPlaceNames == null || extractedPlaceNames.isEmpty()) {
                logger.warn("AI 장소명 추출 결과 없음. category={}, region={}", categoryNm, regionName);
                continue;
            }

            // Step 3: 추출된 장소명 → 카카오 검색 (매칭 성공한 것만 사용)
            for (String placeName : extractedPlaceNames) {
                KakaoPlaceResDTO matched = searchKakaoWithRetry(
                        placeName, regionName, geo.getX(), geo.getY());

                if (matched != null) {
                    matched.setRegionNum(region.getRegionNum());
                    flatKakao.add(matched);
                    logger.info("카카오 매칭 성공: placeName={}, placeUrl={}",
                            matched.getPlaceName(), matched.getPlaceUrl());
                } else {
                    logger.info("카카오 매칭 실패, skip. placeName={}", placeName);
                }
            }

            logger.info("resolved regionName={} for regionNum={}", regionName, updatedRegion.getRegionNum());
        }

        if (flatKakao.isEmpty()) {
            throw new ScheduleException(ErrorCode.PLACE_SEARCH_EMPTY);
        }

        // ---------- 3) AI 2차 호출: 추천 장소 선정 + 한줄 소개 ----------
        List<AIReqDTO> placeList = flatKakao.stream()
                .map(k -> AIReqDTO.builder()
                        .title(k.getPlaceName())
                        .description(
                                "카테고리: " + Optional.ofNullable(k.getCategoryGroupName()).orElse(categoryNm)
                                        + " / 주소: " + Optional.ofNullable(k.getRoadAddressName())
                                        .orElse(Optional.ofNullable(k.getAddressName()).orElse("주소 없음")))
                        .build())
                .collect(Collectors.toList());

        AIReqWrapper aiReqWrapper = AIReqWrapper.builder()
                .tags(tagNames)
                .comment(Optional.ofNullable(scheduleRegistReqDTO.getComment()).orElse(""))
                .placeList(placeList)
                .build();

        AIResDTO aiRes = aiClient.recommandPlace(aiReqWrapper);
        if (aiRes == null) {
            throw new ScheduleException(ErrorCode.AI_RECOMMENDATION_FAILED);
        }

        // ---------- 4) AI가 고른 index → place 선택 ----------
        int chosenIdx = aiRes.getRecommandPlaceIndex();
        if (chosenIdx < 0 || chosenIdx >= flatKakao.size()) {
            logger.warn("AI 추천 인덱스 유효하지 않음(idx={}), fallback=0", chosenIdx);
            chosenIdx = 0;
        }
        KakaoPlaceResDTO aiChosen = flatKakao.get(chosenIdx);

        // ---------- 5) 응답 DTO 생성 ----------
        String regionNm = resolveRegionNm(aiChosen.getRegionNum());

        return PlanRegistResDTO.builder()
                .planSource(PLAN_SOURCE.AI)
                .startTime(plan.getStartTime())
                .endTime(plan.getEndTime())
                .planNm(aiChosen.getPlaceName())
                .planLink(aiChosen.getPlaceUrl())
                .planDescription(aiRes.getAiDescription())
                .planAddress(Optional.ofNullable(aiChosen.getRoadAddressName()).orElse(aiChosen.getAddressName()))
                .regionNm(regionNm)
                .regionNum(aiChosen.getRegionNum())
                .categoryNm(categoryNm)
                .categoryNum(plan.getCategoryNum())
                .itemNm(itemNm)
                .itemNum(plan.getItemNum())
                .planTagRegistResDTOList(
                        Optional.ofNullable(plan.getPlanTagRegistReqDTOList())
                                .orElseGet(List::of)
                                .stream()
                                .map(tagReq -> TagRegistResDTO.builder()
                                        .tagNum(tagReq.getTagNum())
                                        .tagNm(tagReq.getTagNm())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }


    /**
     * 카카오 장소 검색 (재시도 포함)
     * 1차: "장소명 + 지역명" 으로 검색
     * 2차: "장소명"만으로 재검색
     * 둘 다 실패 시 null 반환
     */
    private KakaoPlaceResDTO searchKakaoWithRetry(String placeName, String regionName, String x, String y) {
        // 1차: 장소명 + 지역명
        String query1 = placeName + " " + regionName;
        List<KakaoPlaceResDTO> results = kakaoPlaceClient.searchKakaoPlace(query1, x, y, radius, 1);
        logger.info("kakao 1차: query={}, resultSize={}", query1, results != null ? results.size() : "null");

        if (results != null && !results.isEmpty()) {
            return results.get(0);
        }

        // 2차: 장소명만
        List<KakaoPlaceResDTO> retry = kakaoPlaceClient.searchKakaoPlace(placeName, x, y, radius, 1);
        logger.info("kakao 2차: query={}, resultSize={}", placeName, retry != null ? retry.size() : "null");

        if (retry != null && !retry.isEmpty()) {
            return retry.get(0);
        }

        return null;
    }


    private List<RegionRegistReqDTO> resolveRegions(PlanRegistReqDTO plan, ScheduleRegistReqDTO schedule) {
        if (plan.getRegionRegistReqDTOList() != null && !plan.getRegionRegistReqDTOList().isEmpty()) {
            logger.info("plan 자체 지역 사용. size={}", plan.getRegionRegistReqDTOList().size());
            return plan.getRegionRegistReqDTOList();
        }

        List<RegionRegistReqDTO> scheduleRegions = schedule.getScheduleRegionRegistReqDTOList();
        if (scheduleRegions == null || scheduleRegions.isEmpty()) {
            throw new ScheduleException(ErrorCode.REGION_NOT_FOUND);
        }

        RegionRegistReqDTO randomRegion = scheduleRegions.get(
                ThreadLocalRandom.current().nextInt(scheduleRegions.size())
        );
        logger.info("schedule 지역에서 랜덤 선택. regionNum={}", randomRegion.getRegionNum());

        return new ArrayList<>(List.of(randomRegion));
    }

    private String pickRegionName(RegionRegistReqDTO region) {
        if (region.getRegionLevel3() != null && !region.getRegionLevel3().isEmpty())
            return region.getRegionLevel3();
        if (region.getRegionLevel2() != null && !region.getRegionLevel2().isEmpty())
            return region.getRegionLevel2();
        return region.getRegionLevel1();
    }

    private String resolveRegionNm(Integer regionNum) {
        if (regionNum == null) return null;
        return regionRepository.findById(regionNum)
                .map(region -> {
                    if (region.getRegionLevel3() != null && !region.getRegionLevel3().isEmpty())
                        return region.getRegionLevel3();
                    if (region.getRegionLevel2() != null && !region.getRegionLevel2().isEmpty())
                        return region.getRegionLevel2();
                    return region.getRegionLevel1();
                })
                .orElse(null);
    }

    private PlanRegistResDTO handleUserPlan(PlanRegistReqDTO plan) {

        // CASE 1: 카카오 장소 ID로 선택한 경우
        if (plan.getUserAddedPlaceDTO() != null) {
            logger.info("➜ A-1. 사용자가 직접 입력한 플랜 plan={}", plan);
            UserAddedPlaceDTO userAddedPlaceDTO = plan.getUserAddedPlaceDTO();

            RegionDetailVO regionDetailVO = regionQueryService.getRegionNumByAddress(userAddedPlaceDTO.getAddressName());
            String regionNm = resolveRegionName(regionDetailVO);

            logger.info("사용자가 선택한 장소 userAddedPlaceDTO addressName={}, resolved regionNum={}", userAddedPlaceDTO.getAddressName(), regionDetailVO.getRegionNum());

            return PlanRegistResDTO.builder()
                    .planSource(PLAN_SOURCE.USER_PLACE)
                    .startTime(plan.getStartTime())
                    .endTime(plan.getEndTime())
                    .planNm(userAddedPlaceDTO.getPlaceName())
                    .planLink(userAddedPlaceDTO.getPlaceUrl())
                    .planDescription(null) // 사용자가 추가한 일정은 설명 없음
                    .planAddress(userAddedPlaceDTO.getAddressName())
                    .regionNum(regionDetailVO.getRegionNum()) //todo. check
                    .regionNm(regionNm) //todo. check
                    .categoryNum(plan.getCategoryNum())
                    .categoryNm(categoryMapper.selectCategoryNmBy(plan.getCategoryNum()))
                    .itemNum(plan.getItemNum())
                    .itemNm(itemMapper.selectItemNmBy(plan.getItemNum()))
                    .planTagRegistResDTOList(List.of()) // 사용자 일정은 태그 없음
                    .build();
        } else {

            // CASE 2: 텍스트로 직접 입력한 경우
            logger.info("➜ A-2. 사용자가 직접 입력한 플랜 plan={}", plan);

            RegionDetailVO region = regionQueryService.getRegionByRegionNum(plan.getRegionRegistReqDTOList().get(0).getRegionNum());

            return PlanRegistResDTO.builder()
                    .planSource(PLAN_SOURCE.USER_CUSTOM)
                    .startTime(plan.getStartTime())
                    .endTime(plan.getEndTime())
                    .planNm(plan.getPlanNm())
                    .planAddress(null)
                    .planLink(null)
                    .categoryNum(plan.getCategoryNum())
                    .categoryNm(categoryMapper.selectCategoryNmBy(plan.getCategoryNum()))
                    .itemNum(plan.getItemNum())
                    .itemNm(itemMapper.selectItemNmBy(plan.getItemNum()))
                    .regionNum(plan.getRegionRegistReqDTOList().get(0).getRegionNum())
                    .regionNm(region.getRegionLevel3() != null ? region.getRegionLevel3() : region.getRegionLevel2())
                    .planTagRegistResDTOList(List.of()) // 사용자 입력은 태그 없음
                    .build();
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ApiResponse saveSchedule(ScheduleRegistResDTO scheduleRegistResDTO, HttpServletRequest request) {

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

        // todo. 날짜 형식 검증 필요

        logger.info("START DB SAVE!");

        // 1. Schedule
        Schedule schedule = Schedule.builder()
                .membershipNo(membershipNo)
                .scheduleNm(scheduleRegistResDTO.getScheduleNm())
                .startDate(scheduleRegistResDTO.getStartDate())
                .endDate(scheduleRegistResDTO.getEndDate())
                .radius(radius)
                .delYn("N")
                .build();

        scheduleRepository.save(schedule);
        logger.info("schedule Save! scheduleNum={}", schedule.getScheduleNum());

        // 2. Schedule_tag
        if (scheduleRegistResDTO.getScheduleTagRegistResDTOList() != null) {
            logger.info("schedule save result id={}", schedule.getScheduleNum());

            for (TagRegistResDTO tagReq : scheduleRegistResDTO.getScheduleTagRegistResDTOList()) {
                Tag tag = tagRepository.findById(tagReq.getTagNum())
                        .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_TAG));

                scheduleTagRepository.save(
                        ScheduleTag.builder()
                                .id(new ScheduleTagId(tag.getTagNum(), schedule.getScheduleNum()))
                                .membershipNo(membershipNo)
                                .schedule(schedule)
                                .tag(tag)
                                .build()
                );

            }
        }
        logger.info("scheduleTag Save! scheduleNum={}", schedule.getScheduleNum());

        // 3. Plan + Plan_tag + Plan_region + Place
        for (int i = 0; i < scheduleRegistResDTO.getPlanRegistResDTOList().size(); i++) {

            PlanRegistResDTO planRes = scheduleRegistResDTO.getPlanRegistResDTOList().get(i);

            Item item = itemRepository.findById(planRes.getItemNum())
                    .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_ITEM));

            PlanUserMembershipInfo user = PlanUserMembershipInfo.builder()
                    .membershipNo(membershipNo)
                    .build();

            // 3-1. Plan
            Plan plan = Plan.builder()
                    .planNum(planRes.getPlanNum())
                    .planNm(planRes.getPlanNm())
                    .startTime(planRes.getStartTime())
                    .endTime(planRes.getEndTime())
                    .planLink(planRes.getPlanLink())
                    .planDescription(planRes.getPlanDescription())
                    .planAddress(planRes.getPlanAddress())
                    .schedule(schedule)
                    .user(user)
                    .item(item)
                    .delYn("N")
                    .build();

            planRepository.saveAndFlush(plan);
            logger.info("Plan save! planNum={}", plan.getPlanNum());


            // 3-2. Plan_tag
            if (planRes.getPlanTagRegistResDTOList() != null) {

                for (TagRegistResDTO tagRes : planRes.getPlanTagRegistResDTOList()) {
                    Tag tag = tagRepository.findById(tagRes.getTagNum())
                            .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_TAG));

                    PlanTag planTag = PlanTag.builder()
                            .id(new PlanTagId(tag.getTagNum(), plan.getPlanNum()))
                            .plan(plan)
                            .tag(tag)
                            .build();

                    planTagRepository.save(planTag);
                }
            }

            // 3-3. Plan_region
            if (planRes.getRegionNum() != null) {
                Region region = regionRepository.findById(planRes.getRegionNum())
                        .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_REGION));

                PlanRegionId planRegionId = new PlanRegionId(plan.getPlanNum(), region.getRegionNum());

                PlanRegion planRegion = PlanRegion.builder()
                        .id(planRegionId)
                        .region(region)
                        .plan(plan)
                        .build();

                planRegionRepository.save(planRegion);
                logger.info("PlanRegion save! regionNum={}, planNum={}", region.getRegionNum(), plan.getPlanNum());

            }

            // 3-4. Place
            if (planRes.getRegionNum() != null) {
                Region region = regionRepository.findById(planRes.getRegionNum())
                        .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_REGION));

                Place place = Place.builder()
                        .region(region)
                        .regionNm(region.getRegionLevel3() != null ? region.getRegionLevel3() : region.getRegionLevel2())
                        .address(planRes.getPlanAddress())
                        .planLink(planRes.getPlanLink())
                        .placeDescription(planRes.getPlanDescription())
                        .plan(plan)
                        .build();

                placeRepository.save(place);
                logger.info("Place save! planNum={}, regionNum={}", plan.getPlanNum(), planRes.getRegionNum());

                // 3-5. plan-place 동기화
                plan = plan.toBuilder().place(place).build();
                planRepository.save(plan);
            }

        }
        logger.info("END DB SAVE!");

        return ApiResponse.resultData(
                schedule.getScheduleNum(),
                ErrorCode.SUCCESS_SCHEDULE_SAVE.getCode(),
                ErrorCode.SUCCESS_SCHEDULE_SAVE.getMessage());

    }

    @Transactional
    public ApiResponse updateSchedule(ScheduleRegistResDTO dto, HttpServletRequest request) {
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

            // 3. Schedule 조회
            Schedule schedule = scheduleRepository.findByScheduleNumAndMembershipNo(dto.getScheduleNum(), membershipNo)
                    .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_INFO_SCHEDULE));

            // 4. Schedule 기본 정보 업데이트
            schedule.updateBasicInfo(dto.getScheduleNm(), dto.getStartDate(), dto.getEndDate());

            // 5. ScheduleTag 전체 삭제 후 재등록
            scheduleTagRepository.deleteBySchedule(schedule);

            if (dto.getScheduleTagRegistResDTOList() != null) {
                for (TagRegistResDTO tagReq : dto.getScheduleTagRegistResDTOList()) {
                    Tag tag = tagRepository.findById(tagReq.getTagNum())
                            .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_TAG));

                    scheduleTagRepository.save(
                            ScheduleTag.builder()
                                    .id(new ScheduleTagId(tag.getTagNum(), schedule.getScheduleNum()))
                                    .schedule(schedule)
                                    .tag(tag)
                                    .build()
                    );
                }
            }

            // 6. 기존 Plan은 soft delete 처리
            List<Plan> oldPlans = planRepository.findBySchedule(schedule);
            for (Plan p : oldPlans) {
                p.markDeleted();
            }

            // 7. 새 Plan + PlanTag + PlanRegion + Place 저장
            if (dto.getPlanRegistResDTOList() != null) {

                for (PlanRegistResDTO planRes : dto.getPlanRegistResDTOList()) {

                    Item item = itemRepository.findById(planRes.getItemNum())
                            .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_ITEM));

                    PlanUserMembershipInfo user = PlanUserMembershipInfo.builder()
                            .membershipNo(membershipNo)
                            .build();

                    Plan plan = Plan.builder()
                            .planNm(planRes.getPlanNm())
                            .startTime(planRes.getStartTime())
                            .endTime(planRes.getEndTime())
                            .planLink(planRes.getPlanLink())
                            .planDescription(planRes.getPlanDescription())
                            .planAddress(planRes.getPlanAddress())
                            .schedule(schedule)
                            .user(user)
                            .item(item)
                            .delYn("N")
                            .build();

                    planRepository.save(plan);

                    // PlanTag 저장
                    if (planRes.getPlanTagRegistResDTOList() != null) {
                        for (TagRegistResDTO tagRes : planRes.getPlanTagRegistResDTOList()) {
                            Tag tag = tagRepository.findById(tagRes.getTagNum())
                                    .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_TAG));

                            planTagRepository.save(new PlanTag(new PlanTagId(tag.getTagNum(), plan.getPlanNum()), plan, tag));
                        }
                    }

                    // PlanRegion + Place 저장
                    if (planRes.getRegionNum() != null) {
                        Region region = regionRepository.findById(planRes.getRegionNum())
                                .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_REGION));

                        planRegionRepository.save(new PlanRegion(new PlanRegionId(plan.getPlanNum(), region.getRegionNum()), plan, region));

                        Place place = Place.builder()
                                .region(region)
                                .regionNm(region.getRegionLevel3() != null ? region.getRegionLevel3() : region.getRegionLevel2())
                                .address(planRes.getPlanAddress())
                                .planLink(planRes.getPlanLink())
                                .placeDescription(planRes.getPlanDescription())
                                .plan(plan)
                                .build();

                        placeRepository.save(place);
                    }
                }
            }

            return ApiResponse.success(true, "일정 수정 성공");

        } catch (BasicException e) {
            logger.error("일정 수정 실패 - BasicException: {}, Code: {}", e.getMessage(), e.getCode());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("일정 수정 실패 - Unexpected Exception", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }

    @Transactional
    public ApiResponse deleteSchedule(Integer scheduleNum, HttpServletRequest request) {

        // 1. API SECRET KEY 검증
        if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
            throw new BasicException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 회원번호 조회
        Map<String, Object> membershipInfo = membershipUtil.membershipNoService(request);
        if (!membershipInfo.get("resultCode").equals("A200")) {
            throw new BasicException(ErrorCode.NOT_EXIST_ACCESS_AUTH);
        }
        String membershipNo = String.valueOf(membershipInfo.get("membershipNo"));

        // 3. 일정 조회 및 삭제
        Schedule schedule = scheduleRepository.findByScheduleNumAndMembershipNo(scheduleNum, membershipNo)
                .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_INFO_SCHEDULE));

        schedule.markDeleted();

        // 4. 관련 Plan들도 soft delete
        List<Plan> plans = planRepository.findBySchedule(schedule);
        for (Plan plan : plans) {
            plan.markDeleted();
        }
        return ApiResponse.resultData(
                null,
                ErrorCode.SUCCESS_SCHEDULE_DELETE.getCode(),
                ErrorCode.SUCCESS_SCHEDULE_DELETE.getMessage());
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

    private String resolveRegionName(RegionDetailVO region) {
        if (region.getRegionLevel3() != null && !region.getRegionLevel3().isEmpty()) {
            return region.getRegionLevel3();
        } else if (region.getRegionLevel2() != null && !region.getRegionLevel2().isEmpty()) {
            return region.getRegionLevel2();
        } else {
            return region.getRegionLevel1();
        }
    }
}
