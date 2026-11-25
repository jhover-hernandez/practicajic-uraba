import { useState } from 'react'
import { api } from '../../api/cliente.js'
import { useAuth } from '../../auth/AuthContext.jsx'
import { MUNICIPIOS_URABA } from '../../auth/roles.js'
import { NOMBRE_TAMANO } from '../../componentes/formato.js'
import { Aviso, Boton, Campo, Carga, Encabezado, EstadoInsignia, Tarjeta } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'
import { useFormulario } from '../../componentes/useFormulario.js'

/** RF-06: perfil de la empresa. */
export default function PerfilEmpresa() {
  const { datos, setDatos, error, cargando } = useCarga(() => api.get('/empresa/perfil'))
  return (
    <div className="max-w-3xl space-y-5">
      <Encabezado titulo="Perfil de la empresa" descripcion="Estos datos los ven los estudiantes en el detalle de tus convocatorias." />
      <Carga cargando={cargando} error={error}>
        {datos && <Formulario perfil={datos} onGuardado={setDatos} />}
      </Carga>
    </div>
  )
}

function Formulario({ perfil, onGuardado }) {
  const { refrescarUsuario } = useAuth()
  const [aviso, setAviso] = useState(null)
  const { mensajeError, enviando, alEnviar, campo } = useFormulario(
    {
      razonSocial: perfil.razonSocial, nombreComercial: perfil.nombreComercial ?? '', sector: perfil.sector ?? '',
      tamano: perfil.tamano ?? '', municipio: perfil.municipio, contacto: perfil.contacto ?? '',
    },
    async (v) => {
      const nuevo = await api.put('/empresa/perfil', v)
      onGuardado(nuevo)
      setAviso(perfil.estadoValidacion === 'RECHAZADA'
        ? 'Se guardaron los cambios y la empresa volvió a revisión del Politécnico.'
        : 'Se guardaron los cambios.')
      refrescarUsuario().catch(() => {})
    },
  )

  return (
    <>
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      {perfil.estadoValidacion === 'RECHAZADA' && perfil.motivoRechazo && (
        <Aviso tipo="error">El registro fue rechazado: {perfil.motivoRechazo}. Corrige los datos y guárdalos para que se revisen de nuevo.</Aviso>
      )}
      {perfil.estadoValidacion === 'PENDIENTE' && (
        <Aviso tipo="alerta">La empresa está pendiente de validación. Podrás publicar ofertas cuando el Politécnico la valide.</Aviso>
      )}
      <Tarjeta titulo="Datos de la empresa"
               acciones={<span className="flex items-center gap-2 text-sm text-slate-500">Estado: <EstadoInsignia estado={perfil.estadoValidacion} /></span>}>
        <form onSubmit={alEnviar} noValidate className="space-y-4">
          {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo etiqueta="NIT" value={perfil.nit} disabled readOnly />
            <Campo etiqueta="Correo de la cuenta" value={perfil.correo} disabled readOnly />
            <Campo etiqueta="Razón social" {...campo('razonSocial')} />
            <Campo etiqueta="Nombre comercial" {...campo('nombreComercial')} />
            <Campo etiqueta="Sector" placeholder="Ej.: tecnología, agroindustria, comercio" {...campo('sector')} />
            <Campo etiqueta="Tamaño" como="select" {...campo('tamano')}>
              <option value="">Selecciona</option>
              {Object.entries(NOMBRE_TAMANO).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
            </Campo>
            <Campo etiqueta="Municipio" como="select" {...campo('municipio')}>
              {MUNICIPIOS_URABA.map((m) => <option key={m}>{m}</option>)}
            </Campo>
            <Campo etiqueta="Persona de contacto" placeholder="Nombre y teléfono" {...campo('contacto')} />
          </div>
          <div className="flex justify-end">
            <Boton type="submit" disabled={enviando}>{enviando ? 'Guardando…' : 'Guardar cambios'}</Boton>
          </div>
        </form>
      </Tarjeta>
    </>
  )
}
