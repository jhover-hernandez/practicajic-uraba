package co.edu.elpoli.practicajic.postulacion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Textos legibles para los mensajes que recibe el usuario (RNF-11). */
final class Mensajes {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy 'a las' h:mm a", Locale.forLanguageTag("es-CO"));

    private Mensajes() {
    }

    static String legible(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FORMATO);
    }

    static String estado(EstadoPostulacion estado) {
        return switch (estado) {
            case POSTULADO -> "Postulado";
            case EN_REVISION -> "En revisión";
            case PRESELECCIONADO -> "Preseleccionado";
            case ENTREVISTA -> "Entrevista";
            case SELECCIONADO -> "Seleccionado";
            case NO_SELECCIONADO -> "No seleccionado";
            case PRACTICA_INICIADA -> "Práctica iniciada";
            case PRACTICA_FINALIZADA -> "Práctica finalizada";
        };
    }
}
