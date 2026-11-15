package com.esproject4.arenaufcg.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

//Código provisório, ajeitar depois camila, isso foi apenas para rodar o meu.
@Entity
@Table(name = "teams")
public class Time {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;
    private String sigla;

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getSigla() { return sigla; }
}