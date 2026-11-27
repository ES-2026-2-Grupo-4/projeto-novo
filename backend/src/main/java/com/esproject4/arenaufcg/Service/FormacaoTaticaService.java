package com.esproject4.arenaufcg.Service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.esproject4.arenaufcg.Model.Escalacao;
import com.esproject4.arenaufcg.Model.Modalidade;
import com.esproject4.arenaufcg.Model.RegraModalidade;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.User;
import com.esproject4.arenaufcg.Repository.EscalacaoRepository;
import com.esproject4.arenaufcg.Controller.UsuarioAutenticado;
import com.esproject4.arenaufcg.DTO.FormacoesResponseDTO;

// Dependência com T07 -- Partidas
@Service
@RequiredArgsConstructor
public class FormacaoTaticaService {

    private final PermissaoPartida permissaoPartida;
    private final EscalacaoRepository escalacaoRepository; 
    private final Clock clock;

   @Transactional
    public Escalacao definirFormacao(UUID partidaId, UUID timeId, String formacao, UsuarioAutenticado usuario) {
        // 1) permissão
        permissaoPartida.exigirDirigenteDoTime(partidaId, timeId, usuario);

        Escalacao escalacao = escalacaoRepository.findByPartidaIdAndTimeId(partidaId, timeId)
                .orElseThrow(() -> new RegraNegocioException(HttpStatus.NOT_FOUND,
                        "NAO_ENCONTRADO", "Escalação não encontrada."));

        // 2) escalação fechada
        if (escalacao.estaFechada(OffsetDateTime.now(clock))) {
            throw new RegraNegocioException(HttpStatus.CONFLICT,
                    "ESCALACAO_FECHADA", "A escalação desta partida já está fechada.");
        }

        // 3) valida e guarda a formação
        RegraModalidade regra = RegraModalidade.de(escalacao.getTime().getModalidade());
        escalacao.definirFormacao(formacao, regra);

        // 4) compatibilidade com os titulares já escalados  <-- o seu bloco entra aqui
        long titulares = escalacao.contarTitulares();
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
