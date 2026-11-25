import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api/cliente.js'
import { MUNICIPIOS_URABA } from '../../auth/roles.js'
import FichaOferta from '../../componentes/FichaOferta.jsx'
import { dinero, fecha, NOMBRE_MODALIDAD, NOMBRE_TAMANO } from '../../componentes/formato.js'
import { Aviso, BarraCoincidencia, Boton, Campo, Carga, Dato, Encabezado, Modal, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'
import EstadoPerfil from './EstadoPerfil.jsx'

/** Pantalla 2 del prototipo · RF-17, RF-18, RF-22, RF-25 · CU-08 Buscar y postularse. */
export default function Convocatorias() {
  const [filtros, setFiltros] = useState({ q: '', areaId: '', municipio: '', modalidad: '' })
  const [detalle, setDetalle] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [errorAccion, setErrorAccion] = useState(null)
  const { datos: perfil } = useCarga(() => api.get('/estudiante/perfil'))
  const { datos: areas } = useCarga(() => api.get('/catalogos/areas'))
  const { datos: lista, setDatos, error, cargando } = useCarga(() => {
    const p = new URLSearchParams()
    Object.entries(filtros).forEach(([k, v]) => v && p.set(k, v))
    return api.get(`/estudiante/convocatorias?${p}`)
  }, [filtros])

  const filtrar = (e) => setFiltros((f) => ({ ...f, [e.target.name]: e.target.value }))

  async function postular(c) {
    setErrorAccion(null)
    try {
      await api.post(`/estudiante/convocatorias/${c.oferta.id}/postular`)
      setAviso(`Te postulaste a "${c.oferta.titulo}". Sigue el proceso en Mis postulaciones.`)
      setDatos((l) => l.map((x) => (x.oferta.id === c.oferta.id ? { ...x, yaPostulado: true } : x)))
      setDetalle(null)
    } catch (e) {
      setErrorAccion(e.message)
    }
  }

  return (
    <div className="space-y-5">
      <Encabezado titulo="Convocatorias" descripcion="Solo aparecen ofertas aprobadas por el comité evaluador." />
      {perfil && <EstadoPerfil perfil={perfil} conEnlace />}
      {aviso && <Aviso tipo="exito">{aviso} <Link to="/estudiante/postulaciones" className="font-semibold underline">Ver</Link></Aviso>}
      {errorAccion && <Aviso tipo="error">{errorAccion}</Aviso>}

      <div className="grid gap-3 rounded-xl border border-slate-200 bg-white p-4 sm:grid-cols-4">
        <Campo etiqueta="Buscar" name="q" value={filtros.q} onChange={filtrar} placeholder="Cargo, empresa o palabra clave" />
        <Campo etiqueta="Área" como="select" name="areaId" value={filtros.areaId} onChange={filtrar}>
          <option value="">Todas</option>
          {areas?.map((a) => <option key={a.id} value={a.id}>{a.nombre}</option>)}
        </Campo>
        <Campo etiqueta="Municipio" como="select" name="municipio" value={filtros.municipio} onChange={filtrar}>
          <option value="">Todos</option>
          {MUNICIPIOS_URABA.map((m) => <option key={m}>{m}</option>)}
        </Campo>
        <Campo etiqueta="Modalidad" como="select" name="modalidad" value={filtros.modalidad} onChange={filtrar}>
          <option value="">Todas</option>
          {Object.entries(NOMBRE_MODALIDAD).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
        </Campo>
      </div>

      <Carga cargando={cargando && !lista} error={error}>
        {lista?.length === 0 ? (
          <Vacio>No hay convocatorias abiertas con esos filtros.</Vacio>
        ) : (
          <div className="grid gap-4 lg:grid-cols-2">
            {lista?.map((c) => (
              <article key={c.oferta.id} className="flex flex-col rounded-xl border border-slate-200 bg-white p-5">
                <h2 className="text-lg font-semibold text-slate-900">{c.oferta.titulo}</h2>
                <p className="text-sm text-slate-600">{c.empresa.nombre} · {c.oferta.area}</p>
                <dl className="mt-3 grid grid-cols-2 gap-2 sm:grid-cols-4">
                  <Dato etiqueta="Municipio">{c.oferta.municipio}</Dato>
                  <Dato etiqueta="Modalidad">{NOMBRE_MODALIDAD[c.oferta.modalidad]}</Dato>
                  <Dato etiqueta="Duración">{c.oferta.duracionMeses} meses</Dato>
                  <Dato etiqueta="Incentivo">{dinero(c.oferta.incentivo)}</Dato>
                </dl>
                <div className="mt-3"><BarraCoincidencia coincidentes={c.coincidentes} total={c.oferta.competencias.length} /></div>
                <p className="mt-2 text-xs text-slate-500">Cierra el {fecha(c.oferta.fechaCierre)}</p>
                <div className="mt-4 flex flex-wrap gap-2 pt-1">
                  <Boton variante="secundario" onClick={() => setDetalle(c)}>Ver detalle</Boton>
                  <BotonPostular c={c} perfil={perfil} onPostular={postular} />
                </div>
              </article>
            ))}
          </div>
        )}
      </Carga>

      {detalle && (
        <Modal titulo={detalle.oferta.titulo} ancho="max-w-3xl" onCerrar={() => setDetalle(null)}>
          <div className="space-y-5">
            <div className="rounded-lg bg-slate-50 p-4">
              <h3 className="font-semibold text-slate-900">{detalle.empresa.nombre}</h3>
              <p className="text-sm text-slate-600">
                {[detalle.empresa.razonSocial !== detalle.empresa.nombre && detalle.empresa.razonSocial, detalle.empresa.sector,
                  NOMBRE_TAMANO[detalle.empresa.tamano], detalle.empresa.municipio].filter(Boolean).join(' · ')}
              </p>
            </div>
            <BarraCoincidencia coincidentes={detalle.coincidentes} total={detalle.oferta.competencias.length} />
            <FichaOferta oferta={detalle.oferta} coincidentes={detalle.competenciasCoincidentes} />
            {errorAccion && <Aviso tipo="error">{errorAccion}</Aviso>}
            <div className="flex justify-end gap-2">
              <Boton variante="secundario" onClick={() => setDetalle(null)}>Cerrar</Boton>
              <BotonPostular c={detalle} perfil={perfil} onPostular={postular} />
            </div>
          </div>
        </Modal>
      )}
    </div>
  )
}

function BotonPostular({ c, perfil, onPostular }) {
  const [enviando, setEnviando] = useState(false)
  if (c.yaPostulado) return <Boton disabled>Ya te postulaste</Boton>
  if (!c.vacantesDisponibles) return <Boton disabled>Vacantes cubiertas</Boton>
  const bloqueado = perfil && !perfil.puedePostularse
  return (
    <Boton disabled={enviando || bloqueado} title={bloqueado ? 'Necesitas estar validado y tener hoja de vida' : undefined}
           onClick={async () => { setEnviando(true); await onPostular(c); setEnviando(false) }}>
      {enviando ? 'Enviando…' : 'Postularme'}
    </Boton>
  )
}
