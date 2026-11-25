import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api, ErrorApi } from '../../api/cliente.js'
import { Aviso, Boton, Campo, TarjetaFormulario } from '../../componentes/ui.jsx'
import { useFormulario } from '../../componentes/useFormulario.js'

/** CU-02, paso 2: definir la nueva contraseña desde el enlace recibido. */
export default function Restablecer() {
  const [parametros] = useSearchParams()
  const token = parametros.get('token') ?? ''
  const [listo, setListo] = useState(null)
  const { mensajeError, enviando, alEnviar, campo } = useFormulario({ nuevaContrasena: '', confirmacion: '' }, async (v) => {
    if (v.nuevaContrasena !== v.confirmacion) {
      throw new ErrorApi(400, 'Las contraseñas no coinciden.', { confirmacion: 'Debe ser igual a la nueva contraseña.' })
    }
    const r = await api.post('/auth/restablecer', { token, nuevaContrasena: v.nuevaContrasena })
    setListo(r.mensaje)
  })

  return (
    <TarjetaFormulario titulo="Definir nueva contraseña">
      {!token ? (
        <Aviso tipo="error">El enlace no es válido. <Link to="/recuperar" className="underline">Solicita uno nuevo</Link>.</Aviso>
      ) : listo ? (
        <div className="space-y-4">
          <Aviso tipo="exito">{listo}</Aviso>
          <Link to="/"><Boton className="w-full">Ir a ingresar</Boton></Link>
        </div>
      ) : (
        <form onSubmit={alEnviar} noValidate className="space-y-4">
          {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
          <Campo etiqueta="Nueva contraseña" type="password" autoComplete="new-password"
                 placeholder="Mínimo 8 caracteres, con letras y números" {...campo('nuevaContrasena')} />
          <Campo etiqueta="Confirma la contraseña" type="password" autoComplete="new-password" {...campo('confirmacion')} />
          <Boton type="submit" className="w-full" disabled={enviando}>
            {enviando ? 'Guardando…' : 'Guardar contraseña'}
          </Boton>
        </form>
      )}
    </TarjetaFormulario>
  )
}
