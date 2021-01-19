package com.br.plantai.util;

import android.annotation.SuppressLint;

import com.br.plantai.model.AnnotationPlantas;
import com.br.plantai.activity.R;

import static java.lang.String.format;

public class Utils {
    private final static String TAG = "Utils";

    /**
     * Format specie name, remove space and add underline
     *
     * @param planta user identified plant species
     */
    public static String formatSpecieName(String planta) {

        if (!planta.equals( "" )) {
            planta = planta.replace( " ", "_" );
        }
        return planta;
    }

    /**
     * Transforms the classification to a percentage output
     *
     * @param accuracy Accuracy of the classified plant
     */
    @SuppressLint("DefaultLocale")
    public static String formatAccuracy(String accuracy) {

        double accPlant = Double.parseDouble( accuracy ) * 100;

        return (format( "%.2f", accPlant ) + "%");
    }

    /**
     * Search for the color of the icon corresponds to the threat level of the species.
     *
     * @param SpecieRiskColor user identified plant species.
     * @return Icon with the color corresponds to the threat level of the species.
     */
    public static int SpecieColorRiskFinder(String SpecieRiskColor) {
        int marcador;
        if (SpecieRiskColor.equals( "laranja" )) {
            marcador = R.drawable.flor_em_perigo;
        } else if (SpecieRiskColor.equals( "verde" )) {
            marcador = R.drawable.flor_quase_ameacada;
        } else {
            marcador = R.drawable.flor_nao_avaliada;
        }

        return marcador;
    }

    /**
     * Search for the color of the species classifies, this color corresponds to the threat level of
     * the species according to the Lista Vermelha.
     *
     * @param planta User classified plant species.
     * @return Annotation that returns the color in which the icon must have that corresponds to the
     * threat level of the plant.
     */
    public static String plantColorFinder(String planta) {
        switch (planta) {
            case "araucaria":
                return AnnotationPlantas.araucaria;
            case "jequetiba_vermelho":
                return AnnotationPlantas.jequetiba_vermelho;
            case "guapeba":
                return AnnotationPlantas.guapeba;
            case "guapuruvu":
                return AnnotationPlantas.guapuruvu;
            case "guatambu":
                return AnnotationPlantas.guatambu;
            case "ipe_roxo":
                return AnnotationPlantas.ipe_roxo;
            case "pitanga":
                return AnnotationPlantas.pitanga;
            default:
                return null;
        }
    }
}
