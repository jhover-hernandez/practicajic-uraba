import { useState } from 'react'
import { api } from '../../api/cliente.js'
import FichaOferta from '../../componentes/FichaOferta.jsx'
import { ESTADO, fecha } from '../../componentes/formato.js'
import { Aviso, Boton, Campo, Carga, Encabezado, EstadoInsignia, Modal, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

const ESTADOS_OFERTA = ['EN_REVISION', 'APROBADA', 'AJUSTES_SOLICITADOS', 'RECHAZADA', 'CERRADA', 'BORRADOR']
const CANDIDATOS_ASIGNABLES = ['EN_REVISION', 'PRESELECCIONADO', 'ENTREVISTA']

/** Gestión de convocatorias y RF-34: asignar evaluadores a ofertas y a candidatos. */
export default function Convocatorias() {
  const [pestana, setPestana] = useState('ofertas')
  const { datos: evaluadores } = useCarga(() => api.get('/admin/evaluadores'))
  return (
    <div className="space-y-5">
      <Encabezado titulo="Convocatorias" descripcion="Asigna el miembro del comité que evalúa cada oferta y cada candidato." />
      <div className="flex gap-2" role="tablist">
        {[['ofertas', 'Ofertas'], ['candidatos', 'Candidatos']].map(([v, t]) => (
          <button key={v} role="tab" aria-selected={pestana === v} onClick={() => setPestana(v)}
                  className={`rounded-lg px-4 py-2 text-sm font-semibold ${pestana === v ? 'bg-marca-600 text-white' : 'bg-white text-slate-700 ring-1 ring-slate-300'}`}>
            {t}
          </button>
        ))}
      </div>
      {evaluadores?.length === 0 && (
        <Aviso tipo="alerta">No hay miembros activos del comité. Créalos en Usuarios y roles para poder asignarlos.</Aviso>
      )}
      {pestana === 'ofertas' ? <Ofertas evaluadores={evaluadores ?? []} /> : <Candidatos evaluadores={evaluadores ?? []} />}
    </div>
  )
}

function SelectorEvaluador({ valor, evaluadores, onAsignar }) {
  return (
    <select aria-label="Evaluador" value={valor ?? ''} onChange={(e) => e.target.value && onAsignar(Number(e.target.value))}
            className="w-full min-w-40 rounded-lg border border-slate-300 bg-white px-2 py-1.5 text-sm">
      <option value="">Sin asignar</option>
      {evaluadores.map((ev) => <option key={ev.id} value={ev.id}>{ev.nombre}</option>)}
    </select>
  )
}

function Ofertas({ evaluadores }) {
  const [estado, setEstado] = useState('EN_REVISION')
  const { datos, error, cargando, recargar } = useCarga(() => api.get(`/admin/ofertas${estado ? `?estado=${estado}` : ''}`), [estado])
  const [detalle, setDetalle] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [errorAccion, setErrorAccion] = useState(null)

  async function ejecutar(fn, mensaje) {
    setErrorAccion(null)
    try {
      await fn()
      setAviso(mensaje)
      recargar()
    } catch (e) {
      setErrorAccion(e.message)
    }
  }

  return (
    <>
      <div className="max-w-xs">
        <Campo etiqueta="Estado" como="select" value={estado} onChange={(e) => setEstado(e.target.value)}>
          <option value="">Todos</option>
          {ESTADOS_OFERTA.map((e) => <option key={e} value={e}>{ESTADO[e][0]}</option>)}
        </Campo>
      </div>
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      {errorAccion && <Aviso tipo="error">{errorAccion}</Aviso>}
      <Carga cargando={cargando && !datos} error={error}>
        {datos?.length === 0 ? <Vacio>No hay ofertas en ese estado.</Vacio> : (
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
            <table className="w-full min-w-[820px] text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr><th className="px-4 py-3">Oferta</th><th className="px-4 py-3">Empresa</th><th className="px-4 py-3">Postulados</th>
                  <th className="px-4 py-3">Cierre</th><th className="px-4 py-3">Estado</th><th className="px-4 py-3">Evaluador</th><th className="px-4 py-3" /></tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {datos?.map((o) => (
                  <tr key={o.id}>
                    <td className="px-4 py-3">
                      <button onClick={() => setDetalle(o)} className="text-left font-medium text-slate-900 hover:text-marca-700">{o.titulo}</button>
                      <div className="text-xs text-slate-500">{o.area}</div>
                    </td>
                    <td className="px-4 py-3">{o.empresa}</td>
                    <td className="px-4 py-3">{o.postulados} · {o.vacantesCubiertas}/{o.vacantes} cubiertas</td>
                    <td className="px-4 py-3">{fecha(o.fechaCierre)}</td>
                    <td className="px-4 py-3"><EstadoInsignia estado={o.estado} /></td>
                    <td className="px-4 py-3">
                      {o.estado === 'EN_REVISION'
                        ? <SelectorEvaluador valor={o.evaluadorId} evaluadores={evaluadores}
                                             onAsignar={(id) => ejecutar(() => api.put(`/admin/ofertas/${o.id}/evaluador`, { evaluadorId: id }),
                                               `Se asignó el evaluador de "${o.titulo}".`)} />
                        : o.evaluador ?? '—'}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {o.estado === 'APROBADA' && (
                        <Boton variante="peligro" className="px-3 py-1.5" onClick={() => window.confirm(`¿Cerrar "${o.titulo}"?`)
                          && ejecutar(() => api.post(`/admin/ofertas/${o.id}/cerrar`), `Se cerró "${o.titulo}".`)}>Cerrar</Boton>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Carga>
      {detalle && (
        <Modal titulo={detalle.titulo} ancho="max-w-3xl" onCerrar={() => setDetalle(null)}>
          <p className="mb-4 text-sm text-slate-600">{detalle.empresa} · <EstadoInsignia estado={detalle.estado} /></p>
          <FichaOferta oferta={detalle} />
          {detalle.observacionesComite && <p className="mt-4 text-sm"><strong>Comité:</strong> {detalle.observacionesComite}</p>}
        </Modal>
      )}
    </>
  )
}

function Candidatos({ evaluadores }) {
  const { datos, error, cargando, recargar } = useCarga(() => api.get('/admin/postulaciones'))
  const [aviso, setAviso] = useState(null)
  const [errorAccion, setErrorAccion] = useState(null)
  const lista = datos?.filter((p) => CANDIDATOS_ASIGNABLES.includes(p.estado))

  async function asignar(p, evaluadorId) {
    setErrorAccion(null)
    try {
      await api.put(`/admin/postulaciones/${p.id}/evaluador`, { evaluadorId })
      setAviso(`Se asignó el evaluador de ${p.nombre}.`)
      recargar()
    } catch (e) {
      setErrorAccion(e.message)
    }
  }

  return (
    <>
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      {errorAccion && <Aviso tipo="error">{errorAccion}</Aviso>}
      <Carga cargando={cargando && !datos} error={error}>
        {lista?.length === 0 ? <Vacio>No hay candidatos en revisión, preselección o entrevista.</Vacio> : (
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr><th className="px-4 py-3">Candidato</th><th className="px-4 py-3">Convocatoria</th><th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3">Coincidencia</th><th className="px-4 py-3">Evaluador</th></tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {lista?.map((p) => (
                  <tr key={p.id}>
                    <td className="px-4 py-3"><div className="font-medium text-slate-900">{p.nombre}</div><div className="text-xs text-slate-500">{p.programa}</div></td>
                    <td className="px-4 py-3">{p.oferta}</td>
                    <td className="px-4 py-3"><EstadoInsignia estado={p.estado} /></td>
                    <td className="px-4 py-3">{p.coincidentes} de {p.totalCompetencias}</td>
                    <td className="px-4 py-3"><SelectorEvaluador valor={p.evaluadorId} evaluadores={evaluadores} onAsignar={(id) => asignar(p, id)} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Carga>
    </>
  )
}
