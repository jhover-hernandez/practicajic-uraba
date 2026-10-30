package co.edu.elpoli.practicajic.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío de correos de la plataforma. Si el correo está deshabilitado (app.correo.habilitado=false)
 * el enlace se escribe en el log, útil en desarrollo sin servidor SMTP.
 */
@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final JavaMailSender enviador;
    private final boolean habilitado;
    private final String remitente;

    public CorreoService(JavaMailSender enviador,
                         @Value("${app.correo.habilitado}") boolean habilitado,
                         @Value("${app.correo.remitente}") String remitente) {
        this.enviador = enviador;
        this.habilitado = habilitado;
        this.remitente = remitente;
    }

    public void enviarRecuperacion(String destinatario, String enlace, long minutosVigencia) {
        String cuerpo = """
                Hola,

                Recibimos una solicitud para restablecer la contraseña de tu cuenta en PRACTICAJIC URABÁ.
                Para definir una nueva contraseña, abre este enlace (vence en %d minutos):

                %s

                Si no fuiste tú, ignora este mensaje: tu contraseña actual sigue funcionando.

                Politécnico Colombiano Jaime Isaza Cadavid – Regional Urabá
                """.formatted(minutosVigencia, enlace);
        enviar(destinatario, "Restablece tu contraseña · PRACTICAJIC URABÁ", cuerpo);
    }

    private void enviar(String destinatario, String asunto, String cuerpo) {
        if (!habilitado) {
            log.info("Correo deshabilitado. Mensaje para {} · {}\n{}", destinatario, asunto, cuerpo);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        try {
            enviador.send(mensaje);
        } catch (MailException e) {
            // No se informa al usuario para no revelar si el correo existe; queda en el log del servidor.
            log.error("No se pudo enviar el correo a {}: {}", destinatario, e.getMessage());
        }
    }
}
