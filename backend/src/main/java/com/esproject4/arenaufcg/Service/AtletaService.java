package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.DTO.AtletaRequest;
import com.esproject4.arenaufcg.DTO.AtletaResponse;
import com.esproject4.arenaufcg.Model.Atleta;
import com.esproject4.arenaufcg.Model.Posicao;
import com.esproject4.arenaufcg.Model.Time;
import com.esproject4.arenaufcg.Repository.AtletaElencoRepository;
import com.esproject4.arenaufcg.Repository.AtletaRepository;
import com.esproject4.arenaufcg.Repository.TimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AtletaService {

    private final AtletaRepository atletas;
    private final TimeRepository times;
    private final AtletaElencoRepository elenco;

    public AtletaService(AtletaRepository atletas, TimeRepository times, AtletaElencoRepository elenco) {
        this.atletas = atletas;
        this.times = times;
        this.elenco = elenco;
    }

    public AtletaResponse cadastrar(UUID timeId, UUID usuarioId, AtletaRequest request) {
        Time time = exigirTimeDoDirigente(timeId, usuarioId);
        validarPosicao(request.posicaoPreferida(), time);
        Atleta atleta = new Atleta(time, request.nome(), request.apelido(),
                request.dataNascimento(), request.posicaoPreferida());
        return AtletaResponse.de(atletas.save(atleta));
    }

    @Transactional(readOnly = true)
    public List<AtletaResponse> listarDoTime(UUID timeId, UUID usuarioId) {
        exigirTimeDoDirigente(timeId, usuarioId);
        return atletas.findByTimeIdAndAtivoTrue(timeId).stream().map(AtletaResponse::de).toList();
    }

    public AtletaResponse atualizar(UUID atletaId, UUID usuarioId, AtletaRequest request) {
        Atleta atleta = buscarAtivo(atletaId);
        Time time = exigirTimeDoDirigente(atleta.getTimeId(), usuarioId);
        validarPosicao(request.posicaoPreferida(), time);
        atleta.atualizar(request.nome(), request.apelido(), request.dataNascimento(), request.posicaoPreferida());
        return AtletaResponse.de(atleta);
    }

    public void remover(UUID atletaId, UUID usuarioId) {
        Atleta atleta = buscarAtivo(atletaId);
        exigirTimeDoDirigente(atleta.getTimeId(), usuarioId);
        if (elenco.atletaEmElencoAtivoNaoFinalizado(atletaId)) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "ATLETA_EM_ELENCO_ATIVO",
                    "O atleta está em um elenco ativo de campeonato não finalizado.");
        }
        atleta.remover();
    }

    private Atleta buscarAtivo(UUID atletaId) {
        return atletas.findByIdAndAtivoTrue(atletaId).orElseThrow(() ->
                new RegraNegocioException(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", "Atleta não encontrado."));
    }

    private Time exigirTimeDoDirigente(UUID timeId, UUID usuarioId) {
        Time time = times.findById(timeId).orElseThrow(() ->
                new RegraNegocioException(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", "Time não encontrado."));
        if (!time.pertenceA(usuarioId)) {
            throw new RegraNegocioException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO",
                    "Apenas o dirigente do time pode gerenciar seus atletas.");
        }
        return time;
    }

    private void validarPosicao(Posicao posicao, Time time) {
        if (posicao != null && !posicao.aceita(time.getModalidade())) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "POSICAO_INVALIDA",
                    "A posição preferida não pertence à modalidade do time.");
        }
    }
}
