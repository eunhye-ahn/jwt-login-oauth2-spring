package com.jwtstudy.jwt_oauth.config;

import com.jwtstudy.jwt_oauth.jwt.JwtFilter;
import com.jwtstudy.jwt_oauth.oauth2.CustomOAuth2UserService;
import com.jwtstudy.jwt_oauth.oauth2.SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * [WHAT] SpringSecurity 전체 설정 클래스
 *
 * [흐름] Http요청 -> CORS처리 -> JwtFilter 토큰 검증 -> 인가 확인 -> 컨트롤러
 *
 * [WHY] @Configuration + @EnableWebSecurity 조합 :
 * @Configuration : 이 클래스가 Bean 설정 클래스임을 선언
 * @EnableWebSecurity : SpringSecurity 자동 설정을 끄고 이 클래스가 직접 제어함을 선언
 */

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final SuccessHandler successHandler;
    /**
     * [WHAT] SpringSecurity의 핵심 필터 체인 설정
     *          Http요청이 들어오면 여기서 정의한 순서대로 처리됨
     *
     * [WHY] JwtFilter를 파라미터로 받는 이유 :
     *      @Bean으로 등록된 JwtFilter를 Spring이 자동 주입해줌
     *      -> SecurityConfig가 JwtFilter에 직접 의존하지 않아도 됨
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                //[WHAT] CORS 설정을 아래 corsConfigurationSource() Bean에 위임
                //[WHY] 브라우저는 다른 출처(도메인/포트)로 요청 시 CORS 정책을 확인한다
                .cors(cors->cors.configurationSource(corsConfigurationSource()))

                //[WHAT] CSRF 보호 비활성화
                //[WHY] CSRF는 세션 기반 인증의 취약점을 막는 장치
                //      stateless로 설정하면 springsecurity가 세션을 아예 생성하지 않음
                .csrf(csrf->csrf.disable())

                //[WHAT] 세션 생성 정책을 stateless로 설정
                //[WHY] Jwt는 서버가 상태를 저장하지 않은 무상태 방식
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //[WHAT] 폼 로그인 비활성화
                //[WHY] springsecurity 기본 제공인 로그인 폼을 사용하지 않음
                //      jwt 방식은 직접 만든 /auth/login API로 인증하기 때문
                .formLogin(form->form.disable())

                // Q : authentictationEntryPoint 설정이 없으면 token=null일 때 403이 반환됨
                //      -> 인증 실패임에도 403으로 내려와서 클라이언트가 혼란
                //[WHAT] Http basic 인증 비활성화
                //[WHY] basic 인증 = Authorization : Basic {}
                //      authenticated() 에 대해서는 securitycontext로 인증 필요
                //
                .httpBasic(httpBasic->httpBasic.disable())
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/auth/**", "/oauth2/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated() //여기에 대한 설정을 CorsConfigurationSource << 얘가 해줌

                )

                //[WHAT] Google 로그인 성공 후 흐름을 커스텀하기 위한 설정
                //[흐름] Google 인증 완료 -> 유저정보 수신 // 여기까지 테스트완료 -> DB저장 -> JWT 발급
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(successHandler) //로그인성공 -> JWT발급
                )

                // Q :  token=null일 경우, jwtfilter 에서 인증정보없이 필터가 통과되어서
                //      인가단계에서 403처리됨
                //      -> authenticationEntryPoint 추가 401로 반환되도록 처리
                //[WHAT] 인증 실패 시 처리 (401응답)
                //[흐름] token=null
                //      → 필터 통과 (SecurityContext 비어있음)
                //      → .anyRequest().authenticated() 에서 막힘
                //      → Spring Security가 예외 종류를 판단
                //          ├── 인증 안됨 (익명사용자) → AuthenticationException → AuthenticationEntryPoint 호출
                //          └── 인증은 됐는데 권한 없음 → AccessDeniedException → AccessDeniedHandler 호출
                .exceptionHandling(e->e
                        .authenticationEntryPoint((request, response, authException) -> {
                            //인증실패했을때
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //401
                            response.getWriter().write("토큰 null : 인증이 필요합니다");
                        }))

                //[WHAT] JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 삽입
                //[WHY] springsecurity 필터 체인에서 UsernamePasswordAuthenticationFilter
                //          는 폼 로그인 필터지만, addFilter
                //        그 앞에 JwtFilter를 끼워넣어야
                //        토큰 검증 → SecurityContext 저장이 먼저 일어나고
                //        이후 Security의 인가(authorized) 체크가 올바르게 동작함
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Q : webMvcConfigurer과 CorsConfigurationSource 차이가 뭔가?
     * [흐름]
     * -시큐리티 없는 경우 : 요청 -> dispatcherservlet -> webmvcconfig -> 컨트롤러
     * -시큐리티 있는 경우 : 요청 -> corsfilter -> jwtfiltr
     *                    -> security인가체크(=securityfilterchain)
     *                    -> dispatcheerservlet -> 컨트롤러
     * [WHAT] authenticated()에 대한 CORS설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("*"));

        //[WHY] true로 설정해야 브라우저가 쿠키/Authorization 헤더를 요청에 포함시킴
        //      false면 JWT가 담긴 헤더가 전송되지않음
        //      단, true일 때는 setAllowedOrigins에 "*" 사용 불가 -> 명시적 도메인 필수
        config.setAllowCredentials(true);

        //[WHAT] 위 설정을 모든 경로("/**")에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * [WHAT] 비밀번호 암호화 Bean
     *
     * [WHY] BCrypt를 사용하는 이유:
     *      -단방향 해시 -> 복호화 불가, DB가 털려도 원본 비밀번호가 노출안됨
     *
     * [WHY] Bean으로 등록하는 이유:
     *      UserService 등의 곳에서 주입받아 쓰기 위해
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
