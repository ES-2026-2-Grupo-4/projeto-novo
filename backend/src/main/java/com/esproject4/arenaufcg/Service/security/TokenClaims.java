package com.esproject4.arenaufcg.Service.security;

import java.util.UUID;

import com.esproject4.arenaufcg.Model.UserRole;

public record TokenClaims(UUID userId, String email, UserRole role) {}
