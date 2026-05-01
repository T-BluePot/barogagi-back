package com.barogagi.member.join.oauth.controller;

import com.barogagi.member.join.oauth.dto.OAuthLinkDTO;
import com.barogagi.member.join.oauth.enums.Environment;
import com.barogagi.member.join.oauth.enums.Type;
import com.barogagi.member.join.oauth.service.OAuthService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "OAuth", description = "OAuth 관련 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @Operation(summary = "OAuth Link 조회 기능", description = "OAuth Link 조회 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "L200", description = "조회 성공하였습니다."),
            })
    @GetMapping("/oauth-link")
    public ApiResponse selectOAuthLink(@RequestHeader("API-KEY") String apiSecretKey,
                                        @RequestParam Environment environment,
                                        @RequestParam Type type) {

        OAuthLinkDTO oAuthLinkDTO = new OAuthLinkDTO();
        oAuthLinkDTO.setApiSecretKey(apiSecretKey);
        oAuthLinkDTO.setEnvironment(environment);
        oAuthLinkDTO.setType(type);

        return oAuthService.selectOAuthLink(oAuthLinkDTO);
    }
}
