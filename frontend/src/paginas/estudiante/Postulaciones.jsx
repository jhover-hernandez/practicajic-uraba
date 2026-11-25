import { useState } from 'react'
import { api } from '../../api/cliente.js'
import { fecha, fechaHora, NOMBRE_MODALIDAD } from '../../componentes/formato.js'
import { Aviso, BarraCoincidencia, Boton, Carga, Encabezado, EstadoInsignia, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

/** RF-23 y RF-27 · Tabla "Mis postulaciones" de la pantalla 2. */
export default function Postulaciones() {
  const { datos, error, cargando, recargar } = useCarga(() => api.get('/estudiante/postulaciones'))
  const [aviso, setAviso] = useState(null)
  const [errorAccion, setErrorAccion] = useState(null)

  async function aceptar(p) {
    const otras = datos.filter((x) => x.id !== p.id && ['POSTULADO', 'EN_REVISION', 'PRESELECCIONADO', 'ENTREVISTA', 'SELECCIONADO'].includes(x.estado))
    const nota = otras.length ? `\n\nTus otras ${otras.length} postulaciones abiertas pasarán a "No seleccionado".` : ''
    if (!window.confirm(`¿Aceptas la práctica "${p.oferta}" en ${p.empresa}?${nota}`)) return
    setErrorAccion(null)
    try {
      await api.post(`/estudiante/postulaciones/${p.id}/aceptar`)
      setAviso('Aceptaste la práctica. El Politécnico la formalizará y te avisará por correo.')
      recargar()
    } catch (e) {
      setErrorAccion(e.message)
    }
  }

  return (
    <div className="space-y-5">
      <Encabezado titulo="Mis postulaciones" descripcion="Una postulación avanza en un solo sentido; te avisamos por correo cada cambio." />
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      {errorAccion && <Aviso tipo="error">{errorAccion}</Aviso>}
      <Carga cargando={cargando && !datos} error={error}>
        {datos?.length === 0 ? (
          <Vacio>Aún no te has postulado. Busca ofertas en Convocatorias.</Vacio>
        ) : (
          <div className="space-y-3">
            {datos?.map((p) => (
              <article key={p.id} className="rounded-xl border border-slate-200 bg-white p-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h2 className="font-semibold text-slate-900">{p.oferta}</h2>
                    <p className="text-sm text-slate-600">
                      {p.empresa} · {p.municipio} · {NOMBRE_MODALIDAD[p.modalidad]} · postulado el {fecha(p.fecha)}
                    </p>
                  </div>
                  <EstadoInsignia estado={p.estado} />
                </div>
                <div className="mt-3 flex flex-wrap items-end justify-between gap-3">
                  <BarraCoincidencia coincidentes={p.coincidentes} total={p.totalCompetencias} />
                  {p.puedeAceptar && <Boton onClick={() => aceptar(p)}>Aceptar práctica</Boton>}
                  {p.fechaAceptacion && p.estado === 'SELECCIONADO' && (
                    <span className="text-sm text-marca-700">Aceptada el {fecha(p.fechaAceptacion)} · pendiente de formalizar</span>
                  )}
                </div>
                {p.estado === 'ENTREVISTA' && p.fechaEntrevista && (
                  <div className="mt-3"><Aviso tipo="info">
                    Entrevista: <strong>{fechaHora(p.fechaEntrevista)}</strong>{p.lugarEntrevista && <> en <strong>{p.lugarEntrevista}</strong></>}
                  </Aviso></div>
                )}
                {p.observaciones && <p className="mt-2 text-sm text-slate-600">Observaciones: {p.observaciones}</p>}
              </article>
            ))}
          </div>
        )}
      </Carga>
    </div>
  )
}
