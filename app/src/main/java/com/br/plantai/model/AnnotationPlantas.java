package com.br.plantai.model;

import android.annotation.SuppressLint;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class AnnotationPlantas {

    // laranja - em perigo de extinção
    public static final String araucaria = "laranja";
    public static final String guapeba = "laranja";
    public static final String guatambu = "laranja";
    // verde - quase ameacada
    public static final String ipe_roxo = "verde";
    // branco - nao avaliada
    public static final String guapuruvu = "branco";
    public static final String jequetiba_vermelho = "branco";
    public static final String pitanga = "branco";
    private String planta;

    @PlantaMarcador
    public String getPlanta() {
        return planta;
    }

    public void setPlanta(@PlantaMarcador String plantaEspecie) {
        this.planta = plantaEspecie;
    }

    @SuppressLint("UniqueConstants")
    @Retention(SOURCE)
    @StringDef({araucaria,
            guapeba,
            guapuruvu,
            guatambu,
            ipe_roxo,
            jequetiba_vermelho,
            pitanga})

    public @interface PlantaMarcador {
    }

}



