import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api/cliente.js'
import { MUNICIPIOS_URABA } from '../../auth/roles.js'
import { Aviso, Boton, Campo, TarjetaFormulario } from '../../componentes/ui.jsx'
import { useFormulario } from '../../componentes/useFormulario.js'
import CampoPolitica from './CampoPolitica.jsx'
import RegistroExitoso from './RegistroExitoso.jsx'

const INICIAL = {
  razonSocial: '', nombreComercial: '', nit: '', municipio: '', contacto: '',
  correo: '', contrasena: '', aceptaPoliticaDatos: false,
}

/** RF-01 y CU-03 para empresas. Sector, tamaño y demás datos del perfil se completan en el módulo de empresas (Tarea 10). */
export default function RegistroEmpresa() {
  const [registrado, setRegistrado] = useState(null)
  const { valores, errores, mensajeError, enviando, cambiar, alEnviar, campo } = useFormulario(INICIAL, async (v) => {
    const usuario = await api.post('/auth/registro/empresa', v)
    setRegistrado(usuario.correo)
  })

  if (registrado) return <RegistroExitoso correo={registrado} />

  return (
    <TarjetaFormulario titulo="Registrar mi empresa"
                       descripcion="El Politécnico valida cada empresa antes de que pueda publicar necesidades de práctica.">
      <form onSubmit={alEnviar} noValidate className="space-y-4">
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        <Campo etiqueta="Razón social" autoComplete="organization" {...campo('razonSocial')} />
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo etiqueta="Nombre comercial (opcional)" {...campo('nombreComercial')} />
          <Campo etiqueta="NIT" placeholder="900123456-7" {...campo('nit')} />
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo etiqueta="Municipio" como="select" {...campo('municipio')}>
            <option value="">Selecciona…</option>
            {MUNICIPIOS_URABA.map((m) => <option key={m}>{m}</option>)}
          </Campo>
          <Campo etiqueta="Persona de contacto (opcional)" autoComplete="name" {...campo('contacto')} />
        </div>
        <Campo etiqueta="Correo de la empresa" type="email" autoComplete="email" {...campo('correo')} />
        <Campo etiqueta="Contraseña" type="password" autoComplete="new-password"
               placeholder="Mínimo 8 caracteres, con letras y números" {...campo('contrasena')} />
        <CampoPolitica marcado={valores.aceptaPoliticaDatos} onChange={cambiar} error={errores.aceptaPoliticaDatos} />
        <Boton type="submit" className="w-full" disabled={enviando}>
          {enviando ? 'Registrando…' : 'Registrar empresa'}
        </Boton>
        <p className="text-center text-sm text-slate-600">
          ¿Ya tienes cuenta? <Link to="/" className="font-semibold text-marca-700 hover:underline">Ingresar</Link>
        </p>
      </form>
    </TarjetaFormulario>
  )
}
