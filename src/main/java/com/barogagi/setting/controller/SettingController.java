package com.barogagi.setting.controller;

import com.barogagi.response.ApiResponse;
import com.barogagi.setting.enums.SettingType;
import com.barogagi.setting.enums.Value;
import com.barogagi.setting.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "설정", description = "설정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settings")
public class SettingController {

    private final SettingService settingService;

    @Operation(summary = "설정 목록 조회 기능", description = "설정 목록 조회 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "S200", description = "설정 목록 조회 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-500", description = "서버 오류가 발생했습니다.")
            })
    @GetMapping
    public ApiResponse selectSettings(HttpServletRequest request) {
        return settingService.selectSettings(request);
    }

    @Operation(summary = "설정 수정 기능", description = "설정 수정 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "S202", description = "설정 수정 완료하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "S400", description = "설정 정보 수정에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-500", description = "서버 오류가 발생했습니다.")
            })
    @PatchMapping("/{settingType}/{value}")
    public ApiResponse updateSetting(HttpServletRequest request,
                                     @PathVariable(name = "settingType") SettingType settingType,
                                     @PathVariable(name = "value") Value value) {
        return settingService.updateSetting(request, settingType, value);
    }
}
