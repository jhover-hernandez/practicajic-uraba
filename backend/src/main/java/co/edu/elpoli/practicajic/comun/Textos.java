package co.edu.elpoli.practicajic.comun;

/** Utilidades de texto compartidas por los servicios. */
public final class Textos {

    private Textos() {
    }

    /** Recorta espacios y convierte el texto vacío en {@code null}, para no guardar cadenas en blanco. */
    public static String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
