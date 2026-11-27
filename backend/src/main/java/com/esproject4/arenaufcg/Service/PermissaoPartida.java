package com.esproject4.arenaufcg.Service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Partida;
import com.esproject4.arenaufcg.Model.Time;

// Regras de "quem pode mexer" na escalação e no mando de uma partida.
// O usuarioId vem do controller: usuarioAutenticado.exigir(authorization).
@Component
public class PermissaoPartida {

    // O time precisa jogar a partida e o usuário precisa ser o dirigente dele.
    public Time exigirDirigenteDoTime(Partida partida, UUID timeId, UUID usuarioId) {
        Time time;
        if (partida.ehMandante(timeId)) {
            time = partida.getMandante();
        } else if (partida.ehVisitante(timeId)) {
            time = partida.getVisitante();
        } else {
            throw acessoNegado("Este time não joga esta partida.");
        }

        if (!time.pertenceA(usuarioId)) {
            throw acessoNegado("Você não é o dirigente deste time.");
        }
        return time;
    }

    // Só o dirigente do time mandante.
    public Time exigirMandante(Partida partida, UUID usuarioId) {
        Time mandante = partida.getMandante();
        if (!mandante.pertenceA(usuarioId)) {
            throw acessoNegado("Apenas o dirigente do time mandante pode fazer isso.");
        }
        return mandante;
    }

    private static RegraNegocioException acessoNegado(String mensagem) {
        return new RegraNegocioException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", mensagem);
    }
}
