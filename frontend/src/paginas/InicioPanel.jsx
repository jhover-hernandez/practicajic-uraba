import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { NOMBRE_ROL, RUTA_ROL } from '../auth/roles.js'
import { Aviso } from '../componentes/ui.jsx'

const AVISO_ESTADO = {
  PENDIENTE: ['alerta', 'Tu cuenta está pendiente de validación por el Politécnico. Mientras tanto puedes completar tu perfil.'],
  VALIDADO: ['exito', 'Tu cuenta fue validada por el Politécnico.'],
  VALIDADA: ['exito', 'Empresa validada por el Politécnico. Ya puedes publicar necesidades de práctica.'],
  RECHAZADO: ['error', 'Tu registro fue rechazado. Revisa el motivo en Mi perfil digital, corrige tus datos y se revisarán de nuevo.'],
  RECHAZADA: ['error', 'El registro de la empresa fue rechazado. Revisa el motivo en Perfil de la empresa y corrige los datos.'],
  INACTIVO: ['error', 'Tu perfil está inactivo. Comunícate con el Politécnico.'],
  INACTIVA: ['error', 'El perfil de la empresa está inactivo. Comunícate con el Politécnico.'],
}

// Accesos rápidos de cada rol, en el orden del recorrido del prototipo.
const ATAJOS = {
  ESTUDIANTE: [
    ['perfil', 'Completa tu perfil digital', 'Carga tu hoja de vida en PDF, registra tus competencias y tu portafolio.'],
    ['convocatorias', 'Busca convocatorias', 'Encuentra ofertas aprobadas por el comité y mira cuántas competencias coinciden.'],
    ['postulaciones', 'Sigue tus postulaciones', 'Consulta el estado de cada proceso y acepta la práctica cuando te seleccionen.'],
  ],
  EMPRESA: [
    ['perfil', 'Completa el perfil', 'Sector, tamaño, municipio y contacto de la empresa.'],
    ['ofertas', 'Publica una necesidad', 'Describe la práctica; el comité la evalúa antes de publicarla.'],
    ['postulantes', 'Revisa a los postulantes', 'Ordenados por competencias coincidentes, con su hoja de vida.'],
  ],
  COMITE: [
    ['ofertas', 'Evalúa ofertas', 'Califica cada oferta con la matriz de 100 puntos y decide.'],
    ['candidatos', 'Califica candidatos', 'Usa la matriz del estudiante, emite concepto y recomienda.'],
    ['actas', 'Genera el acta', 'Resume las decisiones de un periodo para imprimir o guardar en PDF.'],
  ],
}

export default function InicioPanel() {
  const { usuario, refrescarUsuario } = useAuth()
  const aviso = AVISO_ESTADO[usuario.estadoPerfil]

  useEffect(() => {
    refrescarUsuario().catch(() => {})
  }, [refrescarUsuario])

  return (
    <div className="max-w-4xl space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Hola, {usuario.nombre ?? usuario.correo}</h1>
        <p className="text-slate-600">{NOMBRE_ROL[usuario.rol]}</p>
      </div>
      {aviso && <Aviso tipo={aviso[0]}>{aviso[1]}</Aviso>}
      <div className="grid gap-3 sm:grid-cols-3">
        {(ATAJOS[usuario.rol] ?? []).map(([ruta, titulo, texto]) => (
          <Link key={ruta} to={`${RUTA_ROL[usuario.rol]}/${ruta}`}
                className="rounded-xl border border-slate-200 bg-white p-4 transition hover:border-marca-500 hover:shadow-sm">
            <span className="block font-semibold text-marca-700">{titulo} →</span>
            <span className="mt-1 block text-sm text-slate-600">{texto}</span>
          </Link>
        ))}
      </div>
    </div>
  )
}
