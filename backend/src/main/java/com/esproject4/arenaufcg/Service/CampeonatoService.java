package com.esproject4.arenaufcg.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Repository.CampeonatoRepository;
import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Repository.LocalRepository;

@Service
public class CampeonatoService {

    private final CampeonatoRepository campeonatoRepository;
    private final LocalRepository localRepository;

    public CampeonatoService(
            CampeonatoRepository campeonatoRepository,
            LocalRepository localRepository) {

       this.localRepository = localRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    @Transactional
    public Campeonato criar(
            String nome,
            String descricao,
            Modalidade modalidade,
            FormatoDisputa formato,
            Integer quantidadeGrupos,
            int maxTimes,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID organizadorId) {

        exigirUsuarioIdentificado(organizadorId);

        Campeonato campeonato = new Campeonato(
            nome,
            descricao,
            modalidade,
            formato,
            quantidadeGrupos,
            maxTimes,
            dataInicio,
            dataFim,
            organizadorId
        );

        return campeonatoRepository.save(campeonato);
    }

    @Transactional(readOnly = true)
    public List<Campeonato> listarMeus(UUID usuarioId) {
        exigirUsuarioIdentificado(usuarioId);

        return campeonatoRepository
            .findByOrganizadorIdAndAtivoTrue(usuarioId);
    }

    @Transactional(readOnly = true)
    public Campeonato buscar(UUID campeonatoId, UUID usuarioId) {
        exigirUsuarioIdentificado(usuarioId);

        Campeonato campeonato = campeonatoRepository
            .findByIdAndAtivoTrue(campeonatoId)
            .orElseThrow(() -> new RegraNegocioException(
                HttpStatus.NOT_FOUND,
                "NAO_ENCONTRADO",
                "Campeonato não encontrado."
            ));

        if (!campeonato.pertenceA(usuarioId)) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO",
                "Somente o dono pode acessar esta operação."
            );
        }

        return campeonato;
    }

    private void exigirUsuarioIdentificado(UUID usuarioId) {
        if (usuarioId == null) {
            throw new RegraNegocioException(
                HttpStatus.UNAUTHORIZED,
                "NAO_AUTENTICADO",
                "É necessário identificar o usuário."
            );
        }
    }

    @Transactional
   public Campeonato atualizar(
           UUID campeonatoId,
           UUID usuarioId,
           String nome,
           String descricao,
           Modalidade modalidade,
           FormatoDisputa formato,
           Integer quantidadeGrupos,
           int maxTimes,
           LocalDate dataInicio,
           LocalDate dataFim) {

       Campeonato campeonato = buscar(campeonatoId, usuarioId);

       campeonato.atualizar(
           nome,
           descricao,
           modalidade,
           formato,
           quantidadeGrupos,
           maxTimes,
           dataInicio,
           dataFim
       );

       return campeonatoRepository.save(campeonato);
   }

   @Transactional
   public Campeonato abrirInscricoes(
           UUID campeonatoId, UUID usuarioId) {

       Campeonato campeonato = buscar(campeonatoId, usuarioId);

       campeonato.abrirInscricoes();

       return campeonatoRepository.save(campeonato);
   }

   @Transactional
   public Campeonato encerrarInscricoes(
           UUID campeonatoId, UUID usuarioId) {

       Campeonato campeonato = buscar(campeonatoId, usuarioId);

       campeonato.encerrarInscricoes();

       return campeonatoRepository.save(campeonato);
   }

   @Transactional
   public Campeonato definirLocais(
           UUID campeonatoId,
           UUID usuarioId,
           Collection<UUID> locaisIds) {

       Campeonato campeonato = buscar(campeonatoId, usuarioId);

       if (campeonato.jaIniciou()) {
           throw new RegraNegocioException(
               HttpStatus.UNPROCESSABLE_CONTENT,
               "CAMPEONATO_INICIADO",
               "Os locais não podem ser alterados depois do início."
           );
       }

       if (locaisIds == null || locaisIds.contains(null)) {
           throw new RegraNegocioException(
               HttpStatus.BAD_REQUEST,
               "DADOS_INVALIDOS",
               "Informe uma lista de identificadores de locais válidos."
           );
       }

       var idsUnicos = new HashSet<>(locaisIds);
       var locais = new ArrayList<Local>(
           localRepository.findAllById(idsUnicos)
       );

       if (locais.size() != idsUnicos.size()) {
           throw new RegraNegocioException(
               HttpStatus.NOT_FOUND,
               "NAO_ENCONTRADO",
               "Um ou mais locais não foram encontrados."
           );
       }

       campeonato.definirLocais(locais);

       return campeonatoRepository.save(campeonato);
   }
}
