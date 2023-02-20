package com.sparta.myselectshop.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.myselectshop.dto.SecurityExceptionDto;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 토큰을 검증하고 유효하지 않으면 다시 로그인 페이지로 되돌려보낸다.
        // 로그인이 필요한 서비스들을 진행할 때 인증처리를 하는 것.
        String token = jwtUtil.resolveToken(request);

        if(token != null) { // 토큰이 없는 페이지도 존재하기 때문에(로그인 페이지) 분기처리 필요
            if(!jwtUtil.validateToken(token)){
                jwtExceptionHandler(response, "Token Error", HttpStatus.UNAUTHORIZED.value());
                return;
            }
            Claims info = jwtUtil.getUserInfoFromToken(token); //인증이 완료되면 정보를 가지고 온다.
            setAuthentication(info.getSubject()); //토큰 내 Username을 전달
        }
        filterChain.doFilter(request,response); //
    }

    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = jwtUtil.createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    //토큰에 대한 오류가 발생했을 때 클라이언트로 커스텀해서 예외처리 값을 날려주는 메서드
    public void jwtExceptionHandler(HttpServletResponse response, String msg, int statusCode) {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        try {
            String json = new ObjectMapper().writeValueAsString(new SecurityExceptionDto(statusCode, msg));
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}