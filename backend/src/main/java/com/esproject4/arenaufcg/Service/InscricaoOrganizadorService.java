package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esproject4.arenaufcg.DTO.InscricaoResponseDTO;
import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.Inscricao;
import com.esproject4.arenaufcg.Model.StatusInscricao;
import com.esproject4.arenaufcg.Repository.CampeonatoRepository;
import com.esproject4.arenaufcg.Repository.InscricaoRepository;

@Service
public class InscricaoOrganizadorService {

    private final InscricaoRepository inscricaoRepository;
    private final CampeonatoRepository campeonatoRepository;

    public InscricaoOrganizadorService(
            InscricaoRepository inscricaoRepository,
            CampeonatoRepository campeonatoRepository) {

        this.inscricaoRepository = inscricaoRepository;
        this.campeonatoRepository = campeonatoRepository;
    }

    @Transactional
    public InscricaoResponseDTO aprovar(UUID inscricaoId, UUID usuarioAtualId) {
        Inscricao inscricao = buscarInscricaoAutorizada(inscricaoId, usuarioAtualId);

        inscricao.getCampeonato().reservarVaga();
        inscricao.aprovar(usuarioAtualId);

        return InscricaoResponseDTO.de(inscricao);
    }

    @Transactional
    public InscricaoResponseDTO recusar(UUID inscricaoId, String motivo, UUID usuarioAtualId) {
        Inscricao inscricao = buscarInscricaoAutorizada(inscricaoId, usuarioAtualId);
        if (inscricao.getCampeonato().jaIniciou()) {
            throw new RegraNegocioException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "CAMPEONATO_INICIADO",
                    "Não é possível remover um time depois do início do campeonato.");
        }

        inscricao.recusar(motivo, usuarioAtualId);

        return InscricaoResponseDTO.de(inscricao);
    }

    @Transactional
    public InscricaoResponseDTO remover(UUID inscricaoId, String motivo, UUID usuarioAtualId) {
        Inscricao inscricao = buscarInscricaoAutorizada(inscricaoId, usuarioAtualId);

        inscricao.remover(motivo);
        inscricao.getCampeonato().liberarVaga();

        return InscricaoResponseDTO.de(inscricao);
    }

    @Transactional(readOnly = true)
    public Page<InscricaoResponseDTO> listar(
            UUID campeonatoId,
            StatusInscricao status,
            Pageable pageable,
            UUID usuarioAtualId) {

        Campeonato campeonato = buscarCampeonato(campeonatoId);
        verificarOrganizador(campeonato, usuarioAtualId);

        Page<Inscricao> inscricoes;

        if (status != null) {
            inscricoes = inscricaoRepository.findByCampeonatoIdAndStatus(
                    campeonatoId, status, pageable);
        } else {
            inscricoes = inscricaoRepository.findByCampeonatoId(
                    campeonatoId, pageable);
        }

        return inscricoes.map(InscricaoResponseDTO::de);
    }

    // Métodos auxiliares

    // Busca a inscrição e verifica se o usuário atual é o organizador do campeonato
    private Inscricao buscarInscricaoAutorizada(UUID inscricaoId, UUID usuarioAtualId) {
        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new RegraNegocioException(
                        HttpStatus.NOT_FOUND,
                        "NAO_ENCONTRADO",
                        "Inscrição não encontrada."));

        verificarOrganizador(inscricao.getCampeonato(), usuarioAtualId);

        return inscricao;
    }

    // Busca o campeonato pelo ID e lança exceção se não encontrado
    private Campeonato buscarCampeonato(UUID campeonatoId) {
        return campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new RegraNegocioException(
                        HttpStatus.NOT_FOUND,
                        "NAO_ENCONTRADO",
                        "Campeonato não encontrado."));
    }

    // Verifica se o usuário atual é o organizador do campeonato
    private void verificarOrganizador(Campeonato campeonato, UUID usuarioAtualId) {
        if (usuarioAtualId == null || !usuarioAtualId.equals(campeonato.getOrganizadorId())) {
            throw new RegraNegocioException(
                    HttpStatus.FORBIDDEN,
                    "ACESSO_NEGADO",
                    "Somente o organizador do campeonato pode realizar esta operação.");
        }
    }
}