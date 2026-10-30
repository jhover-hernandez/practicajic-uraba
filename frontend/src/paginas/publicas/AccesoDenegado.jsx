import { Link } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext.jsx'
import { RUTA_ROL } from '../../auth/roles.js'
import { Boton, TarjetaFormulario } from '../../componentes/ui.jsx'

export default function AccesoDenegado() {
  const { usuario } = useAuth()
  return (
    <TarjetaFormulario titulo="Acceso denegado"
                       descripcion="Tu rol no tiene permiso para entrar a esta sección.">
      <Link to={usuario ? RUTA_ROL[usuario.rol] : '/'}>
        <Boton className="w-full">{usuario ? 'Volver a mi panel' : 'Ir al inicio'}</Boton>
      </Link>
    </TarjetaFormulario>
  )
}
