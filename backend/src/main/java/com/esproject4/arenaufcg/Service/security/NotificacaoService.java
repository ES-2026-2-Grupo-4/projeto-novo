package com.esproject4.arenaufcg.Service.security;

// Porta de saída para o envio de e-mails (verificação de conta e recuperação de senha).
public interface NotificacaoService {
    void enviarCodigoVerificacao(String email, String codigo);
    void enviarRecuperacaoDeSenha(String email, String resetToken);
}