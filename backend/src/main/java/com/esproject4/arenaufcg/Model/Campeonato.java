package com.esproject4.arenaufcg.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

//Código provisório joão, apenas fiz para que meu código rodasse
@Entity
@Table(name = "championships")
public class Campeonato {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;

    public UUID getId() { return id; }
    public String getNome() { return nome; }
}