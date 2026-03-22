package com.jwtstudy.jwt_oauth.jwt;

import com.jwtstudy.jwt_oauth.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * [흐름] 모든 http 요청 -> jwt필터 -> 토큰검증 -> 다음 필터 -> 컨트롤러
 *
 * [WHY] springsecurity는 컨트롤러 진입 전에 토큰 유저를 알아야 한다
 *      그래서 필터 단에서 jwt를 꺼내 securitycontext에 저장하는 것
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    /**
     * [WHY] OncePerRequestFilter를 상속하는 이유:
     * 서블릿 필터는 forward/include 등으로 같은 요청이 내부적으로
     * 여러번 통과할 수 있다. OncePerRequestFilter는 그걸 막아준다
     * -> 토큰 검증이 한 요청에서 딱 1번만 실행됨을 보장
     */

    private final JwtProvider jwtProvider;

    /**
     * [WHAT] Authorization 헤더에서 순수 토큰 문자열을 꺼내는 메서드
     */
    public String resolveToken(HttpServletRequest request){
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")){
            return bearer.substring(7);
        }
        return null; //헤더 없으면 null -> 아래 catch로 예외처리
    }

    /**
     *  [흐름] 실제 필터로직
     *  1. 헤더에서 토큰 추출
     *  2. /auth/reissue 요청이면 검증 없이 통과 (RT 재발급 엔드포인트)
     *  3. 토큰이 있으면 -> 검증 -> securitycontext 인증 정보 저장
     *  4. 토큰 없으면 -> 저장 안함 -> security가 비인증 요청으로 처리
     *  5. 다음 필터로 넘기거나 컨트롤러 진입
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);
        System.out.println("token:"+token);
        try {
            // [WHY] /auth/reissue는 AT가 만료된 상태에서 호출된다
            //          즉, 여기서 토큰 검증을 하면 항상 실패
            //          -> 예외없이 filter 통과 -> securityfilter에서 미인증요청으로 판단
            if(request.getRequestURI().equals("/auth/reissue")){
                filterChain.doFilter(request, response); //토큰검증없이 통과
                return;
            }
            if(token != null){
                //[WHAT] 유효성검사 : 실패하면 CustomException 발생
                jwtProvider.validateToken(token);

                Long userId = jwtProvider.getUserIdFromToken(token);
                String role = jwtProvider.getRoleFromToken(token);

                /**
                 * [WHAT] UsernamePasswordAuthenticationToken : Spring Security의 인증객체
                 *
                 * 파라미터 :
                 *  1. principal -> 현재 유저 식별값(여기서는 userId)
                 *  2. credential -> 비밀번호. 이미 로그인에서 검증됐으니 null
                 *  3. authorities -> 권한 목록. List인 이유 : 유저가 여러권한을 가질 수 있음
                 *
                 *  [WHY] 이 객체를 SecurityContext에 넣으면,
                 *      이 후 컨트롤러에서 @AuthenticationPrincipal 등으로 꺼낼 수 있다
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null, //인증 확인 완료
                                List.of(new SimpleGrantedAuthority(role))
                                //한유저가 여러 권한을 가질 수 있음 > list
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // AT = null 이면 아무것도 안함
                //  -> SecurityFilter에서 미인증 요청으로 판단 -> 401 응답 반환
            }
        }catch(CustomException e) {
            // [WHY] 예외를 여기서 직접 처리하는 이유 :
            //       MVC 안 : @ControllerAdvice + @ExceptionHandler 가 예외처리
            //       MVC 밖 : HttpServletResponse에 직접 write
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().println(e.getErrorCode().getMessage());
            return;
        }
        // [WHAT] 다음 필터로 요청/응답 넘기기. 필터가 없으면 Controller 진입
        filterChain.doFilter(request, response);
    }
}