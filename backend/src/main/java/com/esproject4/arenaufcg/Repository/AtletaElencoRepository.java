package com.esproject4.arenaufcg.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.esproject4.arenaufcg.Model.AtletaElenco;
import com.esproject4.arenaufcg.Model.StatusCampeonato;

public interface AtletaElencoRepository extends JpaRepository<AtletaElenco, UUID> {

    Optional<AtletaElenco> findByIdAndAtivoTrue(UUID id);

    boolean existsByInscricaoIdAndNumeroAndAtivoTrue(UUID inscricaoId, int numero);

    boolean existsByCampeonatoIdAndAtletaIdAndAtivoTrue(UUID campeonatoId, UUID atletaId);

    List<AtletaElenco> findByInscricaoIdAndAtivoTrueOrderByNumero(UUID inscricaoId);

    @Query("""
        select count(e) > 0 from AtletaElenco e
        where e.atleta.id = :atletaId
          and e.ativo = true
          and e.inscricao.campeonato.status <> :status
        """)
    boolean existsEmElencoAtivoDeCampeonatoNaoEm(@Param("atletaId") UUID atletaId,
                                                 @Param("status") StatusCampeonato status);

    default boolean atletaEmElencoAtivoNaoFinalizado(UUID atletaId) {
        return existsEmElencoAtivoDeCampeonatoNaoEm(atletaId, StatusCampeonato.FINALIZADO);
    }
}