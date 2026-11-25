import { useState } from 'react'
import { api } from '../../api/cliente.js'
import FichaOferta from '../../componentes/FichaOferta.jsx'
import { fecha, NOMBRE_TAMANO } from '../../componentes/formato.js'
import Matriz, { MATRIZ_OFERTA, totalMatriz } from '../../componentes/Matriz.jsx'
import { Aviso, Boton, Campo, Carga, Dato, Encabezado, Tarjeta, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

const DECISIONES = [
  ['APROBAR', 'Aprobar y publicar'],
  ['AJUSTES', 'Solicitar ajustes'],
  ['RECHAZAR', 'Rechazar'],
]

/** Pantalla 4 del prototipo · RF-28 a RF-30 · CU-06 Evaluar oferta. */
export default function Ofertas() {
  const { datos, error, cargando, recargar } = useCarga(() => api.get('/comite/ofertas'))
  const [elegida, setElegida] = useState(null)
  const [aviso, setAviso] = useState(null)

  if (elegida) {
    return (
      <Evaluacion item={elegida} onVolver={() => setElegida(null)}
                  onEvaluada={(mensaje) => { setElegida(null); setAviso(mensaje); recargar() }} />
    )
  }

  return (
    <div className="space-y-5">
      <Encabezado titulo="Ofertas pendientes" descripcion="Ofertas en revisión asignadas a ti o todavía sin evaluador." />
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      <Carga cargando={cargando} error={error}>
        {datos?.length === 0 ? <Vacio>No hay ofertas pendientes de evaluación.</Vacio> : (
          <div className="space-y-3">
            {datos?.map((item) => (
              <article key={item.oferta.id} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-200 bg-white p-4">
                <div>
                  <h2 className="font-semibold text-slate-900">{item.oferta.titulo}</h2>
                  <p className="text-sm text-slate-600">
                    {item.oferta.empresa} · {item.oferta.area} · enviada el {fecha(item.oferta.fechaCreacion)}
                    {item.evaluaciones.length > 0 && ` · ${item.evaluaciones.length} evaluación(es) anterior(es)`}
                  </p>
                  {!item.oferta.evaluadorId && <p className="text-xs text-amber-700">Sin evaluador asignado</p>}
                </div>
                <Boton onClick={() => setElegida(item)}>Evaluar</Boton>
              </article>
            ))}
          </div>
        )}
      </Carga>
    </div>
  )
}

function Evaluacion({ item, onVolver, onEvaluada }) {
  const { oferta, empresa, evaluaciones, puntajeMinimo } = item
  const [valores, setValores] = useState({})
  const [decision, setDecision] = useState('APROBAR')
  const [observaciones, setObservaciones] = useState('')
  const [errores, setErrores] = useState({})
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const total = totalMatriz(MATRIZ_OFERTA, valores)

  async function registrar(e) {
    e.preventDefault()
    if (!window.confirm(`¿Registrar la evaluación con ${total.toFixed(1)} puntos y decisión "${DECISIONES.find((d) => d[0] === decision)[1]}"?`)) return
    setEnviando(true)
    setError(null)
    setErrores({})
    try {
      const cuerpo = { decision, observaciones }
      MATRIZ_OFERTA.forEach(([clave]) => { cuerpo[clave] = valores[clave] === undefined || valores[clave] === '' ? null : Number(valores[clave]) })
      await api.post(`/comite/ofertas/${oferta.id}/evaluacion`, cuerpo)
      onEvaluada(`Se registró la evaluación de "${oferta.titulo}". La empresa recibió la decisión por correo.`)
    } catch (err) {
      setError(err.message)
      setErrores(err.campos)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="space-y-5">
      <Encabezado titulo={`Evaluar: ${oferta.titulo}`} descripcion={`${empresa.razonSocial} · NIT ${empresa.nit}`}>
        <Boton variante="secundario" onClick={onVolver}>← Volver</Boton>
      </Encabezado>
      <div className="grid gap-5 lg:grid-cols-[1fr_420px]">
        <div className="space-y-5">
          <Tarjeta titulo="Detalle de la oferta"><FichaOferta oferta={oferta} /></Tarjeta>
          <Tarjeta titulo="Empresa">
            <dl className="grid grid-cols-2 gap-3 sm:grid-cols-3">
              <Dato etiqueta="Nombre comercial">{empresa.nombreComercial}</Dato>
              <Dato etiqueta="Sector">{empresa.sector}</Dato>
              <Dato etiqueta="Tamaño">{NOMBRE_TAMANO[empresa.tamano]}</Dato>
              <Dato etiqueta="Municipio">{empresa.municipio}</Dato>
              <Dato etiqueta="Contacto">{empresa.contacto}</Dato>
              <Dato etiqueta="Correo">{empresa.correo}</Dato>
            </dl>
          </Tarjeta>
          {evaluaciones.length > 0 && (
            <Tarjeta titulo="Evaluaciones anteriores">
              <ul className="space-y-2 text-sm">
                {evaluaciones.map((ev) => (
                  <li key={ev.id}>
                    <strong>{fecha(ev.fecha)}</strong> · {ev.evaluador} · {ev.puntajeTotal} puntos · {DECISIONES.find((d) => d[0] === ev.decision)[1]}
                    {ev.observaciones && <p className="text-slate-600">{ev.observaciones}</p>}
                  </li>
                ))}
              </ul>
            </Tarjeta>
          )}
        </div>

        <form onSubmit={registrar} noValidate className="space-y-4 lg:sticky lg:top-20 lg:self-start">
          <Matriz criterios={MATRIZ_OFERTA} valores={valores} errores={errores} minimo={Number(puntajeMinimo)}
                  onCambio={(k, v) => setValores((x) => ({ ...x, [k]: v }))} />
          <fieldset className="space-y-2 rounded-xl border border-slate-200 bg-white p-4">
            <legend className="px-1 text-sm font-medium text-slate-700">Decisión</legend>
            {DECISIONES.map(([v, t]) => (
              <label key={v} className="flex items-center gap-2 text-sm">
                <input type="radio" name="decision" value={v} checked={decision === v} onChange={() => setDecision(v)}
                       className="accent-marca-600" />
                {t}
              </label>
            ))}
            {decision === 'APROBAR' && total < puntajeMinimo && (
              <p className="text-xs text-red-600">El total no alcanza el mínimo; solo puedes solicitar ajustes o rechazar.</p>
            )}
          </fieldset>
          <Campo etiqueta={decision === 'APROBAR' ? 'Observaciones (opcional)' : 'Observaciones para la empresa'} como="textarea"
                 rows={4} value={observaciones} onChange={(e) => setObservaciones(e.target.value)} error={errores.observaciones} />
          {error && <Aviso tipo="error">{error}</Aviso>}
          <Boton type="submit" className="w-full" disabled={enviando}>{enviando ? 'Registrando…' : 'Registrar evaluación'}</Boton>
        </form>
      </div>
    </div>
  )
}
