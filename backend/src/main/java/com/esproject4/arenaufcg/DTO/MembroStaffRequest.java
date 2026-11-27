package com.esproject4.arenaufcg.DTO;

import com.esproject4.arenaufcg.Model.FuncaoStaff;
import com.esproject4.arenaufcg.Model.MembroStaff;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MembroStaffRequest(
        @NotBlank @Size(max = MembroStaff.TAMANHO_MAX_NOME) String nome,
        @NotNull FuncaoStaff funcao
) {}
