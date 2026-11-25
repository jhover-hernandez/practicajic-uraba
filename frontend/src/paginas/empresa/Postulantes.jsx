import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../../api/cliente.js'
import FichaPostulante from '../../componentes/FichaPostulante.jsx'
import { ESTADO, fecha, fechaHora } from '../../componentes/formato.js'
import { Aviso, Boton, Campo, Carga, Encabezado, EstadoInsignia, Modal, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'
import { useFormulario } from '../../componentes/useFormulario.js'

const TEXTO_AVANZAR = {
  EN_REVISION: 'Pasar a revisión',
  PRESELECCIONADO: 'Preseleccionar',
  ENTREVISTA: 'Programar entrevista',
  SELECCIONADO: 'Seleccionar',
}

/** Pantalla 3 del prototipo · RF-10, RF-24, RF-26 · CU-09 Gestionar postulantes. */
export default function Postulantes() {
  const [params, setParams] = useSearchParams()
  const ofertaId = params.get('oferta') ?? ''
  const { datos: ofertas } = useCarga(() => api.get('/empresa/ofertas'))
  const conPostulados = ofertas?.filter((o) => ['APROBADA', 'CERRADA'].includes(o.estado)) ?? []
  const elegida = ofertaId || conPostulados[0]?.id || ''
  const { datos: postulantes, error, cargando, recargar } = useCarga(
    () => (elegida ? api.get(`/empresa/ofertas/${elegida}/postulantes`) : Promise.resolve(null)), [elegida])
  const [aviso, setAviso] = useState(null)
  const [errorAccion, setErrorAccion] = useState(null)
  const [cambio, setCambio] = useState(null) // { p, estado }

  async function verHojaVida(p) {
    setErrorAccion(null)
    try {
      await api.abrirPdf(`/empresa/postulaciones/${p.id}/hoja-vida`)
    } catch (e) {
      setErrorAccion(e.message)
    }
  }

  return (
    <div className="space-y-5">
      <Encabezado titulo="Postulantes" descripcion="Ordenados por competencias coincidentes. Solo ves a quienes se postularon a tus ofertas." />
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      {errorAccion && <Aviso tipo="error">{errorAccion}</Aviso>}

      {ofertas && conPostulados.length === 0 ? (
        <Vacio>Cuando el comité apruebe una de tus ofertas, aquí verás a los estudiantes que se postulen.</Vacio>
      ) : (
        <>
          <div className="max-w-md">
            <Campo etiqueta="Convocatoria" como="select" value={elegida}
                   onChange={(e) => { setParams({ oferta: e.target.value }); setAviso(null) }}>
              {conPostulados.map((o) => (
                <option key={o.id} value={o.id}>{o.titulo} ({o.postulados} postulados{o.estado === 'CERRADA' ? ', cerrada' : ''})</option>
              ))}
            </Campo>
          </div>
          <Carga cargando={cargando && !postulantes} error={error}>
            {postulantes?.length === 0 ? <Vacio>Esta convocatoria aún no tiene postulantes.</Vacio> : (
              <div className="space-y-3">
                {postulantes?.map((p) => (
                  <article key={p.id} className="rounded-xl border border-slate-200 bg-white p-5">
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <h2 className="font-semibold text-slate-900">{p.nombre}</h2>
                        <p className="text-sm text-slate-600">{p.correo} · postulado el {fecha(p.fecha)}</p>
                      </div>
                      <EstadoInsignia estado={p.estado} />
                    </div>
                    <div className="mt-3"><FichaPostulante p={p} /></div>
                    {p.estado === 'ENTREVISTA' && (
                      <p className="mt-3 text-sm text-sky-800">Entrevista: {fechaHora(p.fechaEntrevista)}{p.lugarEntrevista && ` · ${p.lugarEntrevista}`}</p>
                    )}
                    {p.comite && (
                      <div className="mt-3 rounded-lg bg-slate-50 p-3 text-sm">
                        <strong>Comité evaluador:</strong> {p.comite.puntajePromedio} / 100 en {p.comite.evaluaciones} evaluación(es) ·{' '}
                        {p.comite.recomendado ? <span className="font-semibold text-marca-700">Recomendado</span> : 'No recomendado'}
                        {p.comite.conceptos.map((c) => <p key={c} className="mt-1 text-slate-600">{c}</p>)}
                      </div>
                    )}
                    {p.fechaAceptacion && <p className="mt-2 text-sm text-marca-700">El estudiante aceptó la práctica el {fecha(p.fechaAceptacion)}.</p>}
                    <div className="mt-4 flex flex-wrap gap-2">
                      {p.tieneHojaVida && <Boton variante="secundario" onClick={() => verHojaVida(p)}>Ver hoja de vida</Boton>}
                      {p.siguienteEstado && (
                        <Boton onClick={() => setCambio({ p, estado: p.siguienteEstado })}>{TEXTO_AVANZAR[p.siguienteEstado]}</Boton>
                      )}
                      {p.estado === 'ENTREVISTA' && (
                        <Boton variante="secundario" onClick={() => setCambio({ p, estado: 'REPROGRAMAR' })}>Reprogramar entrevista</Boton>
                      )}
                      {['EN_REVISION', 'PRESELECCIONADO', 'ENTREVISTA'].includes(p.estado) && (
                        <Boton variante="peligro" onClick={() => setCambio({ p, estado: 'NO_SELECCIONADO' })}>No seleccionar</Boton>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            )}
          </Carga>
        </>
      )}

      {cambio && (
        <CambioEstado cambio={cambio} onCerrar={() => setCambio(null)}
                      onGuardado={(mensaje) => { setCambio(null); setAviso(mensaje); recargar() }} />
      )}
    </div>
  )
}

function CambioEstado({ cambio, onCerrar, onGuardado }) {
  const { p, estado } = cambio
  const reprogramar = estado === 'REPROGRAMAR'
  const conEntrevista = estado === 'ENTREVISTA' || reprogramar
  const { mensajeError, enviando, alEnviar, campo } = useFormulario(
    { observaciones: '', fechaEntrevista: p.fechaEntrevista?.slice(0, 16) ?? '', lugarEntrevista: p.lugarEntrevista ?? '' },
    async (v) => {
      const entrevista = { fechaEntrevista: v.fechaEntrevista || null, lugarEntrevista: v.lugarEntrevista }
      if (reprogramar) {
        await api.put(`/empresa/postulaciones/${p.id}/entrevista`, entrevista)
        onGuardado(`Se reprogramó la entrevista de ${p.nombre}. Le avisamos por correo.`)
      } else {
        await api.patch(`/empresa/postulaciones/${p.id}/estado`, { estado, observaciones: v.observaciones, ...(conEntrevista ? entrevista : {}) })
        onGuardado(`${p.nombre} pasó a "${ESTADO[estado][0]}". Le avisamos por correo.`)
      }
    },
  )
  const titulo = reprogramar ? 'Reprogramar entrevista' : `${p.nombre} → ${ESTADO[estado][0]}`
  return (
    <Modal titulo={titulo} onCerrar={onCerrar}>
      <form onSubmit={alEnviar} noValidate className="space-y-4">
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        {estado === 'SELECCIONADO' && <Aviso tipo="info">El estudiante deberá aceptar la práctica; luego el Politécnico la formaliza.</Aviso>}
        {estado === 'NO_SELECCIONADO' && <Aviso tipo="alerta">Este cambio no se puede deshacer.</Aviso>}
        {conEntrevista && (
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo etiqueta="Fecha y hora" type="datetime-local" {...campo('fechaEntrevista')} />
            <Campo etiqueta="Lugar o enlace" placeholder="Oficina, dirección o enlace de videollamada" {...campo('lugarEntrevista')} />
          </div>
        )}
        {!reprogramar && (
          <Campo etiqueta="Observaciones para el estudiante (opcional)" como="textarea" rows={3} {...campo('observaciones')} />
        )}
        <div className="flex justify-end gap-2">
          <Boton type="button" variante="secundario" onClick={onCerrar}>Cancelar</Boton>
          <Boton type="submit" variante={estado === 'NO_SELECCIONADO' ? 'peligro' : 'primario'} disabled={enviando}>
            {enviando ? 'Guardando…' : 'Confirmar'}
          </Boton>
        </div>
      </form>
    </Modal>
  )
}
