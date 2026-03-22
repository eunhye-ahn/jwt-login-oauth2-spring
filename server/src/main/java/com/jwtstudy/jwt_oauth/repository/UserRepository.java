package com.jwtstudy.jwt_oauth.repository;

import com.jwtstudy.jwt_oauth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //로그인 > 유저조회
    Optional<User> findByEmail(String email);

    //회원가입 > 이메일 중복체크
    boolean existsByEmail(String email);



}
