package com.barogagi.member.login.mapper;

import com.barogagi.member.login.dto.RefreshRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthMapper {

    String selectUserInfoByToken(String refreshToken);
}
