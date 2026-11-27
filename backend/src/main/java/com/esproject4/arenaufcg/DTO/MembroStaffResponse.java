package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.FuncaoStaff;
import com.esproject4.arenaufcg.Model.MembroStaff;
import java.util.UUID;

public record MembroStaffResponse(UUID id, UUID timeId, String nome, FuncaoStaff funcao, boolean ativo) {
    public static MembroStaffResponse de(MembroStaff membro) {
        return new MembroStaffResponse(membro.getId(), membro.getTimeId(), membro.getNome(),
                membro.getFuncao(), membro.isAtivo());
    }
}
