import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { iniciales, MENU_ROL, NOMBRE_ROL, RUTA_ROL } from '../auth/roles.js'
import { Logo } from './ui.jsx'

/** Estructura común de los paneles: barra superior y menú lateral según el rol (prototipo, pantallas 2 a 5). */
export default function PanelLayout({ rol }) {
  const { usuario, cerrarSesion } = useAuth()
  const navegar = useNavigate()
  const base = RUTA_ROL[rol]

  async function salir() {
    await cerrarSesion()
    navegar('/', { replace: true })
  }

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white">
        <div className="flex items-center justify-between gap-3 px-4 py-3">
          <Logo subtitulo={NOMBRE_ROL[rol]} />
          <div className="flex items-center gap-3">
            <span className="hidden text-right text-sm sm:block">
              <span className="block font-medium text-slate-800">{usuario.nombre ?? usuario.correo}</span>
              <span className="block text-xs text-slate-500">{usuario.correo}</span>
            </span>
            <span className="grid h-9 w-9 place-items-center rounded-full bg-marca-100 text-sm font-bold text-marca-800"
                  aria-hidden="true">
              {iniciales(usuario.nombre ?? usuario.correo)}
            </span>
            <button onClick={salir} className="rounded-lg px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100">
              Cerrar sesión
            </button>
          </div>
        </div>
      </header>

      <div className="md:flex">
        <nav aria-label="Menú del panel"
             className="flex gap-1 overflow-x-auto border-b border-slate-200 bg-white px-2 py-2 md:min-h-[calc(100vh-61px)] md:w-60 md:shrink-0 md:flex-col md:border-r md:border-b-0 md:px-3 md:py-4">
          {MENU_ROL[rol].map((item) => (
            <NavLink key={item.ruta} to={item.ruta ? `${base}/${item.ruta}` : base} end
                     className={({ isActive }) =>
                       `whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium ${
                         isActive ? 'bg-marca-50 text-marca-800' : 'text-slate-600 hover:bg-slate-100'
                       }`}>
              {item.texto}
            </NavLink>
          ))}
        </nav>
        <main className="min-w-0 flex-1 px-4 py-6 md:px-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
