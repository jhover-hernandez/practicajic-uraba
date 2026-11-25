import { useState } from 'react'
import { api } from '../../api/cliente.js'
import FichaPostulante from '../../componentes/FichaPostulante.jsx'
import { fechaHora } from '../../componentes/formato.js'
import Matriz, { MATRIZ_CANDIDATO, totalMatriz } from '../../componentes/Matriz.jsx'
import { Aviso, Boton, Campo, Carga, Encabezado, EstadoInsignia, Tarjeta, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

/** RF-31 y RF-32 · CU-10: misma pantalla que la evaluación de ofertas, con la matriz del estudiante. */
export default function Candidatos() {
  const { datos, error, cargando, recargar } = useCarga(() => api.get('/comite/candidatos'))
  const [elegido, setElegido] = useState(null)
  const [aviso, setAviso] = useState(null)

  if (elegido) {
    return (
      <Calificar item={elegido} onVolver={() => setElegido(null)}
                 onGuardado={(mensaje) => { setElegido(null); setAviso(mensaje); recargar() }} />
    )
  }

  return (
    <div className="space-y-5">
      <Encabezado titulo="Candidatos por calificar" descripcion="Postulantes en revisión, preselección, entrevista o recién seleccionados." />
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      <Carga cargando={cargando} error={error}>
        {datos?.length === 0 ? <Vacio>No hay candidatos por calificar.</Vacio> : (
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Candidato</th>
                  <th className="px-4 py-3">Convocatoria</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3">Mi calificación</th>
                  <th className="px-4 py-3 text-right" />
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {datos?.map((c) => (
                  <tr key={c.postulante.id}>
                    <td className="px-4 py-3">
                      <div className="font-medium text-slate-900">{c.postulante.nombre}</div>
                      <div className="text-xs text-slate-500">{c.postulante.programa} · semestre {c.postulante.semestre}</div>
                    </td>
                    <td className="px-4 py-3">{c.postulante.oferta}<div className="text-xs text-slate-500">{c.empresa}</div></td>
                    <td className="px-4 py-3"><EstadoInsignia estado={c.postulante.estado} /></td>
                    <td className="px-4 py-3">
                      {c.miEvaluacion ? `${c.miEvaluacion.puntajeFinal} / 100${c.miEvaluacion.recomendado ? ' · recomendado' : ''}` : '—'}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <Boton variante={c.miEvaluacion ? 'secundario' : 'primario'} className="px-3 py-1.5" onClick={() => setElegido(c)}>
                        {c.miEvaluacion ? 'Corregir' : 'Calificar'}
                      </Boton>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Carga>
    </div>
  )
}

function Calificar({ item, onVolver, onGuardado }) {
  const p = item.postulante
  const previa = item.miEvaluacion
  const [valores, setValores] = useState(previa ? {
    tecnico: previa.tecnico, perfil: previa.perfil, academico: previa.academico,
    portafolio: previa.portafolio, blandas: previa.blandas, entrevista: previa.entrevista,
  } : {})
  const [concepto, setConcepto] = useState(previa?.concepto ?? '')
  const [recomendado, setRecomendado] = useState(previa?.recomendado ?? false)
  const [errores, setErrores] = useState({})
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  async function verHojaVida() {
    try {
      await api.abrirPdf(`/comite/candidatos/${p.id}/hoja-vida`)
    } catch (e) {
      setError(e.message)
    }
  }

  async function guardar(e) {
    e.preventDefault()
    setEnviando(true)
    setError(null)
    setErrores({})
    try {
      const cuerpo = { concepto, recomendado }
      MATRIZ_CANDIDATO.forEach(([k]) => { cuerpo[k] = valores[k] === undefined || valores[k] === '' ? null : Number(valores[k]) })
      await api.put(`/comite/candidatos/${p.id}/evaluacion`, cuerpo)
      onGuardado(`Se registró la calificación de ${p.nombre}: ${totalMatriz(MATRIZ_CANDIDATO, valores).toFixed(1)} puntos.`)
    } catch (err) {
      setError(err.message)
      setErrores(err.campos)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="space-y-5">
      <Encabezado titulo={`Calificar: ${p.nombre}`} descripcion={`${p.oferta} · ${item.empresa}`}>
        <Boton variante="secundario" onClick={onVolver}>← Volver</Boton>
      </Encabezado>
      <div className="grid gap-5 lg:grid-cols-[1fr_420px]">
        <div className="space-y-5">
          <Tarjeta titulo="Perfil del candidato"
                   acciones={p.tieneHojaVida && <Boton variante="secundario" onClick={verHojaVida}>Ver hoja de vida</Boton>}>
            <div className="mb-3 flex items-center gap-2 text-sm">Estado: <EstadoInsignia estado={p.estado} /></div>
            <FichaPostulante p={p} />
            {p.fechaEntrevista && <p className="mt-3 text-sm text-slate-600">Entrevista con la empresa: {fechaHora(p.fechaEntrevista)}</p>}
          </Tarjeta>
          {p.comite && p.comite.evaluaciones > (previa ? 1 : 0) && (
            <Tarjeta titulo="Calificaciones del comité">
              <p className="text-sm">Promedio: <strong>{p.comite.puntajePromedio}</strong> en {p.comite.evaluaciones} evaluación(es)</p>
              {p.comite.conceptos.map((c) => <p key={c} className="mt-1 text-sm text-slate-600">{c}</p>)}
            </Tarjeta>
          )}
        </div>
        <form onSubmit={guardar} noValidate className="space-y-4 lg:sticky lg:top-20 lg:self-start">
          <Matriz criterios={MATRIZ_CANDIDATO} valores={valores} errores={errores}
                  onCambio={(k, v) => setValores((x) => ({ ...x, [k]: v }))} />
          <Campo etiqueta="Concepto del comité" como="textarea" rows={4} value={concepto}
                 onChange={(e) => setConcepto(e.target.value)} error={errores.concepto} />
          <label className="flex items-center gap-2 rounded-xl border border-slate-200 bg-white p-4 text-sm font-medium">
            <input type="checkbox" checked={recomendado} onChange={(e) => setRecomendado(e.target.checked)} className="h-4 w-4 accent-marca-600" />
            Recomendar al candidato para la práctica
          </label>
          {error && <Aviso tipo="error">{error}</Aviso>}
          <Boton type="submit" className="w-full" disabled={enviando}>{enviando ? 'Guardando…' : 'Registrar calificación'}</Boton>
        </form>
      </div>
    </div>
  )
}
