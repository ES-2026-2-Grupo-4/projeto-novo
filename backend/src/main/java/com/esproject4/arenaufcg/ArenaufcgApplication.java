package com.esproject4.arenaufcg;

import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ArenaufcgApplication {

    public static void main(String[] args) {

        SpringApplication application =
            new SpringApplication(ArenaufcgApplication.class);

        application.addInitializers(context ->
            validarConfiguracaoCli(context.getEnvironment())
        );

        application.run(args);
    }

    private static void validarConfiguracaoCli(Environment environment) {

        if (!environment.containsProperty("admin.cli.action")) {
            return;
        }

        String modo = environment.getProperty(
            "spring.main.web-application-type", ""
        );

        if (!"none".equalsIgnoreCase(modo)) {
            throw new IllegalStateException(
                "A CLI deve ser executada sem iniciar o servidor web."
            );
        }

        String ddlAuto = environment.getProperty(
            "spring.jpa.hibernate.ddl-auto", ""
        );

        if (!Set.of("update", "validate", "none").contains(ddlAuto)) {
            throw new IllegalStateException(
                "Configuração inválida para a CLI. " +
                "Utilize ddl-auto=update, validate ou none."
            );
        }
    }
}