package com.esproject4.arenaufcg.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.FormatoDisputa;
import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Repository.CampeonatoRepository;
import com.esproject4.arenaufcg.Repository.LocalRepository;

@Service
public class CampeonatoService {

    private final CampeonatoRepository campeonatoRepository;
    private final LocalRepository localRepository;
    private final ConsultaOrganizador consultaOrganizador;
    private final ConsultaInscricoes consultaInscricoes;

    public CampeonatoService(
            CampeonatoRepository campeonatoRepository,
            LocalRepository localRepository,
            ConsultaOrganizador consultaOrganizador,
            ConsultaInscricoes consultaInscricoes) {

        this.campeonatoRepository = campeonatoRepository;
        this.localRepository = localRepository;
        this.consultaOrganizador = consultaOrganizador;
        this.consultaInscricoes = consultaInscricoes;
    }

    @Transactional
    public Campeonato criar(
            UUID usuarioId,
            String nome,
            String descricao,
            Modalidade modalidade,
            FormatoDisputa formato,
            Integer quantidadeGrupos,
            int maxTimes,
            LocalDate dataInicio,
            LocalDate dataFim) {

        if (!consultaOrganizador.isOrganizador(usuarioId)) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "NAO_ORGANIZADOR",
                "Somente organizadores podem criar campeonatos."
            );
        }

        if (!consultaOrganizador.isContaVerificada(usuarioId)) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "CONTA_NAO_VERIFICADA",
                "Verifique sua conta antes de criar campeonatos."
            );
        }

        Campeonato campeonato = new Campeonato(
            nome,
            descricao,
            modalidade,
            formato,
            quantidadeGrupos,
            maxTimes,
            dataInicio,
            dataFim,
            usuarioId
        );

        return campeonatoRepository.save(campeonato);
    }

    @Transactional(readOnly = true)
    public List<Campeonato> listarMeus(UUID usuarioId) {
        return campeonatoRepository
            .findAllByOrganizadorIdAndAtivoTrueOrderByCriadoEmDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public Campeonato buscar(UUID campeonatoId, UUID usuarioId) {
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
    public void remover(UUID campeonatoId, UUID usuarioId) {
        Campeonato campeonato = buscar(campeonatoId, usuarioId);

        if (!campeonato.jaIniciou()
                && consultaInscricoes.possuiInscricoesAtivas(campeonatoId)) {
            throw new RegraNegocioException(
                HttpStatus.CONFLICT,
                "INSCRICOES_ATIVAS",
                "Não é possível remover um campeonato com inscrições pendentes ou aprovadas."
            );
        }

        campeonato.remover();

        campeonatoRepository.save(campeonato);
    }

    @Transactional
    public Campeonato abrirInscricoes(UUID campeonatoId, UUID usuarioId) {
        Campeonato campeonato = buscar(campeonatoId, usuarioId);

        campeonato.abrirInscricoes();

        return campeonatoRepository.save(campeonato);
    }

    @Transactional
    public Campeonato encerrarInscricoes(UUID campeonatoId, UUID usuarioId) {
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

        if (locaisIds == null || locaisIds.contains(null)) {
            throw new RegraNegocioException(
                HttpStatus.BAD_REQUEST,
                "DADOS_INVALIDOS",
                "Informe uma lista de identificadores de locais válidos."
            );
        }

        var idsUnicos = new HashSet<>(locaisIds);
        List<Local> locais = localRepository.findAllById(idsUnicos);

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
