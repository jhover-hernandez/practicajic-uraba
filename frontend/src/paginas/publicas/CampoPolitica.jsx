import { Link } from 'react-router-dom'

/** RNF-06: aceptación explícita de la política de tratamiento de datos (Ley 1581 de 2012). */
export default function CampoPolitica({ marcado, onChange, error }) {
  return (
    <div className="text-sm">
      <label className="flex items-start gap-2">
        <input type="checkbox" name="aceptaPoliticaDatos" checked={marcado} onChange={onChange}
               className="mt-1 h-4 w-4 accent-marca-600" />
        <span className="text-slate-700">
          He leído y acepto la{' '}
          <Link to="/politica-datos" target="_blank" className="font-semibold text-marca-700 hover:underline">
            política de tratamiento de datos personales
          </Link>{' '}
          del Politécnico (Ley 1581 de 2012).
        </span>
      </label>
      {error && <span className="mt-1 block text-xs text-red-600">{error}</span>}
    </div>
  )
}
