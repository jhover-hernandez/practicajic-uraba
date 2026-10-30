import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext.jsx'

/**
 * RF-04 en el frontend: sin sesión lleva al ingreso; con otro rol muestra "acceso denegado".
 * La API aplica la misma regla por su cuenta (RNF-03), esto solo evita pantallas que no corresponden.
 */
export default function RutaProtegida({ roles }) {
  const { usuario, cargando } = useAuth()
  const ubicacion = useLocation()

  if (cargando) {
    return <div className="grid min-h-screen place-items-center text-slate-500">Cargando…</div>
  }
  if (!usuario) {
    return <Navigate to="/" replace state={{ desde: ubicacion.pathname }} />
  }
  if (roles && !roles.includes(usuario.rol)) {
    return <Navigate to="/acceso-denegado" replace />
  }
  return <Outlet />
}
