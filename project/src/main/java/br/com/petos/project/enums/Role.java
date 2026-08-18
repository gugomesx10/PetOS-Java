package br.com.petos.project.enums;

public enum Role {
    TUTOR,
    CLINICA;

    public String authority() {
        return "ROLE_" + name();
    }
}

