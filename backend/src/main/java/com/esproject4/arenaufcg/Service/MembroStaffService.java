package com.esproject4.arenaufcg.Service;

import com.esproject4.arenaufcg.DTO.MembroStaffRequest;
import com.esproject4.arenaufcg.DTO.MembroStaffResponse;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.MembroStaff;
import com.esproject4.arenaufcg.Model.Time;
import com.esproject4.arenaufcg.Repository.MembroStaffRepository;
import com.esproject4.arenaufcg.Repository.TimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MembroStaffService {

    private final MembroStaffRepository membros;
    private final TimeRepository times;

    public MembroStaffService(MembroStaffRepository membros, TimeRepository times) {
        this.membros = membros;
        this.times = times;
    }

    public MembroStaffResponse cadastrar(UUID timeId, UUID usuarioId, MembroStaffRequest request) {
        Time time = buscarTimeDoDirigente(timeId, usuarioId);
        MembroStaff membro = new MembroStaff(time, request.nome(), request.funcao());
        return MembroStaffResponse.de(membros.save(membro));
    }

    @Transactional(readOnly = true)
    public List<MembroStaffResponse> listarDoTime(UUID timeId, UUID usuarioId) {
        buscarTimeDoDirigente(timeId, usuarioId);
        return membros.findByTimeIdAndAtivoTrue(timeId).stream().map(MembroStaffResponse::de).toList();
    }

    public MembroStaffResponse atualizar(UUID staffId, UUID usuarioId, MembroStaffRequest request) {
        MembroStaff membro = buscarMembroDoDirigente(staffId, usuarioId);
        membro.atualizar(request.nome(), request.funcao());
        return MembroStaffResponse.de(membro);
    }

    public void remover(UUID staffId, UUID usuarioId) {
        MembroStaff membro = buscarMembroDoDirigente(staffId, usuarioId);
        membro.remover();
    }

    private Time buscarTimeDoDirigente(UUID timeId, UUID usuarioId) {
        Time time = times.findById(timeId).orElseThrow(() ->
                new RegraNegocioException(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", "Time não encontrado."));
        exigirDirigente(time, usuarioId);
        return time;
    }

    private MembroStaff buscarMembroDoDirigente(UUID staffId, UUID usuarioId) {
        MembroStaff membro = membros.findByIdAndAtivoTrue(staffId).orElseThrow(() ->
                new RegraNegocioException(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", "Membro do staff não encontrado."));
        exigirDirigente(membro.getTime(), usuarioId);
        return membro;
    }

    private void exigirDirigente(Time time, UUID usuarioId) {
        if (!time.pertenceA(usuarioId)) {
            throw new RegraNegocioException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO",
                    "Apenas o dirigente do time pode gerenciar seu staff.");
        }
    }
}
