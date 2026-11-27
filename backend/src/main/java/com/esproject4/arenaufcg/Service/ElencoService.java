package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.DTO.AtualizarElencoRequest;
import com.esproject4.arenaufcg.DTO.ElencoRequest;
import com.esproject4.arenaufcg.DTO.ElencoResponse;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Atleta;
import com.esproject4.arenaufcg.Model.AtletaElenco;
import com.esproject4.arenaufcg.Model.Inscricao;
import com.esproject4.arenaufcg.Model.StatusInscricao;
import com.esproject4.arenaufcg.Repository.AtletaElencoRepository;
import com.esproject4.arenaufcg.Repository.AtletaRepository;
import com.esproject4.arenaufcg.Repository.CampeonatoRepository;
import com.esproject4.arenaufcg.Repository.InscricaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ElencoService {

    private final InscricaoRepository inscricoes;
    private final AtletaRepository atletas;
    private final AtletaElencoRepository elenco;
    private final CampeonatoRepository campeonatos;

    public ElencoService(InscricaoRepository inscricoes, AtletaRepository atletas,
                         AtletaElencoRepository elenco, CampeonatoRepository campeonatos) {
        this.inscricoes = inscricoes;
        this.atletas = atletas;
        this.elenco = elenco;
        this.campeonatos = campeonatos;
    }

    public ElencoResponse adicionar(UUID inscricaoId, UUID usuarioId, ElencoRequest request) {
        campeonatos.bloquearPorInscricao(inscricaoId).orElseThrow(() -> naoEncontrado("Inscrição"));
        Inscricao inscricao = buscarInscricao(inscricaoId);
        exigirEdicao(inscricao, usuarioId);
        Atleta atleta = atletas.findByIdAndAtivoTrue(request.atletaId())
                .orElseThrow(() -> naoEncontrado("Atleta ativo"));

        // O construtor valida, nesta ordem, time do atleta, posição e faixa de números.
        AtletaElenco entrada = new AtletaElenco(inscricao, atleta, request.numero(), request.posicao());
        exigirNumeroLivre(inscricaoId, entrada.getNumero());
        if (elenco.existsByCampeonatoIdAndAtletaIdAndAtivoTrue(entrada.getCampeonatoId(), atleta.getId())) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "ATLETA_JA_NO_CAMPEONATO",
                    "O atleta já está em um elenco ativo deste campeonato.");
        }
        return ElencoResponse.de(elenco.save(entrada));
    }

    @Transactional(readOnly = true)
    public List<ElencoResponse> listar(UUID inscricaoId, UUID usuarioId) {
        Inscricao inscricao = buscarInscricao(inscricaoId);
        if (!inscricao.getTime().pertenceA(usuarioId) && !inscricao.getCampeonato().pertenceA(usuarioId)) {
            throw new RegraNegocioException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO",
                    "Apenas o dirigente do time ou o organizador do campeonato pode consultar o elenco.");
        }
        return elenco.findByInscricaoIdAndAtivoTrueOrderByNumero(inscricaoId).stream()
                .map(ElencoResponse::de).toList();
    }

    public ElencoResponse atualizar(UUID entradaId, UUID usuarioId, AtualizarElencoRequest request) {
        AtletaElenco entrada = buscarParaEdicao(entradaId, usuarioId);
        if (entrada.getNumero() != request.numero()) {
            exigirNumeroLivre(entrada.getInscricao().getId(), request.numero());
        }
        entrada.atualizar(request.numero(), request.posicao());
        return ElencoResponse.de(entrada);
    }

    public void remover(UUID entradaId, UUID usuarioId) {
        buscarParaEdicao(entradaId, usuarioId).remover();
    }

    private Inscricao buscarInscricao(UUID id) {
        return inscricoes.findById(id).orElseThrow(() -> naoEncontrado("Inscrição"));
    }

    private AtletaElenco buscarParaEdicao(UUID id, UUID usuarioId) {
        campeonatos.bloquearPorEntrada(id).orElseThrow(() -> naoEncontrado("Entrada do elenco"));
        AtletaElenco entrada = elenco.findByIdAndAtivoTrue(id).orElseThrow(() -> naoEncontrado("Entrada do elenco"));
        exigirEdicao(entrada.getInscricao(), usuarioId);
        return entrada;
    }

    private void exigirEdicao(Inscricao inscricao, UUID usuarioId) {
        if (!inscricao.getTime().pertenceA(usuarioId)) {
            throw new RegraNegocioException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO",
                    "Apenas o dirigente do time pode alterar o elenco.");
        }
        if (inscricao.getStatus() != StatusInscricao.PENDENTE && inscricao.getStatus() != StatusInscricao.APROVADA) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "INSCRICAO_NAO_ATIVA",
                    "A inscrição deve estar pendente ou aprovada.");
        }
        if (inscricao.getCampeonato().jaIniciou()) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "CAMPEONATO_INICIADO",
                    "O elenco só pode ser alterado antes do início do campeonato.");
        }
    }

    private void exigirNumeroLivre(UUID inscricaoId, int numero) {
        if (elenco.existsByInscricaoIdAndNumeroAndAtivoTrue(inscricaoId, numero)) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "NUMERO_EM_USO",
                    "Outro atleta do elenco já utiliza esse número.");
        }
    }

    private RegraNegocioException naoEncontrado(String recurso) {
        return new RegraNegocioException(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", recurso + ": registro inexistente ou inativo.");
    }
}
