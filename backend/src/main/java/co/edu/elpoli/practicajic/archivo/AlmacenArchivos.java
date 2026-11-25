package co.edu.elpoli.practicajic.archivo;

import co.edu.elpoli.practicajic.comun.NegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

/**
 * RNF-04: solo se aceptan PDF de máximo 5 MB y se guardan con un nombre generado por el sistema,
 * nunca con el nombre que trae el archivo. La carpeta se define con app.archivos.directorio.
 */
@Service
public class AlmacenArchivos {

    public static final long TAMANO_MAXIMO = 5L * 1024 * 1024;
    private static final byte[] FIRMA_PDF = {'%', 'P', 'D', 'F', '-'};

    private final Path raiz;

    public AlmacenArchivos(@Value("${app.archivos.directorio}") String directorio) {
        this.raiz = Path.of(directorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(raiz);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de archivos " + raiz, e);
        }
    }

    /** Valida el PDF, lo guarda y devuelve el nombre generado. */
    public String guardarPdf(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Selecciona un archivo PDF.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "El archivo supera el tamaño máximo de 5 MB.");
        }
        // Se revisa el contenido, no solo la extensión: un PDF real empieza con "%PDF-".
        try (InputStream entrada = archivo.getInputStream()) {
            if (!Arrays.equals(entrada.readNBytes(FIRMA_PDF.length), FIRMA_PDF)) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "Solo se aceptan archivos en formato PDF.");
            }
        } catch (IOException e) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo. Intenta de nuevo.");
        }
        String nombre = UUID.randomUUID() + ".pdf";
        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(entrada, raiz.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo " + nombre, e);
        }
        return nombre;
    }

    public Resource cargar(String nombre) {
        Path ruta = raiz.resolve(nombre).normalize();
        if (!ruta.startsWith(raiz) || !Files.isRegularFile(ruta)) {
            throw new NegocioException(HttpStatus.NOT_FOUND, "El archivo no existe.");
        }
        return new FileSystemResource(ruta);
    }
}
