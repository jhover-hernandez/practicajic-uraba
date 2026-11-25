/** Criterios y pesos de las dos matrices de 100 puntos de la propuesta. */
export const MATRIZ_OFERTA = [
  ['pertinencia', 'Pertinencia de las actividades', 25],
  ['calidadPlan', 'Calidad del plan de práctica', 20],
  ['condiciones', 'Condiciones ofrecidas', 20],
  ['incentivoEconomico', 'Incentivo económico', 15],
  ['acompanamiento', 'Capacidad de acompañamiento', 10],
  ['experienciaLaboral', 'Generación de experiencia laboral', 10],
]

export const MATRIZ_CANDIDATO = [
  ['tecnico', 'Competencias técnicas', 30],
  ['perfil', 'Ajuste al perfil de la oferta', 25],
  ['academico', 'Desempeño académico', 15],
  ['portafolio', 'Portafolio', 15],
  ['blandas', 'Competencias blandas', 10],
  ['entrevista', 'Entrevista', 5],
]

export function totalMatriz(criterios, valores) {
  return criterios.reduce((suma, [clave]) => suma + (Number(valores[clave]) || 0), 0)
}

/** Tabla de calificación con el total sobre 100 (pantalla 4 del prototipo). */
export default function Matriz({ criterios, valores, errores = {}, onCambio, minimo }) {
  const total = totalMatriz(criterios, valores)
  return (
    <div className="overflow-hidden rounded-xl border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-xs uppercase text-slate-500">
          <tr>
            <th className="px-3 py-2 text-left">Criterio</th>
            <th className="px-3 py-2 text-right">Peso</th>
            <th className="w-28 px-3 py-2 text-right">Puntaje</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {criterios.map(([clave, etiqueta, maximo]) => (
            <tr key={clave}>
              <td className="px-3 py-2">
                <label htmlFor={`m-${clave}`}>{etiqueta}</label>
                {errores[clave] && <span className="block text-xs text-red-600">{errores[clave]}</span>}
              </td>
              <td className="px-3 py-2 text-right text-slate-500">{maximo}</td>
              <td className="px-3 py-2">
                <input id={`m-${clave}`} type="number" min="0" max={maximo} step="0.5" value={valores[clave] ?? ''}
                       onChange={(e) => onCambio(clave, e.target.value)}
                       className={`w-full rounded-lg border px-2 py-1 text-right ${Number(valores[clave]) > maximo ? 'border-red-400' : 'border-slate-300'}`} />
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot className="bg-slate-50 font-semibold">
          <tr>
            <td className="px-3 py-2">Total</td>
            <td className="px-3 py-2 text-right text-slate-500">100</td>
            <td className={`px-3 py-2 text-right text-lg ${minimo !== undefined ? (total >= minimo ? 'text-marca-700' : 'text-red-600') : ''}`}>
              {total.toFixed(1)}
            </td>
          </tr>
        </tfoot>
      </table>
      {minimo !== undefined && (
        <p className="border-t border-slate-200 px-3 py-2 text-xs text-slate-600">
          Puntaje mínimo de aprobación definido por el Politécnico: <strong>{minimo}</strong>
        </p>
      )}
    </div>
  )
}
