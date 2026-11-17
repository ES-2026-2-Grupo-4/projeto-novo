package com.esproject4.arenaufcg.Service;

import java.util.List;

import com.esproject4.arenaufcg.Model.Local;
import com.esproject4.arenaufcg.Model.TipoLocal;
import com.esproject4.arenaufcg.Repository.LocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocalService {

    private final LocalRepository localRepository;

    public LocalService(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    @Transactional
    public Local cadastrar(String nome, String endereco, TipoLocal tipo) {
        Local local = new Local(nome, endereco, tipo);

        return localRepository.save(local);
    }

    @Transactional(readOnly = true)
    public List<Local> listar() {
       return localRepository.findAll();
    }
}
