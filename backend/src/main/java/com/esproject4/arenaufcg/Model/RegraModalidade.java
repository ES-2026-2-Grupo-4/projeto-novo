package com.esproject4.arenaufcg.Model;

import java.util.List;
import java.util.Arrays;

public enum RegraModalidade {

    FUTEBOL(Modalidade.FUTEBOL, 11, 18, List.of("4-4-2", "4-3-3", "3-5-2", "4-2-3-1", "5-3-2")),
    FUTSAL(Modalidade.FUTSAL, 5, 14, List.of("2-2", "3-1", "1-2-1", "4-0")),
    VOLEI(Modalidade.VOLEI, 6, 14, List.of("5-1", "4-2", "6-2")),
    BASQUETE(Modalidade.BASQUETE, 5, 12, List.of("2-3", "3-2", "1-3-1", "2-1-2"));

    private Modalidade modalidade;
    private int titulares ;
    private int maxRelacionados;
    private List<String> formacoes;

    RegraModalidade(Modalidade modalidade, int titulares, int maxRelacionados, List<String> formacoes) {
        this.modalidade = modalidade;
        this.titulares = titulares;
        this.maxRelacionados = maxRelacionados;
        this.formacoes = formacoes;
    }

    public static RegraModalidade de(Modalidade m){
        return Arrays.stream(values())
            .filter(r -> r.modalidade == m)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Modalidade sem regra: " + m));
    }

    public int titularesDaFormacao(String formacao){
        if (this == VOLEI) return 6;
        int soma = Arrays.stream(formacao.split("-")).mapToInt(Integer::parseInt).sum();
        boolean temGoleiro = this == FUTEBOL || this == FUTSAL;
        return temGoleiro ? soma + 1 : soma;
    }
}
