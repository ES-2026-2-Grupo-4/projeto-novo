package com.esproject4.arenaufcg.DTO;

import java.time.Instant;
import java.util.UUID;

import com.esproject4.arenaufcg.Model.LogAuditoria;
import com.esproject4.arenaufcg.Model.UserRole;

public record LogAuditoriaResponseDTO(
    UUID id,
    UUID usuarioId,
    String usuarioEmail,
    UserRole perfil,
    String acao,
    String recurso,
    String metodoHttp,
    int statusHttp,
    String detalhes,
    String ipOrigem,
    Instant ocorridoEm
) {

    public static LogAuditoriaResponseDTO de(LogAuditoria log) {
        return new LogAuditoriaResponseDTO(
            log.getId(),
            log.getUsuarioId(),
            log.getUsuarioEmail(),
            log.getPerfil(),
            log.getAcao(),
            log.getRecurso(),
            log.getMetodoHttp(),
            log.getStatusHttp(),
            log.getDetalhes(),
            log.getIpOrigem(),
            log.getOcorridoEm()
        );
    }
}