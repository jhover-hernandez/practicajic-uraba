import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api/cliente.js'
import { Aviso, Boton, Campo, Encabezado, Tarjeta } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

const INFORMES = [
  ['empresas', 'Empresas', 'NIT, razón social, sector, municipio y estado de validación.'],
  ['estudiantes', 'Estudiantes', 'Datos académicos y estado de validación.'],
  ['convocatorias', 'Convocatorias', 'Ofertas con área, modalidad, vacantes, estado y número de postulados.'],
  ['postulaciones', 'Postulaciones', 'Cada postulación con su estado y competencias coincidentes.'],
  ['practicas', 'Prácticas', 'Vinculación, tutores, seguimientos y calificación final.'],
]

/** RF-40: informes en Excel. El PDF de indicadores se genera desde el Panel con "Exportar PDF". */
export default function Informes() {
  const { datos: panel } = useCarga(() => api.get('/admin/panel'))
  const [periodo, setPeriodo] = useState('')
  const [error, setError] = useState(null)
  const [descargando, setDescargando] = useState(null)

  async function descargar(tipo) {
    setError(null)
    setDescargando(tipo)
    try {
      await api.descargar(`/admin/informes/${tipo}${periodo ? `?periodo=${periodo}` : ''}`,
        `informe-${tipo}${periodo ? `-${periodo}` : ''}.csv`)
    } catch (e) {
      setError(e.message)
    } finally {
      setDescargando(null)
    }
  }

  return (
    <div className="max-w-4xl space-y-5">
      <Encabezado titulo="Informes" descripcion="Descarga los datos en un archivo que se abre directamente en Excel." />
      <div className="max-w-xs">
        <Campo etiqueta="Periodo" como="select" value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
          <option value="">Todo el histórico</option>
          {panel?.periodos.map((p) => <option key={p} value={p}>Semestre {p}</option>)}
        </Campo>
      </div>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <div className="grid gap-3 sm:grid-cols-2">
        {INFORMES.map(([tipo, titulo, texto]) => (
          <Tarjeta key={tipo} titulo={titulo}>
            <p className="text-sm text-slate-600">{texto}</p>
            <Boton className="mt-3" disabled={descargando === tipo} onClick={() => descargar(tipo)}>
              {descargando === tipo ? 'Descargando…' : 'Descargar Excel'}
            </Boton>
          </Tarjeta>
        ))}
        <Tarjeta titulo="Indicadores en PDF">
          <p className="text-sm text-slate-600">Abre el panel, elige el periodo y usa "Exportar PDF" para imprimirlo o guardarlo.</p>
          <Link to="/admin" className="mt-3 inline-flex rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">
            Ir al panel
          </Link>
        </Tarjeta>
      </div>
    </div>
  )
}
