import { useCallback, useEffect, useState } from 'react'
import { api } from '../../api/cliente.js'
import { useAuth } from '../../auth/AuthContext.jsx'
import { NOMBRE_ROL } from '../../auth/roles.js'
import { Aviso, Boton, Campo, Insignia } from '../../componentes/ui.jsx'
import { useFormulario } from '../../componentes/useFormulario.js'

const COLOR_ESTADO_PERFIL = {
  PENDIENTE: 'amarillo', VALIDADO: 'verde', VALIDADA: 'verde',
  RECHAZADO: 'rojo', RECHAZADA: 'rojo', INACTIVO: 'gris', INACTIVA: 'gris',
}

const ROLES_INTERNOS = ['ADMIN', 'COMITE']

/** RF-05: el administrador crea, edita, activa y desactiva usuarios. */
export default function Usuarios() {
  const { usuario: actual } = useAuth()
  const [filtros, setFiltros] = useState({ rol: '', activo: '', q: '' })
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState(null)
  const [error, setError] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [edicion, setEdicion] = useState(null) // null = cerrado, {} = nuevo, {id...} = editar

  const cargar = useCallback(async () => {
    const p = new URLSearchParams({ page: pagina, size: 20 })
    Object.entries(filtros).forEach(([k, v]) => v !== '' && p.set(k, v))
    try {
      setDatos(await api.get(`/admin/usuarios?${p}`))
      setError(null)
    } catch (e) {
      setError(e.message)
    }
  }, [filtros, pagina])

  useEffect(() => {
    const espera = setTimeout(cargar, 250)
    return () => clearTimeout(espera)
  }, [cargar])

  function filtrar(e) {
    setFiltros((f) => ({ ...f, [e.target.name]: e.target.value }))
    setPagina(0)
  }

  async function cambiarEstado(u) {
    const accion = u.activo ? 'desactivar' : 'activar'
    if (!window.confirm(`¿Seguro que quieres ${accion} la cuenta ${u.correo}?`)) return
    try {
      await api.patch(`/admin/usuarios/${u.id}/estado`, { activo: !u.activo })
      setAviso(`La cuenta ${u.correo} quedó ${u.activo ? 'inactiva' : 'activa'}.`)
      cargar()
    } catch (e) {
      setError(e.message)
    }
  }

  function guardado(mensaje) {
    setEdicion(null)
    setAviso(mensaje)
    cargar()
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Usuarios y roles</h1>
          <p className="text-sm text-slate-600">Crea cuentas del comité y administradores, y activa o desactiva usuarios.</p>
        </div>
        <Boton onClick={() => setEdicion({})}>+ Nuevo usuario</Boton>
      </div>

      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      {error && <Aviso tipo="error">{error}</Aviso>}

      <div className="grid gap-3 rounded-xl border border-slate-200 bg-white p-4 sm:grid-cols-3">
        <Campo etiqueta="Buscar" name="q" value={filtros.q} onChange={filtrar} placeholder="Correo o nombre" />
        <Campo etiqueta="Rol" como="select" name="rol" value={filtros.rol} onChange={filtrar}>
          <option value="">Todos</option>
          {Object.entries(NOMBRE_ROL).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
        </Campo>
        <Campo etiqueta="Estado" como="select" name="activo" value={filtros.activo} onChange={filtrar}>
          <option value="">Todos</option>
          <option value="true">Activos</option>
          <option value="false">Inactivos</option>
        </Campo>
      </div>

      <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
        <table className="w-full min-w-[720px] text-left text-sm">
          <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-3">Usuario</th>
              <th className="px-4 py-3">Rol</th>
              <th className="px-4 py-3">Validación</th>
              <th className="px-4 py-3">Cuenta</th>
              <th className="px-4 py-3 text-right">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {datos?.content.map((u) => (
              <tr key={u.id}>
                <td className="px-4 py-3">
                  <div className="font-medium text-slate-900">{u.nombre ?? '—'}</div>
                  <div className="text-xs text-slate-500">{u.correo}</div>
                </td>
                <td className="px-4 py-3">{NOMBRE_ROL[u.rol]}</td>
                <td className="px-4 py-3">
                  {u.estadoPerfil ? <Insignia color={COLOR_ESTADO_PERFIL[u.estadoPerfil]}>{u.estadoPerfil}</Insignia> : '—'}
                </td>
                <td className="px-4 py-3">
                  <Insignia color={u.activo ? 'verde' : 'rojo'}>{u.activo ? 'Activa' : 'Inactiva'}</Insignia>
                </td>
                <td className="space-x-2 whitespace-nowrap px-4 py-3 text-right">
                  <Boton variante="secundario" className="px-3 py-1.5" onClick={() => setEdicion(u)}>Editar</Boton>
                  {u.id !== actual.id && (
                    <Boton variante={u.activo ? 'peligro' : 'secundario'} className="px-3 py-1.5" onClick={() => cambiarEstado(u)}>
                      {u.activo ? 'Desactivar' : 'Activar'}
                    </Boton>
                  )}
                </td>
              </tr>
            ))}
            {datos?.content.length === 0 && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-500">No hay usuarios con esos filtros.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {datos && datos.totalPages > 1 && (
        <div className="flex items-center justify-end gap-3 text-sm">
          <Boton variante="secundario" disabled={datos.first} onClick={() => setPagina((p) => p - 1)}>Anterior</Boton>
          <span>Página {datos.number + 1} de {datos.totalPages}</span>
          <Boton variante="secundario" disabled={datos.last} onClick={() => setPagina((p) => p + 1)}>Siguiente</Boton>
        </div>
      )}

      {edicion && (
        <FormularioUsuario usuario={edicion} esActual={edicion.id === actual.id}
                           onCerrar={() => setEdicion(null)} onGuardado={guardado} />
      )}
    </div>
  )
}

