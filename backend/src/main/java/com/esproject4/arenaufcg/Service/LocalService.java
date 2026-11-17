package com.esproject4.arenaufcg.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Model.TipoLocal;
import com.esproject4.arenaufcg.Repository.LocalRepository;

@Service
public class LocalService {

    private final LocalRepository localRepository;
    private final ConsultaOrganizador consultaOrganizador;

    public LocalService(
            LocalRepository localRepository,
            ConsultaOrganizador consultaOrganizador) {

        this.localRepository = localRepository;
        this.consultaOrganizador = consultaOrganizador;
    }

    @Transactional
    public Local cadastrar(UUID usuarioId, String nome, String endereco, TipoLocal tipo) {
        if (!consultaOrganizador.isOrganizador(usuarioId)) {
            throw new RegraNegocioException(
                HttpStatus.FORBIDDEN,
                "NAO_ORGANIZADOR",
                "Somente organizadores podem cadastrar locais."
            );
        }

        return localRepository.save(new Local(nome, endereco, tipo));
    }

    @Transactional(readOnly = true)
    public List<Local> listar() {
        return localRepository.findAll();
    }
}
