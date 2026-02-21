package com.barogagi.ai.constant;

public class AiPrompt {

    private AiPrompt() {}

    /**
     * 1차 AI 호출: Tavily 웹 검색 결과에서 장소명 추출
     */
    public static final String EXTRACT_PLACE_NAMES_SYSTEM = "JSON 배열로만 응답하세요. 다른 텍스트는 절대 포함하지 마세요.";

    public static final String EXTRACT_PLACE_NAMES_USER = """
            아래는 "%s" 지역의 "%s" 관련 웹 검색 결과입니다.
            이 텍스트에서 실제 존재하는 장소명(가게명, 관광지명, 시설명)만 추출해 주세요.
            
            카테고리: %s
            관련 태그: %s
            
            규칙:
            - 반드시 카테고리와 태그에 부합하는 장소만 추출
            - 예: 카테고리가 "탐방"이고 태그가 "박물관, 미술관"이면 음식점/카페는 절대 포함하지 마세요
            - 예: 카테고리가 "식사"이고 태그가 "맛집"이면 박물관/관광지는 절대 포함하지 마세요
            - 블로그 제목, 웹사이트 이름, 일반 명사는 제외
            - 카카오맵에서 검색 가능한 정확한 장소명만 포함 (지점명이 있으면 지점명까지 포함)
            - 최대 %d개까지만 추출
            - 반드시 JSON 배열 형식으로만 응답 (다른 텍스트 없이)
            
            예시 응답: ["국립현대미술관 서울관", "서울역사박물관", "북촌한옥마을"]
            
            검색 결과:
            %s
            """;

    /**
     * 2차 AI 호출: 카카오 장소 목록에서 추천 + 한줄 소개
     */
    public static final String RECOMMEND_PLACE_SYSTEM = """
            너는 AI 추천 도우미야.
            아래 입력 값을 참고해서 추천을 해줘:
            - comment: 사용자가 입력한 설명/코멘트 텍스트
            - tags: 사용자가 입력한 태그 목록(예: 이색카페, 맛집투어, 전통시장)
            - places: 서비스에서 추천 후보로 전달된 장소 목록. 각 장소에는 title(장소명)과 description(카테고리/주소 정보)이 포함됨.
            
            지켜야 할 규칙:
            1) tags와 comment를 바탕으로 places 중 **단 1개**만 추천해줘.
            2) 추천된 place의 description 정보를 참고해서 해당 장소에 대한 한줄 소개를 작성해줘.
               - 반드시 추천된 장소(title)에 대해서만 설명해줘.
               - 제공된 places 목록에 없는 다른 장소를 절대 언급하지 마.
               - 장소 이름은 설명에 넣지 마. (ex. 분위기 좋은 카페에서 특별한 커피를 즐길 수 있다)
            3) 반드시 JSON 형식으로 **정확히** 출력해줘. 키 값은 recommandPlaceIndex, aiDescription 두 개만 있어야 함.
            
            출력(JSON only):
            {
              "recommandPlaceIndex": 추천된 place의 index 번호,
              "aiDescription": "추천된 장소에 대한 한 문장 요약"
            }
            """;
}