package com.esproject4.arenaufcg.Service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Partida;
import com.esproject4.arenaufcg.Repository.PartidaRepository;

@Service
public class MandoService {

    private final PartidaRepository partidaRepository;
    private final PermissaoPartida permissaoPartida;

    public MandoService(PartidaRepository partidaRepository, PermissaoPartida permissaoPartida) {
        this.partidaRepository = partidaRepository;
        this.permissaoPartida = permissaoPartida;
    }

    @Transactional
    public Partida definirLocal(UUID usuarioId, UUID partidaId, UUID localId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new RegraNegocioException(
                        HttpStatus.NOT_FOUND,
                        "PARTIDA_NAO_ENCONTRADA",
                        "Partida não encontrada."));

        // Só o dirigente do mandante pode definir o local (403 caso contrário).
        permissaoPartida.exigirMandante(partida, usuarioId);

        // O local tem que ser um dos locais cadastrados no campeonato.
        boolean localValido = partida.getCampeonato().getLocais().stream()
                .anyMatch(local -> local.getId().equals(localId));
        if (!localValido) {
            throw new RegraNegocioException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "LOCAL_INVALIDO",
                    "O local informado não pertence a este campeonato.");
        }

        // A Partida valida a editabilidade (só AGENDADA) e aplica a mudança.
        partida.definirLocal(localId);
        return partidaRepository.save(partida);
    }
}
