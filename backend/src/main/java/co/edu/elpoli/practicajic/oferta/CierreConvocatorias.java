package co.edu.elpoli.practicajic.oferta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** RF-20: cierre automático de convocatorias al arrancar y cada madrugada. */
@Component
public class CierreConvocatorias {

    private static final Logger log = LoggerFactory.getLogger(CierreConvocatorias.class);

    private final OfertaService ofertas;

    public CierreConvocatorias(OfertaService ofertas) {
        this.ofertas = ofertas;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 0 * * *")
    public void ejecutar() {
        int cerradas = ofertas.cerrarVencidas();
        if (cerradas > 0) {
            log.info("Se cerraron automáticamente {} convocatorias vencidas.", cerradas);
        }
    }
}
