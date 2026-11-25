import { useState } from 'react'
import { api } from '../../api/cliente.js'
import { useAuth } from '../../auth/AuthContext.jsx'
import { MUNICIPIOS_URABA } from '../../auth/roles.js'
import { fecha, NOMBRE_NIVEL, NOMBRE_TIPO_PORTAFOLIO } from '../../componentes/formato.js'
import { Aviso, Boton, Campo, Carga, Encabezado, EstadoInsignia, Modal, Tarjeta } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'
import { useFormulario } from '../../componentes/useFormulario.js'
import EstadoPerfil from './EstadoPerfil.jsx'

const TAMANO_MAXIMO = 5 * 1024 * 1024

/** RF-12 a RF-15 · CU-07 Completar perfil digital. */
export default function Perfil() {
  const { datos: perfil, setDatos, error, cargando } = useCarga(() => api.get('/estudiante/perfil'))
  const [aviso, setAviso] = useState(null)

  function actualizado(nuevo, mensaje) {
    setDatos(nuevo)
    setAviso(mensaje)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <div className="max-w-4xl space-y-5">
      <Encabezado titulo="Mi perfil digital"
                  descripcion="Las empresas ven esta información solo en las convocatorias a las que te postulas." />
      <Carga cargando={cargando} error={error}>
        {perfil && (
          <>
            {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
            {perfil.estado === 'RECHAZADO' && perfil.motivoRechazo && (
              <Aviso tipo="error">
                Tu registro fue rechazado: {perfil.motivoRechazo}. Corrige tus datos y guárdalos para que se revisen de nuevo.
              </Aviso>
            )}
            <EstadoPerfil perfil={perfil} />
            <DatosPersonales perfil={perfil} onGuardado={(p) => actualizado(p, 'Tus datos se guardaron.')} />
            <HojaVida perfil={perfil} onGuardado={(p) => actualizado(p, 'Tu hoja de vida se cargó.')} />
            <Competencias perfil={perfil} onGuardado={(p) => actualizado(p, 'Tus competencias se guardaron.')} />
            <Portafolio perfil={perfil} onCambio={(p) => setDatos(p)} />
          </>
        )}
      </Carga>
    </div>
  )
}

function DatosPersonales({ perfil, onGuardado }) {
  const { refrescarUsuario } = useAuth()
  const { mensajeError, enviando, alEnviar, campo } = useFormulario(
    {
      nombres: perfil.nombres, apellidos: perfil.apellidos, programa: perfil.programa,
      semestre: perfil.semestre ?? '', municipio: perfil.municipio ?? '', disponibilidad: perfil.disponibilidad ?? '',
    },
    async (v) => {
      const nuevo = await api.put('/estudiante/perfil', { ...v, semestre: v.semestre ? Number(v.semestre) : null })
      onGuardado(nuevo)
      refrescarUsuario().catch(() => {})
    },
  )
  return (
    <Tarjeta titulo="Información personal y académica"
             acciones={<span className="flex items-center gap-2 text-sm text-slate-500">Estado: <EstadoInsignia estado={perfil.estado} /></span>}>
      <form onSubmit={alEnviar} noValidate className="space-y-4">
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo etiqueta="Nombres" {...campo('nombres')} />
          <Campo etiqueta="Apellidos" {...campo('apellidos')} />
          <Campo etiqueta="Documento" value={perfil.documento} disabled readOnly />
          <Campo etiqueta="Correo" value={perfil.correo} disabled readOnly />
          <Campo etiqueta="Programa académico" {...campo('programa')} />
          <Campo etiqueta="Semestre" como="select" {...campo('semestre')}>
            <option value="">Selecciona</option>
            {Array.from({ length: 12 }, (_, i) => i + 1).map((n) => <option key={n} value={n}>{n}</option>)}
          </Campo>
          <Campo etiqueta="Municipio" como="select" {...campo('municipio')}>
            <option value="">Selecciona</option>
            {MUNICIPIOS_URABA.map((m) => <option key={m}>{m}</option>)}
          </Campo>
          <Campo etiqueta="Disponibilidad" placeholder="Ej.: tiempo completo, mañanas" {...campo('disponibilidad')} />
        </div>
        <div className="flex justify-end">
          <Boton type="submit" disabled={enviando}>{enviando ? 'Guardando…' : 'Guardar datos'}</Boton>
        </div>
      </form>
    </Tarjeta>
  )
}

function HojaVida({ perfil, onGuardado }) {
  const [error, setError] = useState(null)
  const [subiendo, setSubiendo] = useState(false)
  const hoja = perfil.hojaVida

  async function subir(e) {
    const archivo = e.target.files[0]
    e.target.value = ''
    if (!archivo) return
    setError(null)
    // RNF-04: se valida también en el servidor; aquí solo se evita una espera inútil.
    if (archivo.type !== 'application/pdf' && !archivo.name.toLowerCase().endsWith('.pdf')) {
      setError('Solo se aceptan archivos en formato PDF.')
      return
    }
    if (archivo.size > TAMANO_MAXIMO) {
      setError('El archivo supera el tamaño máximo de 5 MB.')
      return
    }
    setSubiendo(true)
    try {
      onGuardado(await api.subir('/estudiante/hoja-vida', archivo))
    } catch (err) {
      setError(err.message)
    } finally {
      setSubiendo(false)
    }
  }

  async function ver() {
    try {
      await api.abrirPdf('/estudiante/hoja-vida')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <Tarjeta titulo="Hoja de vida">
      {error && <div className="mb-3"><Aviso tipo="error">{error}</Aviso></div>}
      <div className="flex flex-wrap items-center justify-between gap-3">
        {hoja ? (
          <div className="text-sm">
            <p className="font-medium text-slate-800">{hoja.nombreOriginal}</p>
            <p className="text-slate-500">Versión {hoja.version} · actualizada el {fecha(hoja.fechaActualizacion)} · {(hoja.tamanoBytes / 1024).toFixed(0)} KB</p>
          </div>
        ) : (
          <p className="text-sm text-slate-600">Aún no has cargado tu hoja de vida. Es obligatoria para postularte.</p>
        )}
        <div className="flex gap-2">
          {hoja && <Boton variante="secundario" onClick={ver}>Ver PDF</Boton>}
          <label className={`inline-flex cursor-pointer items-center rounded-lg bg-marca-600 px-4 py-2 text-sm font-semibold text-white hover:bg-marca-700 ${subiendo ? 'opacity-60' : ''}`}>
            {subiendo ? 'Cargando…' : hoja ? 'Actualizar PDF' : 'Cargar PDF'}
            <input type="file" accept="application/pdf,.pdf" className="sr-only" onChange={subir} disabled={subiendo} />
          </label>
        </div>
      </div>
      <p className="mt-2 text-xs text-slate-500">Solo PDF, máximo 5 MB. Cada carga crea una versión nueva.</p>
    </Tarjeta>
  )
}

function Competencias({ perfil, onGuardado }) {
  const { datos: catalogo } = useCarga(() => api.get('/catalogos/competencias'))
  const [lista, setLista] = useState(perfil.competencias.map((c) => ({ competenciaId: c.competenciaId, nivel: c.nivel })))
  const [nueva, setNueva] = useState({ competenciaId: '', nivel: 'BASICO' })
  const [error, setError] = useState(null)
  const [guardando, setGuardando] = useState(false)
  const nombre = (id) => catalogo?.find((c) => c.id === id)?.nombre ?? '…'
  const disponibles = catalogo?.filter((c) => !lista.some((l) => l.competenciaId === c.id)) ?? []

  function agregar() {
    if (!nueva.competenciaId) return
    setLista((l) => [...l, { competenciaId: Number(nueva.competenciaId), nivel: nueva.nivel }])
    setNueva({ competenciaId: '', nivel: 'BASICO' })
  }

  async function guardar() {
    setGuardando(true)
    setError(null)
    try {
      onGuardado(await api.put('/estudiante/competencias', { competencias: lista }))
    } catch (e) {
      setError(e.message)
    } finally {
      setGuardando(false)
    }
  }

  return (
    <Tarjeta titulo="Competencias técnicas y blandas"
             acciones={<Boton onClick={guardar} disabled={guardando}>{guardando ? 'Guardando…' : 'Guardar competencias'}</Boton>}>
      {error && <div className="mb-3"><Aviso tipo="error">{error}</Aviso></div>}
      <div className="grid gap-3 sm:grid-cols-[1fr_160px_auto] sm:items-end">
        <Campo etiqueta="Competencia" como="select" value={nueva.competenciaId}
               onChange={(e) => setNueva((n) => ({ ...n, competenciaId: e.target.value }))}>
          <option value="">Selecciona</option>
          {['TECNICA', 'BLANDA'].map((cat) => (
            <optgroup key={cat} label={cat === 'TECNICA' ? 'Técnicas' : 'Blandas'}>
              {disponibles.filter((c) => c.categoria === cat).map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </optgroup>
          ))}
        </Campo>
        <Campo etiqueta="Nivel" como="select" value={nueva.nivel} onChange={(e) => setNueva((n) => ({ ...n, nivel: e.target.value }))}>
          {Object.entries(NOMBRE_NIVEL).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
        </Campo>
        <Boton variante="secundario" onClick={agregar} disabled={!nueva.competenciaId}>Agregar</Boton>
      </div>
      {lista.length === 0 ? (
        <p className="mt-4 text-sm text-slate-500">Agrega las competencias que dominas. Así verás qué convocatorias se ajustan a ti.</p>
      ) : (
        <ul className="mt-4 flex flex-wrap gap-2">
          {lista.map((c, i) => (
            <li key={c.competenciaId} className="flex items-center gap-2 rounded-full bg-marca-50 py-1 pr-1 pl-3 text-sm text-marca-800">
              {nombre(c.competenciaId)}
              <select aria-label={`Nivel de ${nombre(c.competenciaId)}`} value={c.nivel}
                      onChange={(e) => setLista((l) => l.map((x, j) => (j === i ? { ...x, nivel: e.target.value } : x)))}
                      className="rounded-full border-0 bg-white px-2 py-0.5 text-xs">
                {Object.entries(NOMBRE_NIVEL).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
              </select>
              <button onClick={() => setLista((l) => l.filter((_, j) => j !== i))}
                      className="rounded-full px-2 text-marca-800 hover:bg-marca-100" aria-label={`Quitar ${nombre(c.competenciaId)}`}>×</button>
            </li>
          ))}
        </ul>
      )}
      <p className="mt-3 text-xs text-slate-500">Recuerda guardar después de agregar o quitar competencias.</p>
    </Tarjeta>
  )
}

function Portafolio({ perfil, onCambio }) {
  const [edicion, setEdicion] = useState(null)
  const [error, setError] = useState(null)

  async function recargar() {
    onCambio(await api.get('/estudiante/perfil'))
  }

  async function eliminar(p) {
    if (!window.confirm(`¿Eliminar "${p.titulo}" del portafolio?`)) return
    try {
      await api.delete(`/estudiante/portafolio/${p.id}`)
      await recargar()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <Tarjeta titulo="Portafolio" acciones={<Boton variante="secundario" onClick={() => setEdicion({})}>+ Agregar</Boton>}>
      {error && <div className="mb-3"><Aviso tipo="error">{error}</Aviso></div>}
      {perfil.portafolio.length === 0 ? (
        <p className="text-sm text-slate-500">Agrega proyectos, repositorios o certificaciones que muestren lo que sabes hacer.</p>
      ) : (
        <ul className="divide-y divide-slate-100">
          {perfil.portafolio.map((p) => (
            <li key={p.id} className="flex flex-wrap items-start justify-between gap-3 py-3">
              <div className="min-w-0">
                <p className="font-medium text-slate-800">
                  {p.titulo} <span className="text-xs font-normal text-slate-500">· {NOMBRE_TIPO_PORTAFOLIO[p.tipo]}</span>
                </p>
                {p.url && <a href={p.url} target="_blank" rel="noreferrer noopener" className="break-all text-sm text-marca-700 hover:underline">{p.url}</a>}
                {p.descripcion && <p className="text-sm text-slate-600">{p.descripcion}</p>}
              </div>
              <div className="flex gap-2">
                <Boton variante="secundario" className="px-3 py-1.5" onClick={() => setEdicion(p)}>Editar</Boton>
                <Boton variante="peligro" className="px-3 py-1.5" onClick={() => eliminar(p)}>Eliminar</Boton>
              </div>
            </li>
          ))}
        </ul>
      )}
      {edicion && (
        <FormularioPortafolio elemento={edicion} onCerrar={() => setEdicion(null)}
                              onGuardado={async () => { setEdicion(null); await recargar() }} />
      )}
    </Tarjeta>
  )
}

function FormularioPortafolio({ elemento, onCerrar, onGuardado }) {
  const nuevo = !elemento.id
  const { mensajeError, enviando, alEnviar, campo } = useFormulario(
    { tipo: elemento.tipo ?? 'PROYECTO', titulo: elemento.titulo ?? '', url: elemento.url ?? '', descripcion: elemento.descripcion ?? '' },
    async (v) => {
      if (nuevo) await api.post('/estudiante/portafolio', v)
      else await api.put(`/estudiante/portafolio/${elemento.id}`, v)
      await onGuardado()
    },
  )
  return (
    <Modal titulo={nuevo ? 'Agregar al portafolio' : 'Editar elemento'} onCerrar={onCerrar}>
      <form onSubmit={alEnviar} noValidate className="space-y-4">
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        <Campo etiqueta="Tipo" como="select" {...campo('tipo')}>
          {Object.entries(NOMBRE_TIPO_PORTAFOLIO).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
        </Campo>
        <Campo etiqueta="Título" {...campo('titulo')} />
        <Campo etiqueta="Enlace (opcional)" type="url" placeholder="https://github.com/usuario/proyecto" {...campo('url')} />
        <Campo etiqueta="Descripción (opcional)" como="textarea" rows={3} {...campo('descripcion')} />
        <div className="flex justify-end gap-2">
          <Boton type="button" variante="secundario" onClick={onCerrar}>Cancelar</Boton>
          <Boton type="submit" disabled={enviando}>{enviando ? 'Guardando…' : 'Guardar'}</Boton>
        </div>
      </form>
    </Modal>
  )
}
