package com.jwtstudy.jwt_oauth.service;

import com.jwtstudy.jwt_oauth.domain.User;
import com.jwtstudy.jwt_oauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * [WHAT] OAuth2UserService : 유저 정보를 가져오는 클래스의 인터페이스
 *          Google이 유저정보를 줬을 때, 그걸 받아서 처리하는 로직 구현
 *
 * [흐름] userInfo(email, name) => db저장 => handler로 보내기
 */

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{
    private final UserRepository userRepository;
    //유저정보 받기
    public OAuth2User loadUser(OAuth2UserRequest userRequest){
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> userInfo = oauth2User.getAttributes();
        //콘솔에 찍어서확인해보기
        System.out.println(userInfo);
        System.out.println(userInfo.get("name"));
        System.out.println(userInfo.get("email"));

        return oauth2User;
    }
}
