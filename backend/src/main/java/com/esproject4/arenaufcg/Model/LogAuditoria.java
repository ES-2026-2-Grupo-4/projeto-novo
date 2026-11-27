package com.esproject4.arenaufcg.Model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID usuarioId;

    @Column(name = "user_email", length = 180)
    private String usuarioEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole perfil;

    @Column(name = "action", nullable = false, length = 80)
    private String acao;

    @Column(name = "resource", nullable = false, length = 255)
    private String recurso;

    @Column(name = "http_method", nullable = false, length = 10)
    private String metodoHttp;

    @Column(name = "http_status", nullable = false)
    private int statusHttp;

    @Column(name = "details", length = 500)
    private String detalhes;

    @Column(name = "origin_ip", length = 45)
    private String ipOrigem;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant ocorridoEm;

    public LogAuditoria(
            UUID usuarioId,
            String usuarioEmail,
            UserRole perfil,
            String acao,
            String recurso,
            String metodoHttp,
            int statusHttp,
            String detalhes,
            String ipOrigem) {

        this.usuarioId = usuarioId;
        this.usuarioEmail = usuarioEmail;
        this.perfil = perfil;
        this.acao = acao;
        this.recurso = recurso;
        this.metodoHttp = metodoHttp;
        this.statusHttp = statusHttp;
        this.detalhes = detalhes;
        this.ipOrigem = ipOrigem;
        this.ocorridoEm = Instant.now();
    }
}