function FormularioUsuario({ usuario, esActual, onCerrar, onGuardado }) {
  const nuevo = !usuario.id
  const rolEditable = nuevo || (ROLES_INTERNOS.includes(usuario.rol) && !esActual)
  const { mensajeError, enviando, alEnviar, campo } = useFormulario(
    { correo: usuario.correo ?? '', nombre: usuario.nombre ?? '', rol: usuario.rol ?? 'COMITE', contrasena: '' },
    async (v) => {
      if (nuevo) {
        await api.post('/admin/usuarios', v)
        onGuardado(`Se creó la cuenta ${v.correo}.`)
      } else {
        await api.put(`/admin/usuarios/${usuario.id}`, { correo: v.correo, nombre: v.nombre, rol: v.rol })
        onGuardado(`Se actualizó la cuenta ${v.correo}.`)
      }
    },
  )

  return (
    <div className="fixed inset-0 z-20 grid place-items-center bg-slate-900/40 p-4" role="dialog" aria-modal="true">
      <form onSubmit={alEnviar} noValidate className="w-full max-w-md space-y-4 rounded-2xl bg-white p-6 shadow-xl">
        <h2 className="text-lg font-bold text-slate-900">{nuevo ? 'Nuevo usuario' : 'Editar usuario'}</h2>
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        <Campo etiqueta="Nombre" {...campo('nombre')} />
        <Campo etiqueta="Correo" type="email" {...campo('correo')} />
        {rolEditable ? (
          <Campo etiqueta="Rol" como="select" {...campo('rol')}>
            {ROLES_INTERNOS.map((r) => <option key={r} value={r}>{NOMBRE_ROL[r]}</option>)}
          </Campo>
        ) : (
          <p className="text-sm text-slate-600">Rol: <strong>{NOMBRE_ROL[usuario.rol]}</strong></p>
        )}
        {nuevo && (
          <Campo etiqueta="Contraseña temporal" type="password" autoComplete="new-password"
                 placeholder="Mínimo 8 caracteres, con letras y números" {...campo('contrasena')} />
        )}
        <div className="flex justify-end gap-2 pt-2">
          <Boton type="button" variante="secundario" onClick={onCerrar}>Cancelar</Boton>
          <Boton type="submit" disabled={enviando}>{enviando ? 'Guardando…' : 'Guardar'}</Boton>
        </div>
      </form>
    </div>
  )
}
