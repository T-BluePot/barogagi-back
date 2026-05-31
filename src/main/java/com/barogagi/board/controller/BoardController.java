package com.barogagi.board.controller;

import com.barogagi.board.query.service.BoardQueryService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지사항", description = "공지사항 관련 API")
@RestController
@RequestMapping("/api/v1/board")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "apiKeyAuth")
public class BoardController {
    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);

    private final BoardQueryService boardQueryService;

    public BoardController(BoardQueryService boardQueryService) {
        this.boardQueryService = boardQueryService;
    }

    @Operation(
            summary = "공지사항 목록 조회",
            description = "전체 공지사항 목록을 조회합니다. 중요 공지는 상단에 고정됩니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "B200",
                            description = "공지사항 정보 조회에 성공했습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "B101",
                            description = "공지사항 정보를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON-500",
                            description = "서버 오류가 발생했습니다."
                    )
            }
    )
    @GetMapping("/list")
    public ApiResponse getBoardList(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            HttpServletRequest request) {
        logger.info("CALL /api/v1/board/list");
        logger.info("[input] page={}", page);
        return boardQueryService.getBoardList(page, request);
    }

    @Operation(
            summary = "공지사항 상세 조회",
            description = "공지사항 번호로 상세 내용을 조회합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "B200",
                            description = "공지사항 정보 조회에 성공했습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "B101",
                            description = "공지사항 정보를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON-500",
                            description = "서버 오류가 발생했습니다."
                    )
            }
    )
    @GetMapping("/detail")
    public ApiResponse getBoardDetail(
            @Parameter(description = "공지사항 번호", example = "1")
            @RequestParam Integer boardNum,
            HttpServletRequest request) {
        logger.info("CALL /api/v1/board/detail");
        logger.info("[input] boardNum={}", boardNum);
        return boardQueryService.getBoardDetail(boardNum, request);
    }
}