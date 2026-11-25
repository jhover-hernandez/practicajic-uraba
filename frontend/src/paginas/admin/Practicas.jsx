import { useState } from 'react'
import { api } from '../../api/cliente.js'
import FichaPractica from '../../componentes/FichaPractica.jsx'
import { dinero, fecha, hoyIso, num } from '../../componentes/formato.js'
import { Aviso, Boton, Campo, Carga, Encabezado, Modal, Tarjeta, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'
import { useFormulario } from '../../componentes/useFormulario.js'

/** RF-35 a RF-37 · CU-11 Formalizar y seguir la práctica. */
export default function Practicas() {
  const pendientes = useCarga(() => api.get('/admin/practicas/pendientes'))
  const [estado, setEstado] = useState('INICIADA')
  const practicas = useCarga(() => api.get(`/admin/practicas${estado ? `?estado=${estado}` : ''}`), [estado])
  const [formalizando, setFormalizando] = useState(null)
  const [aviso, setAviso] = useState(null)

  const reemplazar = (nueva) => {
    if (estado && nueva.estado !== estado) practicas.recargar()
    else practicas.setDatos((l) => l.map((p) => (p.id === nueva.id ? nueva : p)))
  }

  return (
    <div className="max-w-5xl space-y-5">
      <Encabezado titulo="Prácticas y seguimiento" descripcion="Formaliza las prácticas aceptadas y registra visitas, informes y la evaluación final." />
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}

      <Tarjeta titulo="Pendientes de formalizar">
        <Carga cargando={pendientes.cargando} error={pendientes.error}>
          {pendientes.datos?.length === 0 ? <p className="text-sm text-slate-500">No hay estudiantes esperando formalización.</p> : (
            <ul className="divide-y divide-slate-100">
              {pendientes.datos?.map((p) => (
                <li key={p.postulacionId} className="flex flex-wrap items-center justify-between gap-3 py-3">
                  <div className="text-sm">
                    <p className="font-medium text-slate-900">{p.estudiante} · {p.programa}</p>
                    <p className="text-slate-600">{p.oferta} · {p.empresa} · aceptada el {fecha(p.fechaAceptacion)}</p>
                  </div>
                  <Boton onClick={() => setFormalizando(p)}>Formalizar</Boton>
                </li>
              ))}
            </ul>
          )}
        </Carga>
      </Tarjeta>

      <div className="max-w-xs">
        <Campo etiqueta="Prácticas" como="select" value={estado} onChange={(e) => setEstado(e.target.value)}>
          <option value="INICIADA">En curso</option>
          <option value="FINALIZADA">Finalizadas</option>
          <option value="">Todas</option>
        </Campo>
      </div>
      <Carga cargando={practicas.cargando && !practicas.datos} error={practicas.error}>
        {practicas.datos?.length === 0 ? <Vacio>No hay prácticas en este estado.</Vacio> : (
          <div className="space-y-4">
            {practicas.datos?.map((p) => (
              <FichaPractica key={p.id} practica={p} rutaSeguimiento={`/admin/practicas/${p.id}/seguimientos`} onActualizada={reemplazar} />
            ))}
          </div>
        )}
      </Carga>

      {formalizando && (
        <Formalizar pendiente={formalizando} onCerrar={() => setFormalizando(null)}
                    onGuardado={() => {
                      setFormalizando(null)
                      setAviso(`Se formalizó la práctica de ${formalizando.estudiante}. Se avisó al estudiante y a la empresa.`)
                      pendientes.recargar()
                      practicas.recargar()
                    }} />
      )}
    </div>
  )
}

function Formalizar({ pendiente, onCerrar, onGuardado }) {
  const { mensajeError, enviando, alEnviar, campo } = useFormulario(
    {
      fechaInicio: hoyIso(), fechaFin: '', tipoVinculacion: pendiente.tipoVinculacion ?? '',
      incentivo: pendiente.incentivo ?? '', tutorEmpresa: '', tutorInstitucion: '',
    },
    async (v) => {
      await api.post(`/admin/practicas/formalizar/${pendiente.postulacionId}`,
        { ...v, fechaFin: v.fechaFin || null, incentivo: num(v.incentivo) })
      onGuardado()
    },
  )
  return (
    <Modal titulo={`Formalizar práctica · ${pendiente.estudiante}`} onCerrar={onCerrar}>
      <form onSubmit={alEnviar} noValidate className="space-y-4">
        <p className="text-sm text-slate-600">
          {pendiente.oferta} · {pendiente.empresa} · {pendiente.duracionMeses} meses · ofrecido: {dinero(pendiente.incentivo)}
        </p>
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo etiqueta="Fecha de inicio" type="date" {...campo('fechaInicio')} />
          <Campo etiqueta="Fecha de fin (prevista)" type="date" {...campo('fechaFin')} />
          <Campo etiqueta="Tipo de vinculación" {...campo('tipoVinculacion')} />
          <Campo etiqueta="Incentivo mensual (COP)" type="number" min="0" step="1000" {...campo('incentivo')} />
          <Campo etiqueta="Tutor de la empresa" {...campo('tutorEmpresa')} />
          <Campo etiqueta="Tutor del Politécnico (docente APIT)" {...campo('tutorInstitucion')} />
        </div>
        <div className="flex justify-end gap-2">
          <Boton type="button" variante="secundario" onClick={onCerrar}>Cancelar</Boton>
          <Boton type="submit" disabled={enviando}>{enviando ? 'Guardando…' : 'Formalizar'}</Boton>
        </div>
      </form>
    </Modal>
  )
}
