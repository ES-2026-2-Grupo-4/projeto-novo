package com.esproject4.arenaufcg.Model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.esproject4.arenaufcg.Model.ValueObjects.Email;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"password", "resetPasswordToken", "verifyToken"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @JsonProperty("id")
    private UUID id;

    @Column(name = "name", length = 120, nullable = false)
    @JsonProperty("name")
    private String name;

    @Embedded
    @JsonProperty("email")
    private Email email;

    @Column(name = "password", length = 255, nullable = false)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30, nullable = false)
    @JsonProperty("role")
    private UserRole role;

    @Builder.Default
    @Column(name = "verified", nullable = false)
    @JsonProperty("verified")
    private Boolean verified = false;

    @Builder.Default
    @Column(name = "active", nullable = false)
    @JsonProperty("active")
    private Boolean active = true;

    @Column(name = "accepted_terms", nullable = false)
    @JsonProperty("acceptedTerms")
    private Boolean acceptedTerms;

    @Column(name = "verify_token", length = 255)
    @JsonIgnore
    private String verifyToken;

    @Column(name = "verify_token_expires_in")
    @JsonProperty("verifyTokenExpiresIn")
    private OffsetDateTime verifyTokenExpiresIn;

    @Column(name = "reset_password_token", length = 255, unique = true)
    @JsonIgnore
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expires_in")
    @JsonProperty("resetPasswordTokenExpiresIn")
    private OffsetDateTime resetPasswordTokenExpiresIn;

    @Column(name = "last_login_at")
    @JsonProperty("lastLoginAt")
    private OffsetDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;
}
