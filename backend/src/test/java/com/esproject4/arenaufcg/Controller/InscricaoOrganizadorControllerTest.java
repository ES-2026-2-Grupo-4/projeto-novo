package com.esproject4.arenaufcg.Controller;

import com.esproject4.arenaufcg.DTO.InscricaoResponseDTO;
import com.esproject4.arenaufcg.DTO.UserResponseDTO;
import com.esproject4.arenaufcg.Model.UserRole;
import com.esproject4.arenaufcg.Service.AuthService;
import com.esproject4.arenaufcg.Model.StatusInscricao;
import com.esproject4.arenaufcg.Service.InscricaoOrganizadorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InscricaoOrganizadorControllerTest {

    @Mock
    private InscricaoOrganizadorService service;

    @Mock
    private AuthService authService;

    private static final String TOKEN = "Bearer token-teste";

    private MockMvc mockMvc;

    private UUID inscricaoId;
    private UUID organizadorId;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
            .standaloneSetup(new InscricaoOrganizadorController(service, authService))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(validator)
            .build();

        inscricaoId = UUID.randomUUID();
        organizadorId = UUID.randomUUID();

        // O controller descobre o usuário pelo token (login da T02).
        lenient().when(authService.getSessionUser(TOKEN)).thenReturn(
            new UserResponseDTO(organizadorId, "Organizador", "org@ufcg.edu.br",
                                UserRole.ORGANIZADOR, true, OffsetDateTime.now()));
    }

    // Método auxiliar para criar DTOs de resposta
    private InscricaoResponseDTO criarResposta(StatusInscricao status, String motivo) {
        return new InscricaoResponseDTO(
            inscricaoId,
            new InscricaoResponseDTO.CampeonatoResumo(
                UUID.randomUUID(),
                "Campeonato Universitário"
            ),
            new InscricaoResponseDTO.TimeResumo(
                UUID.randomUUID(),
                "Time A",
                "TMA"
            ),
            status,
            motivo,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }

    // TESTE 1: Aprovar inscrição

    @Test
    void deveAprovarInscricao() throws Exception {
        InscricaoResponseDTO resposta =
            criarResposta(StatusInscricao.APROVADA, null);

        when(service.aprovar(inscricaoId, organizadorId))
            .thenReturn(resposta);

        mockMvc.perform(
            post("/api/v1/registrations/{id}/approve", inscricaoId)
                .header("Authorization", TOKEN)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(inscricaoId.toString()))
        .andExpect(jsonPath("$.status").value("APROVADA"));

        verify(service).aprovar(inscricaoId, organizadorId);
    }

    // TESTE 2: Listar com filtro e paginação

    @Test
    void deveListarInscricoesComFiltroEPaginacao() throws Exception {
        UUID campeonatoId = UUID.randomUUID();

        Page<InscricaoResponseDTO> pagina =
            Page.empty(PageRequest.of(1, 10));

        when(service.listar(
            eq(campeonatoId),
            eq(StatusInscricao.PENDENTE),
            any(Pageable.class),
            eq(organizadorId)
        )).thenReturn(pagina);

        mockMvc.perform(
            get("/api/v1/championships/{campeonatoId}/registrations", campeonatoId)
                .param("status", "PENDENTE")
                .param("page", "1")
                .param("size", "10")
                .header("Authorization", TOKEN)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<Pageable> captor =
            ArgumentCaptor.forClass(Pageable.class);

        verify(service).listar(
            eq(campeonatoId),
            eq(StatusInscricao.PENDENTE),
            captor.capture(),
            eq(organizadorId)
        );

        Pageable pageable = captor.getValue();

        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    // TESTE 3: Listar sem filtro

    @Test
    void deveListarInscricoesSemFiltro() throws Exception {
        UUID campeonatoId = UUID.randomUUID();

        Page<InscricaoResponseDTO> pagina =
            Page.empty(PageRequest.of(0, 20));

        when(service.listar(
            eq(campeonatoId),
            isNull(),
            any(Pageable.class),
            eq(organizadorId)
        )).thenReturn(pagina);

        mockMvc.perform(
            get("/api/v1/championships/{campeonatoId}/registrations", campeonatoId)
                .header("Authorization", TOKEN)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<Pageable> captor =
            ArgumentCaptor.forClass(Pageable.class);

        verify(service).listar(
            eq(campeonatoId),
            isNull(),
            captor.capture(),
            eq(organizadorId)
        );

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    // TESTE 4: Recusar inscrição

    @Test
    void deveRecusarInscricao() throws Exception {
        String motivo = "Documentação incompleta";

        InscricaoResponseDTO resposta =
            criarResposta(StatusInscricao.RECUSADA, motivo);

        when(service.recusar(inscricaoId, motivo, organizadorId))
            .thenReturn(resposta);

        mockMvc.perform(
            post("/api/v1/registrations/{id}/reject", inscricaoId)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Documentação incompleta\"}")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("RECUSADA"))
        .andExpect(jsonPath("$.motivo").value(motivo));

        verify(service).recusar(inscricaoId, motivo, organizadorId);
    }

    // TESTE 5: Remover inscrição

    @Test
    void deveRemoverInscricao() throws Exception {
        String motivo = "Descumprimento das regras";

        InscricaoResponseDTO resposta =
            criarResposta(StatusInscricao.REMOVIDA, motivo);

        when(service.remover(inscricaoId, motivo, organizadorId))
            .thenReturn(resposta);

        mockMvc.perform(
            post("/api/v1/registrations/{id}/remove", inscricaoId)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Descumprimento das regras\"}")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REMOVIDA"))
        .andExpect(jsonPath("$.motivo").value(motivo));

        verify(service).remover(inscricaoId, motivo, organizadorId);
    }

    // TESTE 6: Impedir recusa sem motivo

    @Test
    void naoDeveRecusarInscricaoSemMotivo() throws Exception {
        mockMvc.perform(
            post("/api/v1/registrations/{id}/reject", inscricaoId)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"\"}")
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // TESTE 7: Impedir remoção sem motivo

    @Test
    void naoDeveRemoverInscricaoSemMotivo() throws Exception {
        mockMvc.perform(
            post("/api/v1/registrations/{id}/remove", inscricaoId)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"   \"}")
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // TESTE 8: Impedir motivo com mais de 500 caracteres

    @Test
    void naoDeveRecusarInscricaoComMotivoMuitoLongo() throws Exception {
        String motivo = "A".repeat(501);

        mockMvc.perform(
            post("/api/v1/registrations/{id}/reject", inscricaoId)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"" + motivo + "\"}")
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}