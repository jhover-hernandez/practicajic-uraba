import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api/cliente.js'
import { MUNICIPIOS_URABA } from '../../auth/roles.js'
import { Aviso, Boton, Campo, TarjetaFormulario } from '../../componentes/ui.jsx'
import { useFormulario } from '../../componentes/useFormulario.js'
import CampoPolitica from './CampoPolitica.jsx'
import RegistroExitoso from './RegistroExitoso.jsx'

const INICIAL = {
  nombres: '', apellidos: '', documento: '', correo: '', contrasena: '',
  programa: 'Tecnología en Desarrollo de Software', semestre: '', municipio: '', aceptaPoliticaDatos: false,
}

/** RF-01 para estudiantes. El perfil digital completo se diligencia después (Tarea 11). */
export default function RegistroEstudiante() {
  const [registrado, setRegistrado] = useState(null)
  const { valores, errores, mensajeError, enviando, cambiar, alEnviar, campo } = useFormulario(INICIAL, async (v) => {
    const usuario = await api.post('/auth/registro/estudiante', { ...v, semestre: v.semestre ? Number(v.semestre) : null })
    setRegistrado(usuario.correo)
  })

  if (registrado) return <RegistroExitoso correo={registrado} />

  return (
    <TarjetaFormulario titulo="Crear cuenta de estudiante"
                       descripcion="Usa tu correo institucional. El Politécnico validará tus datos antes de que puedas postularte.">
      <form onSubmit={alEnviar} noValidate className="space-y-4">
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo etiqueta="Nombres" autoComplete="given-name" {...campo('nombres')} />
          <Campo etiqueta="Apellidos" autoComplete="family-name" {...campo('apellidos')} />
        </div>
        <Campo etiqueta="Número de documento" inputMode="numeric" {...campo('documento')} />
        <Campo etiqueta="Correo institucional" type="email" autoComplete="email" {...campo('correo')} />
        <Campo etiqueta="Contraseña" type="password" autoComplete="new-password"
               placeholder="Mínimo 8 caracteres, con letras y números" {...campo('contrasena')} />
        <Campo etiqueta="Programa académico" {...campo('programa')} />
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo etiqueta="Semestre" como="select" {...campo('semestre')}>
            <option value="">Selecciona…</option>
            {Array.from({ length: 10 }, (_, i) => i + 1).map((s) => <option key={s} value={s}>{s}.º semestre</option>)}
          </Campo>
          <Campo etiqueta="Municipio" como="select" {...campo('municipio')}>
            <option value="">Selecciona…</option>
            {MUNICIPIOS_URABA.map((m) => <option key={m}>{m}</option>)}
          </Campo>
        </div>
        <CampoPolitica marcado={valores.aceptaPoliticaDatos} onChange={cambiar} error={errores.aceptaPoliticaDatos} />
        <Boton type="submit" className="w-full" disabled={enviando}>
          {enviando ? 'Creando cuenta…' : 'Crear cuenta'}
        </Boton>
        <p className="text-center text-sm text-slate-600">
          ¿Ya tienes cuenta? <Link to="/" className="font-semibold text-marca-700 hover:underline">Ingresar</Link>
        </p>
      </form>
    </TarjetaFormulario>
  )
}
