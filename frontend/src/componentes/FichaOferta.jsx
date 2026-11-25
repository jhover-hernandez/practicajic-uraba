import { dinero, fecha, NOMBRE_MODALIDAD, NOMBRE_NIVEL } from './formato.js'
import { Dato } from './ui.jsx'

/**
 * Detalle de una oferta: lo usan el estudiante (RF-18), la empresa y el comité (pantalla 4).
 * `coincidentes` resalta en verde las competencias que el estudiante ya tiene (RF-25).
 */
export default function FichaOferta({ oferta, coincidentes }) {
  return (
    <div className="space-y-4">
      <dl className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Dato etiqueta="Área">{oferta.area}</Dato>
        <Dato etiqueta="Modalidad">{NOMBRE_MODALIDAD[oferta.modalidad]}</Dato>
        <Dato etiqueta="Municipio">{oferta.municipio}</Dato>
        <Dato etiqueta="Duración">{oferta.duracionMeses} meses</Dato>
        <Dato etiqueta="Vacantes">{oferta.vacantes}</Dato>
        <Dato etiqueta="Horario">{oferta.horario}</Dato>
        <Dato etiqueta="Vinculación">{oferta.tipoVinculacion}</Dato>
        <Dato etiqueta="Incentivo">{dinero(oferta.incentivo)}</Dato>
        <Dato etiqueta="Cierre">{fecha(oferta.fechaCierre)}</Dato>
        {oferta.fechaPublicacion && <Dato etiqueta="Publicada">{fecha(oferta.fechaPublicacion)}</Dato>}
      </dl>
      <Texto titulo="Descripción" texto={oferta.descripcion} />
      {oferta.perfil && <Texto titulo="Perfil buscado" texto={oferta.perfil} />}
      <Texto titulo="Actividades" texto={oferta.actividades} />
      <div>
        <h3 className="text-xs font-medium uppercase tracking-wide text-slate-500">Competencias pedidas</h3>
        <ul className="mt-1 flex flex-wrap gap-2">
          {oferta.competencias.map((c) => {
            const tiene = coincidentes?.includes(c.competenciaId)
            return (
              <li key={c.competenciaId}
                  className={`rounded-full px-3 py-1 text-xs font-medium ${tiene ? 'bg-marca-100 text-marca-800' : 'bg-slate-100 text-slate-700'}`}>
                {tiene && <span aria-label="La tienes">✓ </span>}{c.nombre} · {NOMBRE_NIVEL[c.nivelRequerido]}
              </li>
            )
          })}
        </ul>
      </div>
    </div>
  )
}

function Texto({ titulo, texto }) {
  return (
    <div>
      <h3 className="text-xs font-medium uppercase tracking-wide text-slate-500">{titulo}</h3>
      <p className="mt-1 whitespace-pre-line text-sm text-slate-800">{texto}</p>
    </div>
  )
}
