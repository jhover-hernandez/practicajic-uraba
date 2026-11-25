import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api/cliente.js'
import { MUNICIPIOS_URABA } from '../../auth/roles.js'
import FichaOferta from '../../componentes/FichaOferta.jsx'
import { fecha, hoyIso, NOMBRE_MODALIDAD, NOMBRE_NIVEL, num } from '../../componentes/formato.js'
import { Aviso, Boton, Campo, Carga, Encabezado, EstadoInsignia, Modal, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'
import { useFormulario } from '../../componentes/useFormulario.js'

/** Pantalla 3 del prototipo · RF-08 y RF-09 · CU-05 Publicar oferta. */
export default function Ofertas() {
  const { datos: perfil } = useCarga(() => api.get('/empresa/perfil'))
  const { datos: ofertas, error, cargando, recargar } = useCarga(() => api.get('/empresa/ofertas'))
  const [edicion, setEdicion] = useState(null)
  const [detalle, setDetalle] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [errorAccion, setErrorAccion] = useState(null)
  const validada = perfil?.estadoValidacion === 'VALIDADA'

  async function accion(texto, fn, exito) {
    if (texto && !window.confirm(texto)) return
    setErrorAccion(null)
    try {
      await fn()
      setAviso(exito)
      recargar()
    } catch (e) {
      setErrorAccion(e.message)
    }
  }

  return (
    <div className="space-y-5">
      <Encabezado titulo="Mis ofertas" descripcion="Cada oferta pasa por el comité evaluador antes de publicarse como convocatoria.">
        <Boton disabled={!validada} onClick={() => setEdicion({})}>+ Publicar nueva oferta</Boton>
      </Encabezado>
      {perfil && (validada
        ? <Aviso tipo="exito">Empresa validada por el Politécnico. Ya puedes publicar ofertas.</Aviso>
        : <Aviso tipo="alerta">La empresa debe estar validada por el Politécnico para publicar ofertas. Revisa el <Link to="/empresa/perfil" className="font-semibold underline">perfil de la empresa</Link>.</Aviso>)}
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      {errorAccion && <Aviso tipo="error">{errorAccion}</Aviso>}

      <Carga cargando={cargando && !ofertas} error={error}>
        {ofertas?.length === 0 ? <Vacio>Todavía no has creado ofertas.</Vacio> : (
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
            <table className="w-full min-w-[760px] text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Oferta</th>
                  <th className="px-4 py-3">Vacantes</th>
                  <th className="px-4 py-3">Postulados</th>
                  <th className="px-4 py-3">Cierre</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {ofertas?.map((o) => (
                  <tr key={o.id} className="align-top">
                    <td className="px-4 py-3">
                      <button onClick={() => setDetalle(o)} className="text-left font-medium text-slate-900 hover:text-marca-700">{o.titulo}</button>
                      <div className="text-xs text-slate-500">{o.area} · {NOMBRE_MODALIDAD[o.modalidad]} · {o.municipio}</div>
                      {o.observacionesComite && ['AJUSTES_SOLICITADOS', 'RECHAZADA', 'APROBADA'].includes(o.estado) && (
                        <div className="mt-1 max-w-md text-xs text-slate-600">Comité: {o.observacionesComite}</div>
                      )}
                    </td>
                    <td className="px-4 py-3">{o.vacantesCubiertas} / {o.vacantes}</td>
                    <td className="px-4 py-3">{o.postulados}</td>
                    <td className="px-4 py-3">{fecha(o.fechaCierre)}</td>
                    <td className="px-4 py-3"><EstadoInsignia estado={o.estado} /></td>
                    <td className="space-x-2 whitespace-nowrap px-4 py-3 text-right">
                      {['BORRADOR', 'AJUSTES_SOLICITADOS'].includes(o.estado) && (
                        <>
                          <Boton variante="secundario" className="px-3 py-1.5" onClick={() => setEdicion(o)}>Editar</Boton>
                          <Boton className="px-3 py-1.5" onClick={() => accion(null, () => api.post(`/empresa/ofertas/${o.id}/enviar`),
                            `"${o.titulo}" se envió al comité evaluador.`)}>Enviar al comité</Boton>
                        </>
                      )}
                      {o.estado === 'BORRADOR' && (
                        <Boton variante="peligro" className="px-3 py-1.5" onClick={() => accion(`¿Eliminar el borrador "${o.titulo}"?`,
                          () => api.delete(`/empresa/ofertas/${o.id}`), 'Se eliminó el borrador.')}>Eliminar</Boton>
                      )}
                      {['APROBADA', 'CERRADA'].includes(o.estado) && (
                        <Link to={`/empresa/postulantes?oferta=${o.id}`}
                              className="inline-flex rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-semibold text-slate-700 hover:bg-slate-50">
                          Postulantes
                        </Link>
                      )}
                      {o.estado === 'APROBADA' && (
                        <Boton variante="peligro" className="px-3 py-1.5" onClick={() => accion(
                          `¿Cerrar la convocatoria "${o.titulo}"? Ya no recibirá postulaciones.`,
                          () => api.post(`/empresa/ofertas/${o.id}/cerrar`), 'Se cerró la convocatoria.')}>Cerrar</Boton>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Carga>

      {edicion && (
        <FormularioOferta oferta={edicion} perfil={perfil} onCerrar={() => setEdicion(null)}
                          onGuardado={(mensaje) => { setEdicion(null); setAviso(mensaje); recargar() }} />
      )}
      {detalle && (
        <Modal titulo={detalle.titulo} ancho="max-w-3xl" onCerrar={() => setDetalle(null)}>
          <div className="mb-4 flex items-center gap-2 text-sm">Estado: <EstadoInsignia estado={detalle.estado} /></div>
          <FichaOferta oferta={detalle} />
        </Modal>
      )}
    </div>
  )
}

function FormularioOferta({ oferta, perfil, onCerrar, onGuardado }) {
  const nueva = !oferta.id
  const enviarARevision = useRef(false)
  const { datos: areas } = useCarga(() => api.get('/catalogos/areas'))
  const { datos: catalogo } = useCarga(() => api.get('/catalogos/competencias'))
  const [competencias, setCompetencias] = useState(
    oferta.competencias?.map((c) => ({ competenciaId: c.competenciaId, nivelRequerido: c.nivelRequerido })) ?? [])
  const [porAgregar, setPorAgregar] = useState({ competenciaId: '', nivelRequerido: 'BASICO' })

  const { mensajeError, errores, enviando, alEnviar, campo } = useFormulario(
    {
      titulo: oferta.titulo ?? '', areaId: oferta.areaId ?? '', descripcion: oferta.descripcion ?? '',
      perfil: oferta.perfil ?? '', actividades: oferta.actividades ?? '', modalidad: oferta.modalidad ?? 'PRESENCIAL',
      municipio: oferta.municipio ?? perfil?.municipio ?? '', duracionMeses: oferta.duracionMeses ?? 6,
      vacantes: oferta.vacantes ?? 1, horario: oferta.horario ?? '', tipoVinculacion: oferta.tipoVinculacion ?? '',
      incentivo: oferta.incentivo ?? '', fechaCierre: oferta.fechaCierre ?? '',
    },
    async (v) => {
      const cuerpo = {
        ...v, areaId: num(v.areaId), duracionMeses: num(v.duracionMeses), vacantes: num(v.vacantes),
        incentivo: num(v.incentivo), fechaCierre: v.fechaCierre || null, competencias,
        enviarARevision: enviarARevision.current,
      }
      if (nueva) await api.post('/empresa/ofertas', cuerpo)
      else await api.put(`/empresa/ofertas/${oferta.id}`, cuerpo)
      onGuardado(enviarARevision.current
        ? `"${v.titulo}" se envió al comité evaluador. Te avisaremos su decisión por correo.`
        : `"${v.titulo}" quedó guardada como borrador.`)
    },
  )
  const nombre = (id) => catalogo?.find((c) => c.id === id)?.nombre ?? '…'

  function agregar() {
    if (!porAgregar.competenciaId) return
    setCompetencias((l) => [...l, { competenciaId: Number(porAgregar.competenciaId), nivelRequerido: porAgregar.nivelRequerido }])
    setPorAgregar({ competenciaId: '', nivelRequerido: 'BASICO' })
  }

  return (
    <Modal titulo={nueva ? 'Publicar nueva oferta' : 'Editar oferta'} ancho="max-w-3xl" onCerrar={onCerrar}>
      <form onSubmit={alEnviar} noValidate className="space-y-4">
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        {oferta.estado === 'AJUSTES_SOLICITADOS' && oferta.observacionesComite && (
          <Aviso tipo="alerta">Ajustes que pidió el comité: {oferta.observacionesComite}</Aviso>
        )}
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo etiqueta="Título o cargo" className="sm:col-span-2" {...campo('titulo')} />
          <Campo etiqueta="Área" como="select" {...campo('areaId')}>
            <option value="">Selecciona</option>
            {areas?.map((a) => <option key={a.id} value={a.id}>{a.nombre}</option>)}
          </Campo>
          <Campo etiqueta="Modalidad" como="select" {...campo('modalidad')}>
            {Object.entries(NOMBRE_MODALIDAD).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
          </Campo>
        </div>
        <Campo etiqueta="Descripción de la necesidad" como="textarea" rows={3} {...campo('descripcion')} />
        <Campo etiqueta="Perfil buscado (opcional)" como="textarea" rows={2} {...campo('perfil')} />
        <Campo etiqueta="Actividades del practicante" como="textarea" rows={3} {...campo('actividades')} />

        <fieldset className="rounded-lg border border-slate-200 p-3">
          <legend className="px-1 text-sm font-medium text-slate-700">Competencias requeridas</legend>
          {errores.competencias && <p className="mb-2 text-xs text-red-600">{errores.competencias}</p>}
          <div className="grid gap-3 sm:grid-cols-[1fr_150px_auto] sm:items-end">
            <Campo etiqueta="Competencia" como="select" value={porAgregar.competenciaId}
                   onChange={(e) => setPorAgregar((n) => ({ ...n, competenciaId: e.target.value }))}>
              <option value="">Selecciona</option>
              {catalogo?.filter((c) => !competencias.some((x) => x.competenciaId === c.id))
                .map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </Campo>
            <Campo etiqueta="Nivel" como="select" value={porAgregar.nivelRequerido}
                   onChange={(e) => setPorAgregar((n) => ({ ...n, nivelRequerido: e.target.value }))}>
              {Object.entries(NOMBRE_NIVEL).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
            </Campo>
            <Boton type="button" variante="secundario" onClick={agregar} disabled={!porAgregar.competenciaId}>Agregar</Boton>
          </div>
          <ul className="mt-3 flex flex-wrap gap-2">
            {competencias.map((c, i) => (
              <li key={c.competenciaId} className="flex items-center gap-1 rounded-full bg-slate-100 py-1 pr-1 pl-3 text-sm">
                {nombre(c.competenciaId)} · {NOMBRE_NIVEL[c.nivelRequerido]}
                <button type="button" onClick={() => setCompetencias((l) => l.filter((_, j) => j !== i))}
                        className="rounded-full px-2 hover:bg-slate-200" aria-label={`Quitar ${nombre(c.competenciaId)}`}>×</button>
              </li>
            ))}
          </ul>
        </fieldset>

        <div className="grid gap-4 sm:grid-cols-3">
          <Campo etiqueta="Municipio" como="select" {...campo('municipio')}>
            <option value="">Selecciona</option>
            {MUNICIPIOS_URABA.map((m) => <option key={m}>{m}</option>)}
          </Campo>
          <Campo etiqueta="Duración (meses)" type="number" min="1" max="24" {...campo('duracionMeses')} />
          <Campo etiqueta="Vacantes" type="number" min="1" max="50" {...campo('vacantes')} />
          <Campo etiqueta="Horario" placeholder="Lunes a viernes, 8 a. m. a 5 p. m." {...campo('horario')} />
          <Campo etiqueta="Tipo de vinculación" placeholder="Contrato de aprendizaje, convenio…" {...campo('tipoVinculacion')} />
          <Campo etiqueta="Incentivo mensual (COP)" type="number" min="0" step="1000" {...campo('incentivo')} />
          <Campo etiqueta="Fecha de cierre" type="date" min={hoyIso()} {...campo('fechaCierre')} />
        </div>

        <div className="flex flex-wrap justify-end gap-2 pt-2">
          <Boton type="button" variante="secundario" onClick={onCerrar}>Cancelar</Boton>
          <Boton type="submit" variante="secundario" disabled={enviando}
                 onClick={() => { enviarARevision.current = false }}>Guardar borrador</Boton>
          <Boton type="submit" disabled={enviando} onClick={() => { enviarARevision.current = true }}>
            {enviando ? 'Enviando…' : 'Enviar al comité'}
          </Boton>
        </div>
      </form>
    </Modal>
  )
}
