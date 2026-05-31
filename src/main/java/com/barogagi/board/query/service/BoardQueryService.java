package com.barogagi.board.query.service;
import com.barogagi.board.query.mapper.BoardMapper;
import com.barogagi.board.query.vo.BoardListVO;
import com.barogagi.board.query.vo.BoardVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.BasicException;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardQueryService {
    private static final Logger logger = LoggerFactory.getLogger(BoardQueryService.class);

    private final BoardMapper boardMapper;
    private final Validator validator;

    @Autowired
    public BoardQueryService(BoardMapper boardMapper, Validator validator) {
        this.boardMapper = boardMapper;
        this.validator = validator;
    }

    public ApiResponse getBoardList(Integer page, HttpServletRequest request) {
        try {
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(),
                        ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            int size = 20;
            int offset = (page - 1) * size;

            List<BoardListVO> boardList = boardMapper.selectBoardList(offset, size);
            return ApiResponse.success(boardList, "공지사항 목록 조회 성공");

        } catch (BasicException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("getBoardList error", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(),
                    ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }

    public ApiResponse getBoardDetail(Integer boardNum, HttpServletRequest request) {
        try {
            if (!validator.apiSecretKeyCheck(request.getHeader("API-KEY"))) {
                return ApiResponse.error(ErrorCode.NOT_EQUAL_API_SECRET_KEY.getCode(),
                        ErrorCode.NOT_EQUAL_API_SECRET_KEY.getMessage());
            }

            if (boardNum == null || boardNum <= 0) {
                return ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(),
                        ErrorCode.INVALID_REQUEST.getMessage());
            }

            BoardVO board = boardMapper.selectBoardDetail(boardNum);

            if (board == null) {
                return ApiResponse.error(ErrorCode.NOT_FOUND_BOARD.getCode(),
                        ErrorCode.NOT_FOUND_BOARD.getMessage());
            }

            return ApiResponse.success(board, "공지사항 상세 조회 성공");

        } catch (BasicException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("getBoardDetail error", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(),
                    ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }
}