import { NOMBRE_NIVEL, NOMBRE_TIPO_PORTAFOLIO } from './formato.js'
import { BarraCoincidencia, Dato } from './ui.jsx'

/** Datos del candidato que ven la empresa y el comité: programa, competencias (las coincidentes en verde) y portafolio. */
export default function FichaPostulante({ p }) {
  return (
    <div className="space-y-3">
      <dl className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Dato etiqueta="Programa">{p.programa}</Dato>
        <Dato etiqueta="Semestre">{p.semestre}</Dato>
        <Dato etiqueta="Municipio">{p.municipio}</Dato>
        <Dato etiqueta="Disponibilidad">{p.disponibilidad}</Dato>
      </dl>
      <BarraCoincidencia coincidentes={p.coincidentes} total={p.totalCompetencias} />
      {p.competencias.length > 0 && (
        <ul className="flex flex-wrap gap-1.5">
          {p.competencias.map((c) => {
            const coincide = p.competenciasCoincidentes.includes(c.competenciaId)
            return (
              <li key={c.competenciaId}
                  className={`rounded-full px-2.5 py-0.5 text-xs ${coincide ? 'bg-marca-100 font-semibold text-marca-800' : 'bg-slate-100 text-slate-700'}`}>
                {c.nombre} · {NOMBRE_NIVEL[c.nivel]}
              </li>
            )
          })}
        </ul>
      )}
      {p.portafolio.length > 0 && (
        <div>
          <h3 className="text-xs font-medium uppercase tracking-wide text-slate-500">Portafolio</h3>
          <ul className="mt-1 space-y-1 text-sm">
            {p.portafolio.map((x) => (
              <li key={x.id}>
                <span className="text-slate-500">{NOMBRE_TIPO_PORTAFOLIO[x.tipo]}:</span>{' '}
                {x.url ? <a href={x.url} target="_blank" rel="noreferrer noopener" className="text-marca-700 hover:underline">{x.titulo}</a> : x.titulo}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
