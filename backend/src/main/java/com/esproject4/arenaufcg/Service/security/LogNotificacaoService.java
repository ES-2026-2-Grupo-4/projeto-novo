package com.esproject4.arenaufcg.Service.security;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

// APENAS PARA DESENVOLVIMENTO: imprime o código/token no log em vez de enviar e-mail.
// Devemos trocar por uma implementação real antes de ir para produção.
@Slf4j
@Service
public class LogNotificacaoService implements NotificacaoService {

    @Override
    public void enviarCodigoVerificacao(String email, String codigo) {
        log.info("[DEV] Código de verificação para {}: {}", email, codigo);
    }

    @Override
    public void enviarRecuperacaoDeSenha(String email, String resetToken) {
        log.info("[DEV] Token de recuperação de senha para {}: {}", email, resetToken);
    }
}
