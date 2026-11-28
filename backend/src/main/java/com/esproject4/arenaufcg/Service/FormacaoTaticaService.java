package com.esproject4.arenaufcg.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Escalacao;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.Partida;
import com.esproject4.arenaufcg.Model.RegraModalidade;
import com.esproject4.arenaufcg.Model.Time;
import com.esproject4.arenaufcg.Repository.EscalacaoRepository;
import com.esproject4.arenaufcg.Repository.PartidaRepository;
import com.esproject4.arenaufcg.DTO.FormacoesResponseDTO;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FormacaoTaticaService {

    private final PermissaoPartida permissaoPartida;
    private final PartidaRepository partidaRepository;
    private final EscalacaoRepository escalacaoRepository;
    private final Clock clock;

    @Transactional
    public Escalacao definirFormacao(UUID partidaId, UUID timeId, String formacao, UUID usuarioId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new RegraNegocioException(HttpStatus.NOT_FOUND,
                        "NAO_ENCONTRADO", "Partida não encontrada."));

        // 1) permissão: o time joga a partida e o usuário é o dirigente dele (403)
        Time time = permissaoPartida.exigirDirigenteDoTime(partida, timeId, usuarioId);

        Escalacao escalacao = escalacaoRepository.findByPartidaIdAndTimeId(partidaId, timeId)
                .orElseGet(() -> escalacaoRepository.save(new Escalacao(partida, time)));

        // 2) e 3) fechada (409) e formação da lista (422): a entidade faz os dois
        escalacao.definirFormacao(formacao, OffsetDateTime.now(clock));

        // 4) compatibilidade com os titulares já escalados
        RegraModalidade regra = RegraModalidade.de(time.getModalidade());
        int titulares = escalacao.contarTitulares();
        if (titulares >= regra.getTitulares()
                && titulares != regra.titularesDaFormacao(formacao)) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "FORMACAO_INCOMPATIVEL", "A formação não bate com o número de titulares.");
        }

        return escalacao;
    }

    public FormacoesResponseDTO listarFormacoes(Modalidade modalidade) {
        RegraModalidade regra = RegraModalidade.de(modalidade);
        return new FormacoesResponseDTO(modalidade, regra.getTitulares(),
                regra.getMaxRelacionados(), regra.getFormacoes());
    }
}