package co.edu.elpoli.practicajic.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueo temporal por correo tras varios intentos fallidos, para frenar ataques de fuerza bruta.
 * Vive en memoria: suficiente para el piloto con una sola instancia del backend.
 */
@Component
public class IntentosLogin {

    private record Estado(int fallos, Instant bloqueadoHasta) {
    }

    private final Map<String, Estado> estados = new ConcurrentHashMap<>();
    private final int maxIntentos;
    private final Duration bloqueo;

    public IntentosLogin(@Value("${app.login.max-intentos}") int maxIntentos,
                         @Value("${app.login.bloqueo}") Duration bloqueo) {
        this.maxIntentos = maxIntentos;
        this.bloqueo = bloqueo;
    }

    public boolean estaBloqueado(String correo) {
        Estado estado = estados.get(correo);
        return estado != null && estado.bloqueadoHasta() != null && Instant.now().isBefore(estado.bloqueadoHasta());
    }

    public void registrarFallo(String correo) {
        estados.compute(correo, (clave, actual) -> {
            int fallos = (actual == null || actual.bloqueadoHasta() != null) ? 1 : actual.fallos() + 1;
            Instant hasta = fallos >= maxIntentos ? Instant.now().plus(bloqueo) : null;
            return new Estado(fallos, hasta);
        });
    }

    public void registrarExito(String correo) {
        estados.remove(correo);
    }

    public long minutosBloqueo() {
        return bloqueo.toMinutes();
    }
}
