import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api/cliente.js'
import { Aviso, Boton, Campo, TarjetaFormulario } from '../../componentes/ui.jsx'
import { useFormulario } from '../../componentes/useFormulario.js'

/** CU-02, paso 1: solicitar el enlace de recuperación por correo. */
export default function Recuperar() {
  const [mensaje, setMensaje] = useState(null)
  const { mensajeError, enviando, alEnviar, campo } = useFormulario({ correo: '' }, async (v) => {
    const r = await api.post('/auth/recuperar', v)
    setMensaje(r.mensaje)
  })

  return (
    <TarjetaFormulario titulo="Recuperar contraseña"
                       descripcion="Te enviaremos un enlace para definir una nueva contraseña.">
      {mensaje ? (
        <div className="space-y-4">
          <Aviso tipo="exito">{mensaje}</Aviso>
          <Link to="/" className="block text-center text-sm font-semibold text-marca-700 hover:underline">Volver al inicio</Link>
        </div>
      ) : (
        <form onSubmit={alEnviar} noValidate className="space-y-4">
          {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
          <Campo etiqueta="Correo electrónico" type="email" autoComplete="email" {...campo('correo')} />
          <Boton type="submit" className="w-full" disabled={enviando}>
            {enviando ? 'Enviando…' : 'Enviar enlace'}
          </Boton>
          <Link to="/" className="block text-center text-sm text-marca-700 hover:underline">Volver al inicio</Link>
        </form>
      )}
    </TarjetaFormulario>
  )
}
