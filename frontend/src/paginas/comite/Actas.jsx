import { useState } from 'react'
import { api } from '../../api/cliente.js'
import { fecha, fechaHora, hoyIso } from '../../componentes/formato.js'
import { Aviso, Boton, Campo, Encabezado, Vacio } from '../../componentes/ui.jsx'

const DECISION = { APROBAR: 'Aprobada', AJUSTES: 'Ajustes solicitados', RECHAZAR: 'Rechazada' }

function inicioDeMes() {
  return `${hoyIso().slice(0, 8)}01`
}

/** RF-33: acta de comité. Se imprime o se guarda como PDF desde el navegador. */
export default function Actas() {
  const [rango, setRango] = useState({ desde: inicioDeMes(), hasta: hoyIso() })
  const [acta, setActa] = useState(null)
  const [error, setError] = useState(null)
  const [cargando, setCargando] = useState(false)

  async function generar(e) {
    e.preventDefault()
    setError(null)
    setCargando(true)
    try {
      setActa(await api.get(`/comite/acta?desde=${rango.desde}&hasta=${rango.hasta}`))
    } catch (err) {
      setError(err.message)
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="max-w-5xl space-y-5">
      <div className="print:hidden">
        <Encabezado titulo="Actas de comité" descripcion="Reúne las decisiones del comité en un periodo. Usa Imprimir para guardarla como PDF." />
      </div>
      <form onSubmit={generar} className="flex flex-wrap items-end gap-3 rounded-xl border border-slate-200 bg-white p-4 print:hidden">
        <Campo etiqueta="Desde" type="date" value={rango.desde} onChange={(e) => setRango((r) => ({ ...r, desde: e.target.value }))} />
        <Campo etiqueta="Hasta" type="date" value={rango.hasta} onChange={(e) => setRango((r) => ({ ...r, hasta: e.target.value }))} />
        <Boton type="submit" disabled={cargando}>{cargando ? 'Generando…' : 'Generar acta'}</Boton>
        {acta && <Boton type="button" variante="secundario" onClick={() => window.print()}>Imprimir / guardar PDF</Boton>}
      </form>
      {error && <Aviso tipo="error">{error}</Aviso>}

      {acta && (
        <article className="space-y-6 rounded-xl border border-slate-200 bg-white p-6 print:border-0 print:p-0">
          <header className="border-b border-slate-200 pb-4 text-center">
            <p className="text-sm font-semibold uppercase tracking-wide text-slate-500">Politécnico Colombiano Jaime Isaza Cadavid · Regional Urabá</p>
            <h1 className="mt-1 text-xl font-bold text-slate-900">Acta del comité evaluador · PRACTICAJIC URABÁ</h1>
            <p className="text-sm text-slate-600">Periodo: {fecha(acta.desde)} a {fecha(acta.hasta)} · Generada el {fechaHora(acta.generada)}</p>
          </header>

          <section>
            <h2 className="font-semibold text-slate-900">1. Participantes</h2>
            <p className="mt-1 text-sm text-slate-700">{acta.evaluadores.length ? acta.evaluadores.join(', ') : 'Sin evaluaciones en el periodo.'}</p>
            <p className="mt-1 text-sm text-slate-700">Puntaje mínimo de aprobación de ofertas: {acta.puntajeMinimo} / 100.</p>
          </section>

          <section>
            <h2 className="font-semibold text-slate-900">2. Evaluación de ofertas de práctica</h2>
            {acta.ofertas.length === 0 ? <Vacio>No se evaluaron ofertas en el periodo.</Vacio> : (
              <table className="mt-2 w-full text-left text-sm">
                <thead className="border-b border-slate-300 text-xs uppercase text-slate-500">
                  <tr><th className="py-2 pr-2">Fecha</th><th className="py-2 pr-2">Oferta</th><th className="py-2 pr-2">Empresa</th>
                    <th className="py-2 pr-2">Evaluador</th><th className="py-2 pr-2 text-right">Puntaje</th><th className="py-2">Decisión</th></tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {acta.ofertas.map((o) => (
                    <tr key={o.id} className="align-top">
                      <td className="py-2 pr-2 whitespace-nowrap">{fecha(o.fecha)}</td>
                      <td className="py-2 pr-2">{o.oferta}{o.observaciones && <div className="text-xs text-slate-500">{o.observaciones}</div>}</td>
                      <td className="py-2 pr-2">{o.empresa}</td>
                      <td className="py-2 pr-2">{o.evaluador}</td>
                      <td className="py-2 pr-2 text-right">{o.puntajeTotal}</td>
                      <td className="py-2">{DECISION[o.decision]}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>

          <section>
            <h2 className="font-semibold text-slate-900">3. Calificación de candidatos y recomendaciones</h2>
            {acta.candidatos.length === 0 ? <Vacio>No se calificaron candidatos en el periodo.</Vacio> : (
              <table className="mt-2 w-full text-left text-sm">
                <thead className="border-b border-slate-300 text-xs uppercase text-slate-500">
                  <tr><th className="py-2 pr-2">Fecha</th><th className="py-2 pr-2">Candidato</th><th className="py-2 pr-2">Convocatoria</th>
                    <th className="py-2 pr-2">Evaluador</th><th className="py-2 pr-2 text-right">Puntaje</th><th className="py-2">Concepto</th></tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {acta.candidatos.map((c) => (
                    <tr key={c.id} className="align-top">
                      <td className="py-2 pr-2 whitespace-nowrap">{fecha(c.fecha)}</td>
                      <td className="py-2 pr-2">{c.estudiante}</td>
                      <td className="py-2 pr-2">{c.oferta}<div className="text-xs text-slate-500">{c.empresa}</div></td>
                      <td className="py-2 pr-2">{c.evaluador}</td>
                      <td className="py-2 pr-2 text-right">{c.puntajeFinal}</td>
                      <td className="py-2">{c.recomendado ? 'Recomendado. ' : 'No recomendado. '}{c.concepto}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>

          <section className="grid gap-10 pt-10 sm:grid-cols-2">
            {(acta.evaluadores.length ? acta.evaluadores : ['', '']).map((nombre, i) => (
              <div key={i} className="border-t border-slate-400 pt-2 text-center text-sm text-slate-600">{nombre || 'Firma'}<br />Comité evaluador</div>
            ))}
          </section>
        </article>
      )}
    </div>
  )
}
