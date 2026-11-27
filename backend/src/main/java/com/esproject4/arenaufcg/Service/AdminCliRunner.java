package com.esproject4.arenaufcg.Service;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;

@Component
@ConditionalOnProperty(name = "admin.cli.action")
public class AdminCliRunner implements ApplicationRunner {

    private final AdminUsuarioService adminUsuarioService;
    private final Environment environment;
    private final ConfigurableApplicationContext context;

    public AdminCliRunner(
            AdminUsuarioService adminUsuarioService,
            Environment environment,
            ConfigurableApplicationContext context) {

        this.adminUsuarioService = adminUsuarioService;
        this.environment = environment;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {

        int codigoSaida = 0;

        try {
            if (!"none".equalsIgnoreCase(
                    environment.getProperty("spring.main.web-application-type"))) {

                throw new IllegalArgumentException(
                    "A CLI deve ser executada sem iniciar o servidor web."
                );
            }

            String acao = environment.getProperty("admin.cli.action");

            switch (acao) {
                case "create" -> criarAdministrador();
                case "deactivate" -> desativarAdministrador();

                default -> throw new IllegalArgumentException(
                    "Ação inválida. Utilize create ou deactivate."
                );
            }

        } catch (RegraNegocioException exception) {

            System.err.println(
                exception.getCodigo() + ": " + exception.getMessage()
            );

            codigoSaida = 1;

        } catch (IllegalArgumentException exception) {

            System.err.println(exception.getMessage());
            codigoSaida = 1;

        } catch (Exception exception) {

            System.err.println(
                "Não foi possível concluir a operação administrativa."
            );

            codigoSaida = 1;
        }

        final int resultado = codigoSaida;

        int exitCode = SpringApplication.exit(
            context,
            () -> resultado
        );

        System.exit(exitCode);
    }

    private void criarAdministrador() {

        String nome = obterArgumento(
            "admin.cli.nome",
            "Nome"
        );

        String email = obterArgumento(
            "admin.cli.email",
            "E-mail"
        );

        String senha = obterSenha();

        var administrador = adminUsuarioService.criarAdministrador(
            nome,
            email,
            senha
        );

        System.out.println("Administrador criado com sucesso.");
        System.out.println("ID: " + administrador.id());
        System.out.println("E-mail: " + administrador.email());
    }

    private void desativarAdministrador() {

        String email = obterArgumento(
            "admin.cli.email",
            "E-mail"
        );

        adminUsuarioService.desativarAdministrador(email);

        System.out.println("Administrador desativado com sucesso.");
    }

    private String obterArgumento(
            String propriedade,
            String mensagem) {

        String valor = environment.getProperty(propriedade);

        if (valor != null) {
            return valor;
        }

        return lerEntrada(mensagem);
    }

    private String obterSenha() {

        String senha = environment.getProperty("admin.cli.senha");

        if (senha != null) {
            return senha;
        }

        Console console = System.console();

        if (console != null) {
            char[] caracteres = console.readPassword("Senha: ");

            if (caracteres == null) {
                throw new IllegalArgumentException("Senha não informada.");
            }

            try {
                return new String(caracteres);
            } finally {
                Arrays.fill(caracteres, '\0');
            }
        }

        System.out.println("Aviso: a senha será visível durante a digitação.");

        return lerEntrada("Senha");
    }

    private Scanner scanner;

    private String lerEntrada(String mensagem) {

        Console console = System.console();

        if (console != null) {
            return console.readLine("%s: ", mensagem);
        }

        if (scanner == null) {
            scanner = new Scanner(System.in);
        }

        System.out.print(mensagem + ": ");

        if (!scanner.hasNextLine()) {
            throw new IllegalArgumentException(
                "Entrada não informada."
            );
        }

        return scanner.nextLine();
    }
}