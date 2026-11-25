import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../../api/cliente.js'
import { fecha, NOMBRE_NIVEL, NOMBRE_TAMANO, NOMBRE_TIPO_PORTAFOLIO } from '../../componentes/formato.js'
import { Aviso, Boton, Campo, Carga, Dato, Encabezado, EstadoInsignia, Modal, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

const ESTADOS = {
  empresas: ['PENDIENTE', 'VALIDADA', 'RECHAZADA', 'INACTIVA'],
  estudiantes: ['PENDIENTE', 'VALIDADO', 'RECHAZADO', 'INACTIVO'],
}

/** RF-07 y RF-16 · CU-04 Validar empresa o estudiante. */
export default function Validaciones() {
  const [params, setParams] = useSearchParams()
  const tipo = params.get('tipo') === 'estudiantes' ? 'estudiantes' : 'empresas'
  const [estado, setEstado] = useState('PENDIENTE')
  const { datos, error, cargando, recargar } = useCarga(
    () => api.get(`/admin/validaciones/${tipo}${estado ? `?estado=${estado}` : ''}`), [tipo, estado])
  const [decision, setDecision] = useState(null) // { item, decision }
  const [perfil, setPerfil] = useState(null)
  const [aviso, setAviso] = useState(null)

  function cambiarTipo(t) {
    setParams(t === 'estudiantes' ? { tipo: t } : {})
    setEstado('PENDIENTE')
    setAviso(null)
  }

  return (
    <div className="space-y-5">
      <Encabezado titulo="Validaciones" descripcion="Valida o rechaza los registros. El usuario recibe la decisión por correo." />
      <div className="flex gap-2" role="tablist">
        {['empresas', 'estudiantes'].map((t) => (
          <button key={t} role="tab" aria-selected={tipo === t} onClick={() => cambiarTipo(t)}
                  className={`rounded-lg px-4 py-2 text-sm font-semibold ${tipo === t ? 'bg-marca-600 text-white' : 'bg-white text-slate-700 ring-1 ring-slate-300'}`}>
            {t === 'empresas' ? 'Empresas' : 'Estudiantes'}
          </button>
        ))}
      </div>
      <div className="max-w-xs">
        <Campo etiqueta="Estado" como="select" value={estado} onChange={(e) => setEstado(e.target.value)}>
          <option value="">Todos</option>
          {ESTADOS[tipo].map((e) => <option key={e} value={e}>{e[0] + e.slice(1).toLowerCase()}</option>)}
        </Campo>
      </div>
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}

      <Carga cargando={cargando && !datos} error={error}>
        {datos?.length === 0 ? <Vacio>No hay registros {estado ? 'en ese estado' : ''}.</Vacio> : (
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
            <table className="w-full min-w-[760px] text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                {tipo === 'empresas' ? (
                  <tr><th className="px-4 py-3">Empresa</th><th className="px-4 py-3">NIT</th><th className="px-4 py-3">Municipio</th>
                    <th className="px-4 py-3">Registro</th><th className="px-4 py-3">Estado</th><th className="px-4 py-3 text-right">Acciones</th></tr>
                ) : (
                  <tr><th className="px-4 py-3">Estudiante</th><th className="px-4 py-3">Documento</th><th className="px-4 py-3">Programa</th>
                    <th className="px-4 py-3">Hoja de vida</th><th className="px-4 py-3">Estado</th><th className="px-4 py-3 text-right">Acciones</th></tr>
                )}
              </thead>
              <tbody className="divide-y divide-slate-100">
                {datos?.map((x) => (
                  <tr key={x.id} className="align-top">
                    {tipo === 'empresas' ? (
                      <>
                        <td className="px-4 py-3">
                          <div className="font-medium text-slate-900">{x.razonSocial}</div>
                          <div className="text-xs text-slate-500">{[x.nombreComercial, x.sector, NOMBRE_TAMANO[x.tamano]].filter(Boolean).join(' · ')}</div>
                          <div className="text-xs text-slate-500">{x.correo}{x.contacto && ` · ${x.contacto}`}</div>
                        </td>
                        <td className="px-4 py-3">{x.nit}</td>
                        <td className="px-4 py-3">{x.municipio}</td>
                        <td className="px-4 py-3">{fecha(x.fechaRegistro)}</td>
                      </>
                    ) : (
                      <>
                        <td className="px-4 py-3">
                          <button onClick={() => setPerfil(x.id)} className="text-left font-medium text-slate-900 hover:text-marca-700">{x.nombre}</button>
                          <div className="text-xs text-slate-500">{x.correo} · registro {fecha(x.fechaRegistro)}</div>
                        </td>
                        <td className="px-4 py-3">{x.documento}</td>
                        <td className="px-4 py-3">{x.programa}<div className="text-xs text-slate-500">Semestre {x.semestre}{x.municipio && ` · ${x.municipio}`}</div></td>
                        <td className="px-4 py-3">{x.tieneHojaVida ? 'Cargada' : <span className="text-slate-400">Sin cargar</span>}</td>
                      </>
                    )}
                    <td className="px-4 py-3">
                      <EstadoInsignia estado={x.estadoValidacion ?? x.estado} />
                      {x.motivoRechazo && <div className="mt-1 max-w-48 text-xs text-slate-500">{x.motivoRechazo}</div>}
                    </td>
                    <td className="space-x-2 whitespace-nowrap px-4 py-3 text-right">
                      {tipo === 'estudiantes' && <Boton variante="secundario" className="px-3 py-1.5" onClick={() => setPerfil(x.id)}>Ver perfil</Boton>}
                      {!['VALIDADA', 'VALIDADO'].includes(x.estadoValidacion ?? x.estado) && (
                        <Boton className="px-3 py-1.5" onClick={() => setDecision({ item: x, decision: 'VALIDAR' })}>Validar</Boton>
                      )}
                      {['PENDIENTE'].includes(x.estadoValidacion ?? x.estado) && (
                        <Boton variante="peligro" className="px-3 py-1.5" onClick={() => setDecision({ item: x, decision: 'RECHAZAR' })}>Rechazar</Boton>
                      )}
                      {['VALIDADA', 'VALIDADO'].includes(x.estadoValidacion ?? x.estado) && (
                        <Boton variante="peligro" className="px-3 py-1.5" onClick={() => setDecision({ item: x, decision: 'INACTIVAR' })}>Inactivar</Boton>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Carga>

      {decision && (
        <Decision tipo={tipo} {...decision} onCerrar={() => setDecision(null)}
                  onGuardado={(m) => { setDecision(null); setAviso(m); recargar() }} />
      )}
      {perfil && <PerfilEstudiante id={perfil} onCerrar={() => setPerfil(null)} />}
    </div>
  )
}

function Decision({ tipo, item, decision, onCerrar, onGuardado }) {
  const [motivo, setMotivo] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const nombre = item.razonSocial ?? item.nombre
  const textos = { VALIDAR: 'Validar', RECHAZAR: 'Rechazar', INACTIVAR: 'Inactivar' }
  const resultado = { VALIDAR: 'quedó validado', RECHAZAR: 'quedó rechazado', INACTIVAR: 'quedó inactivo' }

  async function confirmar(e) {
    e.preventDefault()
    setEnviando(true)
    setError(null)
    try {
      await api.patch(`/admin/validaciones/${tipo}/${item.id}`, { decision, motivo })
      onGuardado(`El registro de ${nombre} ${resultado[decision]}. Se le avisó por correo.`)
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal titulo={`${textos[decision]} · ${nombre}`} onCerrar={onCerrar}>
      <form onSubmit={confirmar} className="space-y-4">
        {error && <Aviso tipo="error">{error}</Aviso>}
        {decision === 'VALIDAR'
          ? <p className="text-sm text-slate-700">Confirma que revisaste los datos. {tipo === 'empresas' ? 'La empresa podrá publicar ofertas.' : 'El estudiante podrá postularse si tiene hoja de vida.'}</p>
          : <Campo etiqueta="Motivo (lo recibe el usuario)" como="textarea" rows={3} value={motivo} onChange={(e) => setMotivo(e.target.value)} />}
        <div className="flex justify-end gap-2">
          <Boton type="button" variante="secundario" onClick={onCerrar}>Cancelar</Boton>
          <Boton type="submit" variante={decision === 'VALIDAR' ? 'primario' : 'peligro'} disabled={enviando}>{textos[decision]}</Boton>
        </div>
      </form>
    </Modal>
  )
}

function PerfilEstudiante({ id, onCerrar }) {
  const { datos: p, error, cargando } = useCarga(() => api.get(`/admin/validaciones/estudiantes/${id}`), [id])
  const [errorPdf, setErrorPdf] = useState(null)
  return (
    <Modal titulo={p ? `${p.nombres} ${p.apellidos}` : 'Perfil del estudiante'} ancho="max-w-2xl" onCerrar={onCerrar}>
      <Carga cargando={cargando} error={error}>
        {p && (
          <div className="space-y-4">
            <dl className="grid grid-cols-2 gap-3 sm:grid-cols-3">
              <Dato etiqueta="Documento">{p.documento}</Dato>
              <Dato etiqueta="Correo">{p.correo}</Dato>
              <Dato etiqueta="Estado"><EstadoInsignia estado={p.estado} /></Dato>
              <Dato etiqueta="Programa">{p.programa}</Dato>
              <Dato etiqueta="Semestre">{p.semestre}</Dato>
              <Dato etiqueta="Municipio">{p.municipio}</Dato>
            </dl>
            <div>
              <h3 className="text-xs font-medium uppercase tracking-wide text-slate-500">Competencias</h3>
              <p className="mt-1 text-sm">{p.competencias.length ? p.competencias.map((c) => `${c.nombre} (${NOMBRE_NIVEL[c.nivel]})`).join(', ') : '—'}</p>
            </div>
            <div>
              <h3 className="text-xs font-medium uppercase tracking-wide text-slate-500">Portafolio</h3>
              <p className="mt-1 text-sm">{p.portafolio.length ? p.portafolio.map((x) => `${NOMBRE_TIPO_PORTAFOLIO[x.tipo]}: ${x.titulo}`).join(' · ') : '—'}</p>
            </div>
            {errorPdf && <Aviso tipo="error">{errorPdf}</Aviso>}
            {p.hojaVida
              ? <Boton variante="secundario" onClick={() => api.abrirPdf(`/admin/validaciones/estudiantes/${id}/hoja-vida`).catch((e) => setErrorPdf(e.message))}>
                  Ver hoja de vida (versión {p.hojaVida.version})
                </Boton>
              : <p className="text-sm text-slate-500">No ha cargado hoja de vida.</p>}
          </div>
        )}
      </Carga>
    </Modal>
  )
}
