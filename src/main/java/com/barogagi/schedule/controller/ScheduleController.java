package com.barogagi.schedule.controller;

import com.barogagi.plan.query.service.PlanQueryService;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.command.service.ScheduleCommandService;
import com.barogagi.schedule.dto.*;
import com.barogagi.schedule.query.service.ScheduleQueryService;
import com.barogagi.util.InputValidate;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.exception.BasicException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Tag(name = "일정", description = "일정 관련 API")
@RestController
@RequestMapping("/api/v1/schedule")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "apiKeyAuth")
public class ScheduleController implements SwaggerScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final InputValidate inputValidate;
    private final ScheduleQueryService scheduleQueryService;
    private final ScheduleCommandService scheduleCommandService;
    private final PlanQueryService planQueryService;
    private final String API_SECRET_KEY;
    private final MembershipUtil membershipUtil;

    public ScheduleController(Environment environment,
                              InputValidate inputValidate,
                              ScheduleQueryService scheduleQueryService,
                              ScheduleCommandService scheduleCommandService,
                              PlanQueryService planQueryService,
                              MembershipUtil membershipUtil) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.inputValidate = inputValidate;
        this.scheduleQueryService = scheduleQueryService;
        this.scheduleCommandService = scheduleCommandService;
        this.planQueryService = planQueryService;
        this.membershipUtil = membershipUtil;
    }

    @GetMapping("/list")
    public ApiResponse getScheduleList(HttpServletRequest request) {
        logger.info("CALL /api/v1/schedule/list");
        return scheduleQueryService.getScheduleList(request);
    }

    @GetMapping("/detail")
    public ApiResponse getScheduleDetail(@RequestParam Integer scheduleNum, HttpServletRequest request) {
        logger.info("CALL /api/v1/schedule/detail");
        return scheduleQueryService.getScheduleDetail(scheduleNum, request);
    }

    @PostMapping("/create")
    public ApiResponse createSchedule(@RequestBody ScheduleRegistReqDTO scheduleRegistReqDTO,
                                      HttpServletRequest request) {
        logger.info("CALL /api/v1/schedule/create");
        return scheduleCommandService.createSchedule(scheduleRegistReqDTO, request);
    }

    @PostMapping("/save")
    public ApiResponse saveSchedule(HttpServletRequest request,
                                    @RequestBody ScheduleRegistResDTO scheduleRegistResDTO) {
        logger.info("CALL api/v1/schedule/save");
        return scheduleCommandService.saveSchedule(scheduleRegistResDTO, request);
    }

    @PutMapping("/")
    public ApiResponse updateSchedule(@RequestBody ScheduleRegistResDTO scheduleRegistResDTO, HttpServletRequest request) {
        logger.info("CALL /api/v1/schedule/update");
        return scheduleCommandService.updateSchedule(scheduleRegistResDTO, request);
    }

    @DeleteMapping("/")
    public ApiResponse deleteSchedule(@RequestParam Integer scheduleNum, HttpServletRequest request) {
        logger.info("CALL /api/v1/schedule/delete");
        return scheduleCommandService.deleteSchedule(scheduleNum, request);
    }

    // 구현체 (ScheduleController)
    @Override
    @GetMapping("/image/proxy")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url, HttpServletRequest request) {
        try {
            String queryString = request.getQueryString();
            String fullUrl = URLDecoder.decode(
                    queryString.substring(queryString.indexOf("url=") + 4),
                    StandardCharsets.UTF_8
            );

            logger.info("프록시 요청 URL: {}", fullUrl);

            Connection.Response response = Jsoup.connect(fullUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://place.map.kakao.com/")
                    .ignoreContentType(true)
                    .timeout(5000)
                    .execute();

            return ResponseEntity.ok()
                    .header("Content-Type", response.contentType())
                    .header("Cache-Control", "public, max-age=86400")
                    .body(response.bodyAsBytes());

        } catch (IOException e) {
            logger.error("프록시 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}