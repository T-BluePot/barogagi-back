package com.barogagi.board.query.mapper;

import com.barogagi.board.query.vo.BoardListVO;
import com.barogagi.board.query.vo.BoardVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BoardMapper {

    // 공지사항 목록 조회
    List<BoardListVO> selectBoardList(int offset, int size);

    // 공지사항 상세 조회
    BoardVO selectBoardDetail(int boardNum);
}