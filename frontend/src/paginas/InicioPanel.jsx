import { useAuth } from '../auth/AuthContext.jsx'
import { NOMBRE_ROL } from '../auth/roles.js'
import { Aviso } from '../componentes/ui.jsx'

const AVISO_ESTADO = {
  PENDIENTE: ['alerta', 'Tu cuenta está pendiente de validación por el Politécnico. Te avisaremos cuando quede validada.'],
  VALIDADO: ['exito', 'Tu cuenta fue validada por el Politécnico.'],
  VALIDADA: ['exito', 'Empresa validada por el Politécnico. Ya puedes publicar necesidades de práctica.'],
  RECHAZADO: ['error', 'Tu registro fue rechazado. Comunícate con el Politécnico para conocer el motivo.'],
  RECHAZADA: ['error', 'El registro de la empresa fue rechazado. Comunícate con el Politécnico para conocer el motivo.'],
  INACTIVO: ['error', 'Tu perfil está inactivo.'],
  INACTIVA: ['error', 'El perfil de la empresa está inactivo.'],
}

export default function InicioPanel() {
  const { usuario } = useAuth()
  const aviso = AVISO_ESTADO[usuario.estadoPerfil]

  return (
    <div className="max-w-3xl space-y-4">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Hola, {usuario.nombre ?? usuario.correo}</h1>
        <p className="text-slate-600">{NOMBRE_ROL[usuario.rol]}</p>
      </div>
      {aviso && <Aviso tipo={aviso[0]}>{aviso[1]}</Aviso>}
      <p className="text-sm text-slate-600">Usa el menú para moverte por las secciones de tu panel.</p>
    </div>
  )
}
