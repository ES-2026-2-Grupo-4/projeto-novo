package com.esproject4.arenaufcg.Service;

import org.springframework.stereotype.Service;
import com.esproject4.arenaufcg.Repository.TimeRepository;
import com.esproject4.arenaufcg.Model.Time;
import java.util.List;
import com.esproject4.arenaufcg.DTO.CriarTimeRequest;
import com.esproject4.arenaufcg.DTO.TimeResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;

@Service 
public class TimeService {
    
    private final TimeRepository timeRepository;

    public TimeService(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    // Cadastra um novo time vinculado ao dirigente autenticado.
    public TimeResponse criar(CriarTimeRequest request, UUID dirigenteId) {
    Time time = new Time(
        request.nome(),
        request.sigla(),
        request.modalidade(),
        dirigenteId
    );

    Time timeSalvo = timeRepository.save(time);

    return TimeResponse.from(timeSalvo);
    
    }

    // Retorna todos os times pertencentes ao dirigente informado.
    public List<TimeResponse> meusTimes(UUID dirigenteId) {
    return timeRepository.findByDirigenteId(dirigenteId)
            .stream()
            .map(TimeResponse::from)
            .toList();
    }

    public TimeResponse editar(Long id, CriarTimeRequest request, UUID dirigenteId) {

    Time time = timeRepository.findById(id)
            .orElseThrow(() -> new RegraNegocioException(
                    HttpStatus.NOT_FOUND,
                    "TIME_NAO_ENCONTRADO",
                    "Time não encontrado."
            ));

    if (!time.getDirigenteId().equals(dirigenteId)) {
        throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO",
                "Você não tem permissão para editar este time."
        );
    }

    time.setNome(request.nome());
    time.setSigla(request.sigla());
    time.setModalidade(request.modalidade());

    Time timeSalvo = timeRepository.save(time);

    return TimeResponse.from(timeSalvo);
    
}

}
