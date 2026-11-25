package co.edu.elpoli.practicajic.archivo;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/** Respuesta HTTP para mostrar un PDF en el navegador con un nombre legible. */
public final class DescargaPdf {

    private DescargaPdf() {
    }

    public static ResponseEntity<Resource> de(Resource archivo, String nombreVisible) {
        ContentDisposition disposicion = ContentDisposition.inline()
                .filename(nombreVisible == null ? "hoja-de-vida.pdf" : nombreVisible, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(archivo);
    }
}
