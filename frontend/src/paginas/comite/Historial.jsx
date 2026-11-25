import { api } from '../../api/cliente.js'
import { fecha } from '../../componentes/formato.js'
import { Carga, Encabezado, Tarjeta, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

const DECISION = { APROBAR: 'Aprobada', AJUSTES: 'Ajustes solicitados', RECHAZAR: 'Rechazada' }

/** Evaluaciones registradas por el miembro del comité en sesión. */
export default function Historial() {
  const { datos, error, cargando } = useCarga(() => api.get('/comite/historial'))
  return (
    <div className="max-w-5xl space-y-5">
      <Encabezado titulo="Historial" descripcion="Tus evaluaciones de ofertas y candidatos." />
      <Carga cargando={cargando} error={error}>
        {datos && (
          <>
            <Tarjeta titulo={`Ofertas evaluadas (${datos.ofertas.length})`}>
              {datos.ofertas.length === 0 ? <Vacio>Aún no has evaluado ofertas.</Vacio> : (
                <ul className="divide-y divide-slate-100 text-sm">
                  {datos.ofertas.map((o) => (
                    <li key={o.id} className="py-2">
                      <strong>{o.oferta}</strong> · {o.empresa} · {fecha(o.fecha)} · {o.puntajeTotal} puntos · {DECISION[o.decision]}
                      {o.observaciones && <p className="text-slate-600">{o.observaciones}</p>}
                    </li>
                  ))}
                </ul>
              )}
            </Tarjeta>
            <Tarjeta titulo={`Candidatos calificados (${datos.candidatos.length})`}>
              {datos.candidatos.length === 0 ? <Vacio>Aún no has calificado candidatos.</Vacio> : (
                <ul className="divide-y divide-slate-100 text-sm">
                  {datos.candidatos.map((c) => (
                    <li key={c.id} className="py-2">
                      <strong>{c.estudiante}</strong> · {c.oferta} ({c.empresa}) · {fecha(c.fecha)} · {c.puntajeFinal} puntos
                      {c.recomendado && <span className="ml-1 font-semibold text-marca-700">· recomendado</span>}
                      {c.concepto && <p className="text-slate-600">{c.concepto}</p>}
                    </li>
                  ))}
                </ul>
              )}
            </Tarjeta>
          </>
        )}
      </Carga>
    </div>
  )
}
