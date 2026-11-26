package com.esproject4.arenaufcg.Security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.esproject4.arenaufcg.DTO.RegistroLogCommand;
import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.PermissaoSistema;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Service.AuthService;
import com.esproject4.arenaufcg.Service.AuditoriaService;
import com.esproject4.arenaufcg.Service.ControleAcessoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AutorizacaoPerfilInterceptor implements HandlerInterceptor {

    private static final String CONTEXTO_AUDITORIA =
        AutorizacaoPerfilInterceptor.class.getName() + ".contexto";

    private final AuthService authService;
    private final ControleAcessoService controleAcessoService;
    private final AuditoriaService auditoriaService;

    public AutorizacaoPerfilInterceptor(
            AuthService authService,
            ControleAcessoService controleAcessoService,
            AuditoriaService auditoriaService) {

        this.authService = authService;
        this.controleAcessoService = controleAcessoService;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        if (!(handler instanceof HandlerMethod metodo)) {
            return true;
        }

        RequerPermissao requerPermissao =
            metodo.getMethodAnnotation(RequerPermissao.class);

        if (requerPermissao == null) {
            return true;
        }

        UserResponseDTO usuario = authService.getSessionUser(
            request.getHeader("Authorization")
        );

        try {
            if (requerPermissao.somenteAdmin()
                    && usuario.perfil() != UserRole.ADMIN) {

                throw new RegraNegocioException(
                    HttpStatus.FORBIDDEN,
                    "ACESSO_NEGADO",
                    "Esta operação é exclusiva de administradores."
                );
            }

            for (PermissaoSistema permissao : requerPermissao.value()) {
                controleAcessoService.verificarPermissao(
                    usuario.perfil(),
                    permissao
                );
            }

        } catch (RegraNegocioException exception) {

            if (exception.getStatus().value() == 403) {
                registrarAuditoria(
                    request,
                    usuario,
                    obterAcao(requerPermissao),
                    403,
                    "Tentativa de acesso negado."
                );
            }

            throw exception;
        }

        request.setAttribute(
            CONTEXTO_AUDITORIA,
            new ContextoAuditoria(usuario, requerPermissao)
        );

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {

        if (!(request.getAttribute(CONTEXTO_AUDITORIA)
                instanceof ContextoAuditoria contexto)) {
            return;
        }

        int status = response.getStatus();

        if (status == 403) {
            registrarAuditoria(
                request,
                contexto.usuario(),
                obterAcao(contexto.permissao()),
                status,
                "Tentativa de acesso negado."
            );

        } else if (exception == null
                && status >= 200
                && status < 300
                && !contexto.permissao().acao().isBlank()) {

            registrarAuditoria(
                request,
                contexto.usuario(),
                contexto.permissao().acao(),
                status,
                "Operação concluída com sucesso."
            );
        }
    }

    private String obterAcao(RequerPermissao permissao) {
        if (!permissao.acao().isBlank()) {
            return permissao.acao();
        }

        return permissao.value().length > 0
            ? permissao.value()[0].name()
            : "ACESSO_NEGADO";
    }

    private void registrarAuditoria(
            HttpServletRequest request,
            UserResponseDTO usuario,
            String acao,
            int status,
            String detalhes) {

        auditoriaService.registrarLog(
            new RegistroLogCommand(
                usuario.id(),
                usuario.email(),
                usuario.perfil(),
                acao,
                request.getRequestURI(),
                request.getMethod(),
                status,
                detalhes,
                request.getRemoteAddr()
            )
        );
    }

    private record ContextoAuditoria(
        UserResponseDTO usuario,
        RequerPermissao permissao
    ) {}
}