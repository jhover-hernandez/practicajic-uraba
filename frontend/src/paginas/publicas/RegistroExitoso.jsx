import { Link } from 'react-router-dom'
import { Aviso, Boton, TarjetaFormulario } from '../../componentes/ui.jsx'

export default function RegistroExitoso({ correo }) {
  return (
    <TarjetaFormulario titulo="¡Cuenta creada!">
      <div className="space-y-4">
        <Aviso tipo="exito">
          Registramos la cuenta <strong>{correo}</strong>. Queda en estado <strong>Pendiente</strong> hasta que el
          Politécnico valide tus datos; mientras tanto ya puedes ingresar y revisar tu panel.
        </Aviso>
        <Link to="/"><Boton className="w-full">Ir a ingresar</Boton></Link>
      </div>
    </TarjetaFormulario>
  )
}
