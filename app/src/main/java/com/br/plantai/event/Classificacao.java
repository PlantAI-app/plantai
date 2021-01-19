package com.br.plantai.event;

public class Classificacao {

    private String especie;
    private String acuracia;

    public Classificacao(String especie, String acuracia) {
        this.especie = especie;
        this.acuracia = acuracia;
    }

    public String getAcuracia() {
        return acuracia;
    }

    public void setAcuracia(String acuracia) {
        this.acuracia = acuracia;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }
}
