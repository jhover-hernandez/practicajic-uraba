import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext.jsx'
import { RUTA_ROL } from '../../auth/roles.js'
import { Aviso, Boton, Campo, Logo } from '../../componentes/ui.jsx'

const PASOS = [
  ['Regístrate', 'Crea tu cuenta y el Politécnico valida tus datos.'],
  ['Publica o postúlate', 'La empresa publica su necesidad; el estudiante arma su perfil digital.'],
  ['El comité evalúa', 'Ofertas y candidatos se califican con matrices de 100 puntos.'],
  ['Inicia tu práctica', 'Se formaliza la vinculación y el Politécnico hace el seguimiento.'],
]

/** Pantalla 1 del prototipo: inicio público con el formulario de ingreso (CU-01). */
export default function Inicio() {
  const { usuario, cargando, iniciarSesion } = useAuth()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [correo, setCorreo] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (!cargando && usuario) {
    return <Navigate to={RUTA_ROL[usuario.rol]} replace />
  }

  async function ingresar(e) {
    e.preventDefault()
    setError(null)
    setEnviando(true)
    try {
      const u = await iniciarSesion(correo, contrasena)
      const destino = ubicacion.state?.desde?.startsWith(RUTA_ROL[u.rol]) ? ubicacion.state.desde : RUTA_ROL[u.rol]
      navegar(destino, { replace: true })
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
          <Logo />
          <nav className="hidden gap-6 text-sm text-slate-600 md:flex">
            <a href="#como-funciona" className="hover:text-marca-700">Cómo funciona</a>
            <a href="#empresas" className="hover:text-marca-700">Para empresas</a>
            <a href="#ingresar" className="font-semibold text-marca-700">Ingresar</a>
          </nav>
        </div>
      </header>

      <main className="flex-1">
        <section className="mx-auto grid max-w-6xl gap-10 px-4 py-12 md:grid-cols-[1fr_400px] md:py-16">
          <div className="flex flex-col justify-center">
            <p className="text-sm font-semibold text-marca-700">
              Politécnico Colombiano Jaime Isaza Cadavid · Regional Urabá · Piloto 2026
            </p>
            <h1 className="mt-3 text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">
              Las prácticas profesionales de Urabá, en un solo lugar
            </h1>
            <p className="mt-4 max-w-xl text-lg text-slate-600">
              Las empresas publican sus necesidades, los estudiantes del Politécnico se postulan con su perfil digital y
              el comité valida cada oferta antes de que llegue a ti.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <Link to="/registro/estudiante"><Boton>Soy estudiante</Boton></Link>
              <Link to="/registro/empresa"><Boton variante="secundario">Soy empresa</Boton></Link>
            </div>
          </div>

          <form id="ingresar" onSubmit={ingresar} noValidate
                className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900">Ingresar a la plataforma</h2>
            <div className="mt-4 space-y-4">
              {error && <Aviso tipo="error">{error}</Aviso>}
              <Campo etiqueta="Correo electrónico" type="email" autoComplete="email" required
                     value={correo} onChange={(e) => setCorreo(e.target.value)} />
              <Campo etiqueta="Contraseña" type="password" autoComplete="current-password" required
                     value={contrasena} onChange={(e) => setContrasena(e.target.value)} />
              <div className="text-right">
                <Link to="/recuperar" className="text-sm text-marca-700 hover:underline">¿Olvidaste tu contraseña?</Link>
              </div>
              <Boton type="submit" className="w-full" disabled={enviando || !correo || !contrasena}>
                {enviando ? 'Ingresando…' : 'Ingresar'}
              </Boton>
              <p className="text-center text-sm text-slate-600">
                ¿No tienes cuenta? Crear cuenta de{' '}
                <Link to="/registro/estudiante" className="font-semibold text-marca-700 hover:underline">estudiante</Link>
                {' '}o{' '}
                <Link to="/registro/empresa" className="font-semibold text-marca-700 hover:underline">empresa</Link>
              </p>
            </div>
          </form>
        </section>

        <section id="como-funciona" className="border-y border-slate-200 bg-white">
          <div className="mx-auto max-w-6xl px-4 py-12">
            <h2 className="text-2xl font-bold text-slate-900">Cómo funciona</h2>
            <ol className="mt-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {PASOS.map(([titulo, texto], i) => (
                <li key={titulo}>
                  <span className="grid h-8 w-8 place-items-center rounded-full bg-marca-100 font-bold text-marca-700">{i + 1}</span>
                  <h3 className="mt-3 font-semibold text-slate-900">{titulo}</h3>
                  <p className="mt-1 text-sm text-slate-600">{texto}</p>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section id="empresas" className="mx-auto flex max-w-6xl flex-col items-start justify-between gap-4 px-4 py-12 md:flex-row md:items-center">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">¿Tu empresa necesita apoyo tecnológico?</h2>
            <p className="mt-2 max-w-2xl text-slate-600">
              Publica tu necesidad en software, páginas web, bases de datos o soporte, y recibe estudiantes con las
              competencias que buscas.
            </p>
          </div>
          <Link to="/registro/empresa"><Boton>Registrar mi empresa</Boton></Link>
        </section>
      </main>

      <footer className="border-t border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl flex-col gap-2 px-4 py-6 text-sm text-slate-500 sm:flex-row sm:justify-between">
          <span>Politécnico Colombiano Jaime Isaza Cadavid · Centro Regional Urabá, Apartadó</span>
          <Link to="/politica-datos" className="hover:text-marca-700">Política de tratamiento de datos (Ley 1581 de 2012)</Link>
        </div>
      </footer>
    </div>
  )
}
