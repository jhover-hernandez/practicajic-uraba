import { Link } from 'react-router-dom'

/** "Estado del perfil" de la pantalla 2: lo que falta para poder postularse (RN-05). */
export default function EstadoPerfil({ perfil, conEnlace = false }) {
  const pasos = [
    ['Validado por el Politécnico', perfil.estado === 'VALIDADO', true],
    ['Hoja de vida cargada', !!perfil.hojaVida, true],
    ['Competencias registradas', perfil.competencias.length > 0, false],
    ['Portafolio', perfil.portafolio.length > 0, false],
  ]
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="font-semibold text-slate-900">Estado del perfil</h2>
        {perfil.puedePostularse
          ? <span className="text-sm font-medium text-marca-700">Ya puedes postularte</span>
          : <span className="text-sm font-medium text-amber-700">Aún no puedes postularte</span>}
      </div>
      <ul className="mt-3 grid gap-2 sm:grid-cols-4">
        {pasos.map(([texto, listo, obligatorio]) => (
          <li key={texto} className={`rounded-lg px-3 py-2 text-sm ${listo ? 'bg-marca-50 text-marca-800' : 'bg-slate-50 text-slate-600'}`}>
            <span aria-hidden="true">{listo ? '✓ ' : '○ '}</span>{texto}
            {!listo && obligatorio && <span className="block text-xs text-amber-700">Obligatorio para postularte</span>}
          </li>
        ))}
      </ul>
      {conEnlace && !perfil.puedePostularse && (
        <Link to="/estudiante/perfil" className="mt-3 inline-block text-sm font-semibold text-marca-700 hover:underline">
          Completar mi perfil digital →
        </Link>
      )}
    </div>
  )
}
