package com.calorie.CalorieCalculator_backend.mapper;

import com.calorie.CalorieCalculator_backend.entity.AppUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;

@Mapper
public interface AuthMapper {
    @Select("INSERT INTO app_users(wechat_openid) VALUES(#{openid}) " +
            "ON CONFLICT (wechat_openid) DO UPDATE SET updated_at = CURRENT_TIMESTAMP " +
            "RETURNING id")
    Long upsertUser(@Param("openid") String openid);

    @Insert("INSERT INTO app_sessions(user_id, token_hash, expires_at) " +
            "VALUES(#{userId}, #{tokenHash}, #{expiresAt})")
    void insertSession(
            @Param("userId") Long userId,
            @Param("tokenHash") String tokenHash,
            @Param("expiresAt") Instant expiresAt);

    @Select("SELECT user_id FROM app_sessions " +
            "WHERE token_hash = #{tokenHash} AND expires_at > #{now}")
    Long findValidUserId(
            @Param("tokenHash") String tokenHash,
            @Param("now") Instant now);

    @Delete("DELETE FROM app_sessions WHERE token_hash = #{tokenHash} AND user_id = #{userId}")
    int deleteSession(@Param("tokenHash") String tokenHash, @Param("userId") Long userId);

    @Delete("DELETE FROM app_sessions WHERE expires_at <= #{now}")
    int deleteExpiredSessions(@Param("now") Instant now);

    @Select("SELECT id, created_at AS createdAt FROM app_users " +
            "WHERE id = #{userId} AND NOT is_legacy_owner")
    AppUser findUser(@Param("userId") Long userId);

    @Delete("DELETE FROM app_sessions WHERE user_id = #{userId}")
    int deleteSessionsByUser(@Param("userId") Long userId);

    @Delete("DELETE FROM app_users WHERE id = #{userId} AND NOT is_legacy_owner")
    int deleteUser(@Param("userId") Long userId);
}
