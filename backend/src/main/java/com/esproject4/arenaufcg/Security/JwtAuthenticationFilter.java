package com.esproject4.arenaufcg.Security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final RequestMatcher rotasPublicas;

    public JwtAuthenticationFilter(
            AuthService authService,
            RequestMatcher rotasPublicas) {

        this.authService = authService;
        this.rotasPublicas = rotasPublicas;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return rotasPublicas.matches(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization != null) {
            try {
                UserResponseDTO usuario =
                    authService.getSessionUser(authorization);

                var authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                        usuario,
                        null,
                        List.of(new SimpleGrantedAuthority(
                            "ROLE_" + usuario.perfil().name()
                        ))
                    );

                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

            } catch (RegraNegocioException exception) {
                SecurityContextHolder.clearContext();
                responderNaoAutenticado(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    public static void responderNaoAutenticado(
            HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("""
            {
                "codigo": "TOKEN_INVALIDO",
                "mensagem": "Token ausente ou inválido."
            }
            """);
    }
}