package com.esproject4.arenaufcg.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.esproject4.arenaufcg.DTO.LogAuditoriaResponseDTO;
import com.esproject4.arenaufcg.DTO.RegistroLogCommand;
import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.LogAuditoria;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Repository.LogAuditoriaRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class AuditoriaService {

    private static final Logger logger =
        LoggerFactory.getLogger(AuditoriaService.class);

    private final LogAuditoriaRepository repository;
    private final TransactionTemplate transactionTemplate;

    public AuditoriaService(
            LogAuditoriaRepository repository,
            PlatformTransactionManager transactionManager) {

        this.repository = repository;

        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(
            TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
    }

    public void registrarLog(RegistroLogCommand comando) {
        try {
            transactionTemplate.executeWithoutResult(transaction -> {
                LogAuditoria log = new LogAuditoria(
                    comando.usuarioId(),
                    comando.usuarioEmail(),
                    comando.perfil(),
                    comando.acao(),
                    comando.recurso(),
                    comando.metodoHttp(),
                    comando.statusHttp(),
                    comando.detalhes(),
                    comando.ipOrigem()
                );

                repository.save(log);
            });
        } catch (RuntimeException exception) {
            logger.error(
                "Falha ao registrar log de auditoria. Tipo: {}",
                exception.getClass().getSimpleName()
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<LogAuditoriaResponseDTO> consultarLogs(
            UserRole perfil,
            String acao,
            Instant inicio,
            Instant fim,
            Pageable pageable) {

        validarPeriodo(inicio, fim);

        Specification<LogAuditoria> specification = (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();

            if (perfil != null) {
                filtros.add(cb.equal(root.get("perfil"), perfil));
            }

            if (acao != null && !acao.isBlank()) {
                filtros.add(cb.equal(root.get("acao"), acao));
            }

            if (inicio != null) {
                filtros.add(
                    cb.greaterThanOrEqualTo(root.<Instant>get("ocorridoEm"), inicio)
                );
            }

            if (fim != null) {
                filtros.add(
                    cb.lessThanOrEqualTo(root.<Instant>get("ocorridoEm"), fim)
                );
            }

            return cb.and(filtros.toArray(new Predicate[0]));
        };

        Pageable paginacao = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "ocorridoEm")
        );

        return repository.findAll(specification, paginacao)
            .map(LogAuditoriaResponseDTO::de);
    }

    private void validarPeriodo(Instant inicio, Instant fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RegraNegocioException(
                HttpStatus.BAD_REQUEST,
                "PARAMETRO_INVALIDO",
                "A data inicial não pode ser posterior à data final."
            );
        }
    }
}