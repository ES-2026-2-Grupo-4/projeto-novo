package com.esproject4.arenaufcg.DTO;

import java.util.UUID;

import com.esproject4.arenaufcg.Model.UserRole;

public record RegistroLogCommand(
    UUID usuarioId,
    String usuarioEmail,
    UserRole perfil,
    String acao,
    String recurso,
    String metodoHttp,
    int statusHttp,
    String detalhes,
    String ipOrigem
) {}