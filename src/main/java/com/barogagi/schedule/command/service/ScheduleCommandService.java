package com.barogagi.schedule.command.service;

import com.barogagi.ai.client.AIClient;
import com.barogagi.ai.dto.AIReqDTO;
import com.barogagi.ai.dto.AIReqWrapper;
import com.barogagi.ai.dto.AIResDTO;
import com.barogagi.config.exception.BusinessException;
import com.barogagi.kakaoplace.client.KakaoPlaceClient;
import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.naverblog.client.NaverBlogClient;
import com.barogagi.naverblog.dto.NaverBlogResDTO;
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
import com.barogagi.tag.command.entity.*;
import com.barogagi.tag.command.repository.ScheduleTagRepository;
import com.barogagi.tag.command.repository.TagRepository;
import com.barogagi.tag.dto.TagRegistReqDTO;
import com.barogagi.tag.dto.TagRegistResDTO;
import com.barogagi.tag.query.service.TagQueryService;
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
import java.util.stream.Collectors;

import static com.barogagi.util.HtmlUtils.stripHtml;

@Service
public class ScheduleCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleCommandService.class);

    private final CategoryMapper categoryMapper;
    private final ItemMapper itemMapper;
    private final KakaoPlaceClient kakaoPlaceClient;
    private final NaverBlogClient naverBlogClient;
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
                                  KakaoPlaceClient kakaoPlaceClient, NaverBlogClient naverBlogClient,
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
        this.naverBlogClient = naverBlogClient;
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
            // 1. API SECRET KEY 검증
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(), ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            // 2. 회원번호 조회
            Map<String, Object> membershipInfo = membershipUtil.membershipNoService(request);
            if (!membershipInfo.get("resultCode").equals("A200")) {
                return ApiResponse.error(ErrorCode.NOT_EXIST_ACCESS_AUTH.getCode(), ErrorCode.NOT_EXIST_ACCESS_AUTH.getMessage());
            }

            List<PlanRegistResDTO> planResList = new ArrayList<>();

            // 스케줄 공통 정보
            String scheduleNm = scheduleRegistReqDTO.getScheduleNm();
            String startDate = scheduleRegistReqDTO.getStartDate();
            String endDate = scheduleRegistReqDTO.getEndDate();

            scheduleRegistReqDTO.getPlanRegistReqDTOList().forEach(plan -> {
                if (plan.getPlanTagRegistReqDTOList() != null) {
                    plan.getPlanTagRegistReqDTOList().forEach(tag -> {
                        logger.info("tagNum={}, tagNm={}", tag.getTagNum(), tag.getTagNm());
                    });
                } else {
                    logger.info("태그 없음");
                }
            });

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

            return ApiResponse.success(result, "일정 생성 성공");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }

    private PlanRegistResDTO handleAIPlan(ScheduleRegistReqDTO scheduleRegistReqDTO, PlanRegistReqDTO plan) {
        // ---------- 1) 지역 번호로 x, y 좌표 검색 & Kakao 후보장소 수집(평탄화) ----------
        if (plan.getRegionRegistReqDTOList() == null || plan.getRegionRegistReqDTOList().isEmpty()) {
            logger.info("skip: plan has no regions. plan={}", plan);
            return null;
        }

        int limitPlace = calLimitPlace(plan.getRegionRegistReqDTOList().size());

        List<List<KakaoPlaceResDTO>> allKakaoPlaceResults = new ArrayList<>();

        // 랜덤 카테고리를 선택한 경우, 카테고리 번호를 랜덤으로 선택
        if (plan.getIsRandomCategory().equals("Y")) {
            plan = plan.toBuilder()
                    .categoryNum(categoryMapper.selectRandomCategoryNum())
                    .build();
            logger.info("getCategoryNum={}", plan.getCategoryNum());
        }

        String categoryNm = categoryMapper.selectCategoryNmBy(plan.getCategoryNum());
        String queryString = categoryNm;  // 검색어

        String itemNm = itemMapper.selectItemNmBy(plan.getItemNum()); // todo. itemNm을 검색어로 쓸지 고려하기

        for (RegionRegistReqDTO region : plan.getRegionRegistReqDTOList()) {
            // regionNum으로 좌표 가져오기
            RegionGeoCodeResDTO geo = regionGeoCodeService.getGeocode(region.getRegionNum());
            if (geo == null) {
                logger.warn("regionNum={} not found in DB, skip.", region.getRegionNum());
                continue;
            }

            RegionRegistReqDTO updatedRegion = region.toBuilder()
                    .regionLevel1(geo.getRegionLevel1())
                    .regionLevel2(geo.getRegionLevel2())
                    .regionLevel3(geo.getRegionLevel3())
                    .regionLevel4(geo.getRegionLevel4())
                    .build();

            // 리스트 교체
            int idx = plan.getRegionRegistReqDTOList().indexOf(region);
            plan.getRegionRegistReqDTOList().set(idx, updatedRegion);

            // 지역명 결정 (레벨2/3 우선순위 적용)
            String regionName = null;
            if (updatedRegion.getRegionLevel3() != null && !updatedRegion.getRegionLevel3().isEmpty()) {
                regionName = updatedRegion.getRegionLevel3();
            } else if (updatedRegion.getRegionLevel2() != null && !updatedRegion.getRegionLevel2().isEmpty()) {
                regionName = updatedRegion.getRegionLevel2();
            }

            List<KakaoPlaceResDTO> oneRegionPlaces =
                    kakaoPlaceClient.searchKakaoPlace(queryString, geo.getX(), geo.getY(), radius, limitPlace);
            allKakaoPlaceResults.add(oneRegionPlaces);

            // 각 장소에 regionNum 세팅
            if (oneRegionPlaces != null) {
                oneRegionPlaces.forEach(k -> k.setRegionNum(region.getRegionNum()));
            }

            logger.info("resolved regionName={} for regionNum={}", regionName, updatedRegion.getRegionNum());

        }

        logger.info("allKakaoPlaceResults: {}", allKakaoPlaceResults);


        // Kakao 평탄화(이 순서를 기준으로 이후 Naver/AI도 동일하게 맞춤)
        List<KakaoPlaceResDTO> flatKakao = allKakaoPlaceResults.stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (flatKakao.isEmpty()) {
            logger.info("no kakao results. plan={}", plan);
            return null;
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
        // todo. 일정 전체에 대한 태그(schedulePlanTagRegistReqDTOList)도 참고하도록 수정해야 함
        List<Integer> tagNums = Optional.ofNullable(plan.getPlanTagRegistReqDTOList())
                .orElseGet(List::of)
                .stream()
                .map(TagRegistReqDTO::getTagNum)
                .collect(Collectors.toList());

        List<String> tagNames = tagQueryService.findTagNmByTagNum(tagNums);

        AIReqWrapper aiReqWrapper = AIReqWrapper.builder()
                .tags(tagNames)
                .comment(Optional.ofNullable(scheduleRegistReqDTO.getComment()).orElse(""))
                .placeList(placeList)
                .build();

        AIResDTO aiRes = aiClient.recommandPlace(aiReqWrapper);

        // ---------- 4) AI가 고른 index → Kakao place 선택 ----------
        Integer idx = (aiRes != null) ? aiRes.getRecommandPlaceIndex() : null;
        if (idx == null || idx < 0 || idx >= flatKakao.size()) {
            idx = 0; // fallback
        }
        KakaoPlaceResDTO aiChosen = flatKakao.get(idx);

        // ---------- 5) 응답 DTO 생성 ----------
        String regionNm = null;
        if (aiChosen.getRegionNum() != null) {
            regionNm = regionRepository.findById(aiChosen.getRegionNum())
                    .map(region -> {
                        // 지역명은 보통 3레벨 > 2레벨 순으로 선택
                        if (region.getRegionLevel3() != null && !region.getRegionLevel3().isEmpty())
                            return region.getRegionLevel3();
                        else if (region.getRegionLevel2() != null && !region.getRegionLevel2().isEmpty())
                            return region.getRegionLevel2();
                        else
                            return region.getRegionLevel1();
                    })
                    .orElse(null);
        }

        return PlanRegistResDTO.builder()
                .planSource(PLAN_SOURCE.AI)
                .startTime(plan.getStartTime())
                .endTime(plan.getEndTime())
                .planNm(aiChosen.getPlaceName())
                .planLink(aiChosen.getPlaceUrl())
                .planDescription(aiRes != null ? aiRes.getAiDescription() : null)
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
                // .aiChosen(aiChosen)
                .build();
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

        return ApiResponse.success(schedule.getScheduleNum(), "일정 저장 성공");

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
            Schedule schedule = scheduleRepository.findById(String.valueOf(dto.getScheduleNum()))
                    .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_INFO_SCHEDULE));

            // 4. Schedule 기본 정보 업데이트
            schedule.updateBasicInfo(
                    dto.getScheduleNm(),
                    dto.getStartDate(),
                    dto.getEndDate()
            );

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

                    // 새 Plan 생성
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

                            planTagRepository.save(
                                    new PlanTag(new PlanTagId(tag.getTagNum(), plan.getPlanNum()), plan, tag)
                            );
                        }
                    }

                    // PlanRegion + Place 저장
                    if (planRes.getRegionNum() != null) {
                        Region region = regionRepository.findById(planRes.getRegionNum())
                                .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND_REGION)); // 수정: NOT_FOUND_TAG -> NOT_FOUND_REGION

                        // PlanRegion
                        PlanRegionId regionId = new PlanRegionId(plan.getPlanNum(), region.getRegionNum());
                        planRegionRepository.save(new PlanRegion(regionId, plan, region));

                        // Place
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
            logger.error("일정 수정 실패 - BasicException: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("일정 수정 실패 - Exception", e);
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

        return ApiResponse.success(null, "일정 삭제 성공");
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

//    private String firstRegionName(List<RegionRegistReqDTO> regions) {
//        if (regions == null || regions.isEmpty()) return null;
//        // RegionRegistReqDTO에 regionName 같은 필드가 있다면 그걸 반환
//        // 예시로 cityName+district 조합 등을 사용
//        return Optional.ofNullable(regions.get(0).getRegionName()).orElse(null);
//    }
}
