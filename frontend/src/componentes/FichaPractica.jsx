import { useState } from 'react'
import { api } from '../api/cliente.js'
import { dinero, fecha, hoyIso, NOMBRE_TIPO_SEGUIMIENTO, num } from './formato.js'
import { Aviso, Boton, Campo, Dato, EstadoInsignia, Modal } from './ui.jsx'
import { useFormulario } from './useFormulario.js'

/**
 * Práctica con sus seguimientos (RF-35 a RF-37). La usan el estudiante, la empresa y el administrador.
 * Con `rutaSeguimiento` muestra el botón para registrar un seguimiento o la evaluación final.
 */
export default function FichaPractica({ practica, rutaSeguimiento, onActualizada }) {
  const [registrando, setRegistrando] = useState(false)
  const activa = practica.estado === 'INICIADA'

  return (
    <article className="rounded-xl border border-slate-200 bg-white p-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-lg font-semibold text-slate-900">{practica.oferta}</h2>
          <p className="text-sm text-slate-600">{practica.estudiante} · {practica.programa} · {practica.empresa}</p>
        </div>
        <EstadoInsignia estado={practica.estado} />
      </div>
      <dl className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Dato etiqueta="Inicio">{fecha(practica.fechaInicio)}</Dato>
        <Dato etiqueta="Fin">{fecha(practica.fechaFin)}</Dato>
        <Dato etiqueta="Vinculación">{practica.tipoVinculacion}</Dato>
        <Dato etiqueta="Incentivo">{dinero(practica.incentivo)}</Dato>
        <Dato etiqueta="Tutor de la empresa">{practica.tutorEmpresa}</Dato>
        <Dato etiqueta="Tutor del Politécnico">{practica.tutorInstitucion}</Dato>
        <Dato etiqueta="Área">{practica.area}</Dato>
        <Dato etiqueta="Calificación final">{practica.calificacionFinal ?? '—'}</Dato>
      </dl>

      <div className="mt-5">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h3 className="font-semibold text-slate-900">Seguimientos ({practica.seguimientos.length})</h3>
          {rutaSeguimiento && activa && <Boton variante="secundario" onClick={() => setRegistrando(true)}>+ Registrar seguimiento</Boton>}
        </div>
        {practica.seguimientos.length === 0 ? (
          <p className="mt-2 text-sm text-slate-500">Todavía no hay seguimientos registrados.</p>
        ) : (
          <ol className="mt-3 space-y-3 border-l-2 border-marca-100 pl-4">
            {practica.seguimientos.map((s) => (
              <li key={s.id}>
                <p className="text-sm font-medium text-slate-800">
                  {NOMBRE_TIPO_SEGUIMIENTO[s.tipo]} · {fecha(s.fecha)}
                  {s.calificacion !== null && <span className="ml-2 rounded bg-marca-50 px-1.5 text-marca-800">{s.calificacion} / 5</span>}
                </p>
                <p className="whitespace-pre-line text-sm text-slate-600">{s.descripcion}</p>
                <p className="text-xs text-slate-400">Registró: {s.responsable} ({s.rolResponsable === 'EMPRESA' ? 'empresa' : 'Politécnico'})</p>
              </li>
            ))}
          </ol>
        )}
      </div>

      {registrando && (
        <FormularioSeguimiento ruta={rutaSeguimiento} fechaInicio={practica.fechaInicio}
                               onCerrar={() => setRegistrando(false)}
                               onGuardado={(p) => { setRegistrando(false); onActualizada(p) }} />
      )}
    </article>
  )
}

function FormularioSeguimiento({ ruta, fechaInicio, onCerrar, onGuardado }) {
  const { valores, mensajeError, enviando, alEnviar, campo } = useFormulario(
    { tipo: 'VISITA', fecha: hoyIso(), descripcion: '', calificacion: '' },
    async (v) => {
      if (v.tipo === 'EVALUACION_FINAL'
        && !window.confirm('La evaluación final cierra la práctica y ya no se podrán agregar seguimientos. ¿Continuar?')) return
      onGuardado(await api.post(ruta, { ...v, calificacion: num(v.calificacion) }))
    },
  )
  const conNota = valores.tipo === 'EVALUACION_PARCIAL' || valores.tipo === 'EVALUACION_FINAL'
  return (
    <Modal titulo="Registrar seguimiento" onCerrar={onCerrar}>
      <form onSubmit={alEnviar} noValidate className="space-y-4">
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo etiqueta="Tipo" como="select" {...campo('tipo')}>
            {Object.entries(NOMBRE_TIPO_SEGUIMIENTO).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
          </Campo>
          <Campo etiqueta="Fecha" type="date" min={fechaInicio} max={hoyIso()} {...campo('fecha')} />
        </div>
        <Campo etiqueta="Descripción" como="textarea" rows={4}
               placeholder="Actividades realizadas, avances, observaciones y compromisos" {...campo('descripcion')} />
        <Campo etiqueta={conNota ? 'Calificación (0 a 5)' : 'Calificación (opcional, 0 a 5)'} type="number" min="0" max="5" step="0.1"
               {...campo('calificacion')} />
        <div className="flex justify-end gap-2">
          <Boton type="button" variante="secundario" onClick={onCerrar}>Cancelar</Boton>
          <Boton type="submit" disabled={enviando}>{enviando ? 'Guardando…' : 'Registrar'}</Boton>
        </div>
      </form>
    </Modal>
  )
}
