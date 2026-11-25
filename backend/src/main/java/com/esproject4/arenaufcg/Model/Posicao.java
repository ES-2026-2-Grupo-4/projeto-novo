package com.esproject4.arenaufcg.Model;

import java.util.Arrays;
import java.util.List;

public enum Posicao {
    //futebol
    GOLEIRO(Modalidade.FUTEBOL, Modalidade.FUTSAL),
    ZAGUEIRO(Modalidade.FUTEBOL),
    LATERAL(Modalidade.FUTEBOL),
    VOLANTE(Modalidade.FUTEBOL),
    MEIA(Modalidade.FUTEBOL),
    ATACANTE(Modalidade.FUTEBOL),

    //futsal
    FIXO(Modalidade.FUTSAL),
    ALA(Modalidade.FUTSAL, Modalidade.BASQUETE),
    PIVO(Modalidade.FUTSAL, Modalidade.BASQUETE),

    //volei
    LEVANTADOR(Modalidade.VOLEI),
    OPOSTO(Modalidade.VOLEI),
    PONTEIRO(Modalidade.VOLEI),
    CENTRAL(Modalidade.VOLEI),
    LIBERO(Modalidade.VOLEI),

    //basquete
    ARMADOR(Modalidade.BASQUETE),
    ALA_ARMADOR(Modalidade.BASQUETE),
    ALA_PIVO(Modalidade.BASQUETE);

    private final List<Modalidade> modalidades;


//aqui eu pesquisei sobre, e vi que o ideal é usar Varargs, que basicamente serve para 
//passar uma ou várias modalidades sem criar um array manualmente.
    Posicao(Modalidade... modalidades) { 
        this.modalidades = Arrays.asList(modalidades);
    }

    public boolean aceita(Modalidade modalidade) {
        return modalidades.contains(modalidade);
    }
}