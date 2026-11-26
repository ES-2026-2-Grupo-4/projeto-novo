package com.esproject4.arenaufcg.Repository;

import com.esproject4.arenaufcg.Model.Inscricao;
import com.esproject4.arenaufcg.Model.StatusInscricao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Collection;
import java.util.List;
import java.util.UUID;


public interface InscricaoRepository extends JpaRepository<Inscricao, UUID>{
    
    //Antecipando o que vai ser usado por Camila: Impede que o segundo pedido ativo do mesmo time (INSCRICAO_ATIVA_EXISTENTE)
    boolean existsByCampeonatoIdAndTimeIdAndStatusIn(UUID campeonatoId, UUID timeId,Collection<StatusInscricao> status);

    //Antecipando também a parte de camila : "meus pedidos", do mais recente ao mais antigo
    List<Inscricao> findByTimeIdOrderBySolicitadaEmDesc(UUID timeId);

    //Usado por joão: a lista do organizador, com ou sem filtro de status
    Page<Inscricao> findByCampeonatoIdAndStatus(UUID campeonatoId, StatusInscricao status, Pageable pageable);

    Page<Inscricao> findByCampeonatoId(UUID campeonatoId, Pageable pageable);



}
