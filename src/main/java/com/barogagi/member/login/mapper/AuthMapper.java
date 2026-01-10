package com.barogagi.member.login.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthMapper {

    String selectUserInfoByToken(String refreshToken);
}
