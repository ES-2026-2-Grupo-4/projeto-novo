package com.esproject4.arenaufcg.Security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.esproject4.arenaufcg.Model.PermissaoSistema;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequerPermissao {

    PermissaoSistema[] value();

    boolean somenteAdmin() default false;

    String acao() default "";
}