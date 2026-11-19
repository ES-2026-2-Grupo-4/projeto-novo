package com.esproject4.arenaufcg.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.util.UUID;

@Entity
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private String sigla;

    @Enumerated(EnumType.STRING)
    private Modalidade modalidade;
    
    private UUID dirigenteId;

    public Time(){

    }

    public Time(String nome, String sigla, Modalidade modalidade, UUID dirigenteId){

        this.nome = nome;
        this.sigla = sigla;
        this.modalidade = modalidade;
        this.dirigenteId = dirigenteId;
    }

      public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public UUID getDirigenteId() {
        return dirigenteId;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    public void setDirigenteId(UUID dirigenteId) {
        this.dirigenteId = dirigenteId;
    }
}
