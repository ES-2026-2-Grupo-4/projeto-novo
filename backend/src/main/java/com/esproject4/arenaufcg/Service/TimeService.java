package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.DTO.CriarTimeRequest;
import com.esproject4.arenaufcg.DTO.EditarTimeRequest;
import com.esproject4.arenaufcg.DTO.TimeResponse;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.Time;
import com.esproject4.arenaufcg.Repository.TimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TimeService {

    private final TimeRepository timeRepository;

    public TimeService(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    // Cadastra um novo time; o dirigente é sempre o usuário autenticado.
    @Transactional
    public TimeResponse criar(CriarTimeRequest request, UUID dirigenteId) {
        Time time = new Time(
                request.nome(),
                request.sigla(),
                request.modalidade(),
                dirigenteId
        );
        return TimeResponse.from(timeRepository.save(time));
    }

    // Retorna os times ativos do dirigente.
    @Transactional(readOnly = true)
    public List<TimeResponse> meusTimes(UUID dirigenteId) {
        return timeRepository.findByDirigenteIdAndAtivoTrue(dirigenteId)
                .stream()
                .map(TimeResponse::from)
                .toList();
    }

    // Edita nome e sigla; só o dono do time pode editar.
    @Transactional
    public TimeResponse editar(UUID teamId, EditarTimeRequest request, UUID dirigenteId) {
        Time time = timeRepository.findById(teamId)
                .orElseThrow(() -> new RegraNegocioException(
                        HttpStatus.NOT_FOUND,
                        "NAO_ENCONTRADO",
                        "Time não encontrado."
                ));

        if (!time.pertenceA(dirigenteId)) {
            throw new RegraNegocioException(
                    HttpStatus.FORBIDDEN,
                    "ACESSO_NEGADO",
                    "Você não tem permissão para editar este time."
            );
        }

        time.editar(request.nome(), request.sigla());
        return TimeResponse.from(timeRepository.save(time));
    }
}
