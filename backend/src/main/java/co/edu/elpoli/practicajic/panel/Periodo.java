package co.edu.elpoli.practicajic.panel;

import co.edu.elpoli.practicajic.comun.NegocioException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Semestre académico "2026-1" (enero a junio) o "2026-2" (julio a diciembre). Sin periodo = todo el histórico. */
public record Periodo(String nombre, LocalDate desde, LocalDate hasta) {

    private static final Pattern FORMATO = Pattern.compile("^(\\d{4})-([12])$");

    public static Periodo de(String texto) {
        if (texto == null || texto.isBlank()) {
            return new Periodo(null, LocalDate.of(2000, 1, 1), LocalDate.of(2999, 12, 31));
        }
        Matcher m = FORMATO.matcher(texto.trim());
        if (!m.matches()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "El periodo debe tener el formato 2026-2.");
        }
        int anio = Integer.parseInt(m.group(1));
        boolean primero = m.group(2).equals("1");
        return new Periodo(texto.trim(), LocalDate.of(anio, primero ? 1 : 7, 1), LocalDate.of(anio, primero ? 6 : 12, primero ? 30 : 31));
    }

    /** Los semestres desde el inicio del piloto hasta el actual, del más reciente al más antiguo. */
    public static List<String> disponibles() {
        List<String> lista = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (int anio = hoy.getYear(); anio >= 2026; anio--) {
            if (anio < hoy.getYear() || hoy.getMonthValue() >= 7) {
                lista.add(anio + "-2");
            }
            lista.add(anio + "-1");
        }
        return lista;
    }

    public boolean contiene(LocalDate fecha) {
        return fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    public boolean contiene(LocalDateTime fecha) {
        return fecha != null && contiene(fecha.toLocalDate());
    }

    /** Ya existía al terminar el periodo. */
    public boolean existiaAlCierre(LocalDateTime fecha) {
        return fecha != null && !fecha.toLocalDate().isAfter(hasta);
    }
}
