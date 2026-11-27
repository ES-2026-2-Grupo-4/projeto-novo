package com.esproject4.arenaufcg.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esproject4.arenaufcg.DTO.InscricaoResponseDTO;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Campeonato;
import com.esproject4.arenaufcg.Model.Inscricao;
import com.esproject4.arenaufcg.Model.StatusInscricao;
import com.esproject4.arenaufcg.Model.Time;
import com.esproject4.arenaufcg.Repository.CampeonatoRepository;
import com.esproject4.arenaufcg.Repository.InscricaoRepository;
import com.esproject4.arenaufcg.Repository.TimeRepository;

@Service
public class InscricaoDirigenteService {
    
    private final InscricaoRepository inscricaoRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final TimeRepository timeRepository;

    public InscricaoDirigenteService(
        InscricaoRepository inscricaoRepository, 
        CampeonatoRepository campeonatoRepository, 
        TimeRepository timeRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.campeonatoRepository = campeonatoRepository;
        this.timeRepository = timeRepository;
    }

    @Transactional
    public InscricaoResponseDTO solicitar(
            UUID campeonatoId,
            UUID timeId,
            UUID dirigenteId) {

        Campeonato campeonato = buscarCampeonato(campeonatoId);
        Time time = buscarTime(timeId);

        verificarDirigente(time, dirigenteId);

        if (!campeonato.aceitaInscricoes()) {
            throw new RegraNegocioException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "INSCRICOES_FECHADAS",
                    "As inscrições para este campeonato estão fechadas."
            );
        }

        if (time.getModalidade() != campeonato.getModalidade()) {
            throw new RegraNegocioException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "MODALIDADE_DIFERENTE",
                    "A modalidade do time é diferente da modalidade do campeonato."
            );
        }

        boolean inscricaoExistente = inscricaoRepository
                .existsByCampeonatoIdAndTimeIdAndStatusIn(
                        campeonatoId,
                        timeId,
                        List.of(StatusInscricao.PENDENTE, StatusInscricao.APROVADA)
                );

        if (inscricaoExistente) {
            throw new RegraNegocioException(
                    HttpStatus.CONFLICT,
                    "INSCRICAO_ATIVA_EXISTENTE",
                    "Este time já possui uma inscrição ativa neste campeonato."
            );
        }

        Inscricao inscricao = new Inscricao(campeonato, time);

        return InscricaoResponseDTO.de(inscricaoRepository.save(inscricao));
    }

    @Transactional
    public InscricaoResponseDTO cancelar(
            UUID inscricaoId,
            UUID dirigenteId) {

        Inscricao inscricao = buscarInscricao(inscricaoId);

        verificarDirigente(inscricao.getTime(), dirigenteId);

        inscricao.cancelarPeloDirigente();

        return InscricaoResponseDTO.de(inscricaoRepository.save(inscricao));
    }

    @Transactional(readOnly = true)
    public List<InscricaoResponseDTO> listar(
            UUID timeId,
            UUID dirigenteId) {

        Time time = buscarTime(timeId);

        verificarDirigente(time, dirigenteId);

        return inscricaoRepository
                .findByTimeIdOrderBySolicitadaEmDesc(timeId)
                .stream()
                .map(InscricaoResponseDTO::de)
                .toList();
    }

    private Campeonato buscarCampeonato(UUID campeonatoId) {
        return campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new RegraNegocioException(
                        HttpStatus.NOT_FOUND,
                        "NAO_ENCONTRADO",
                        "Campeonato não encontrado."
                ));
    }

    private Time buscarTime(UUID timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new RegraNegocioException(
                        HttpStatus.NOT_FOUND,
                        "NAO_ENCONTRADO",
                        "Time não encontrado."
                ));
    }

    private Inscricao buscarInscricao(UUID inscricaoId) {
        return inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new RegraNegocioException(
                        HttpStatus.NOT_FOUND,
                        "NAO_ENCONTRADO",
                        "Inscrição não encontrada."
                ));
    }

    private void verificarDirigente(Time time, UUID dirigenteId) {
        if (!time.pertenceA(dirigenteId)) {
            throw new RegraNegocioException(
                    HttpStatus.FORBIDDEN,
                    "ACESSO_NEGADO",
                    "Você não tem permissão para realizar esta operação."
            );
        }
    }
}
