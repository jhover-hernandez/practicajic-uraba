package co.edu.elpoli.practicajic.empresa;

import co.edu.elpoli.practicajic.actividad.ActividadService;
import co.edu.elpoli.practicajic.comun.NegocioException;
import co.edu.elpoli.practicajic.comun.Textos;
import co.edu.elpoli.practicajic.empresa.EmpresaDtos.ActualizarEmpresaRequest;
import co.edu.elpoli.practicajic.empresa.EmpresaDtos.PerfilEmpresaDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-06: la empresa completa y actualiza su perfil. El NIT no cambia porque identifica a la empresa. */
@Service
@Transactional
public class PerfilEmpresaService {

    private final EmpresaRepository empresas;
    private final ActividadService actividad;

    public PerfilEmpresaService(EmpresaRepository empresas, ActividadService actividad) {
        this.empresas = empresas;
        this.actividad = actividad;
    }

    @Transactional(readOnly = true)
    public PerfilEmpresaDto ver(Long usuarioId) {
        return PerfilEmpresaDto.de(delUsuario(usuarioId));
    }

    public PerfilEmpresaDto actualizar(Long usuarioId, ActualizarEmpresaRequest datos) {
        Empresa e = delUsuario(usuarioId);
        e.setRazonSocial(datos.razonSocial().trim());
        e.setNombreComercial(Textos.limpiar(datos.nombreComercial()));
        e.setSector(Textos.limpiar(datos.sector()));
        e.setTamano(Textos.limpiar(datos.tamano()));
        e.setMunicipio(datos.municipio().trim());
        e.setContacto(Textos.limpiar(datos.contacto()));
        e.getUsuario().setNombre(e.getRazonSocial());
        // Si el registro fue rechazado, corregir los datos lo devuelve a revisión del administrador.
        if (e.getEstadoValidacion() == EstadoValidacionEmpresa.RECHAZADA) {
            e.setEstadoValidacion(EstadoValidacionEmpresa.PENDIENTE);
            e.setMotivoRechazo(null);
            actividad.registrar(usuarioId, "REENVIAR_VALIDACION", "empresa", e.getId(), "Datos corregidos");
        }
        return PerfilEmpresaDto.de(e);
    }

    public Empresa delUsuario(Long usuarioId) {
        return empresas.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "No se encontró el perfil de la empresa."));
    }
}
