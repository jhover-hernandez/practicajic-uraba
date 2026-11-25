import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api/cliente.js'
import { ESTADO, fecha } from '../../componentes/formato.js'
import { Boton, Campo, Carga, Encabezado, Tarjeta } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

/** Pantalla 5 del prototipo · RF-38 a RF-40 · CU-12 Consultar indicadores. */
export default function Panel() {
  const [periodo, setPeriodo] = useState('')
  const { datos: d, error, cargando } = useCarga(() => api.get(`/admin/panel${periodo ? `?periodo=${periodo}` : ''}`), [periodo])

  return (
    <div className="space-y-5">
      <Encabezado titulo="Panel administrativo"
                  descripcion={d?.periodo ? `Semestre ${d.periodo}: ${fecha(d.desde)} a ${fecha(d.hasta)}` : 'Todo el histórico del piloto'}>
        <Boton variante="secundario" onClick={() => window.print()}>Exportar PDF</Boton>
        <Link to="/admin/informes" className="inline-flex items-center rounded-lg bg-marca-600 px-4 py-2 text-sm font-semibold text-white hover:bg-marca-700">
          Exportar Excel
        </Link>
      </Encabezado>
      <div className="max-w-xs print:hidden">
        <Campo etiqueta="Periodo" como="select" value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
          <option value="">Todo el histórico</option>
          {d?.periodos.map((p) => <option key={p} value={p}>Semestre {p}</option>)}
        </Campo>
      </div>

      <Carga cargando={cargando && !d} error={error}>
        {d && (
          <>
            <div className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
              <Indicador titulo="Empresas validadas" valor={d.empresasValidadas} />
              <Indicador titulo="Estudiantes validados" valor={d.estudiantesValidados} />
              <Indicador titulo="Convocatorias abiertas" valor={d.convocatoriasAbiertas} />
              <Indicador titulo="Postulaciones" valor={d.postulaciones} />
              <Indicador titulo="Prácticas" valor={d.practicas} detalle={`${d.practicasFinalizadas} finalizadas`} />
              <Indicador titulo="Tasa de colocación" valor={`${d.tasaColocacion}%`}
                         detalle={`${d.estudiantesConPractica} de ${d.estudiantesPostulados} postulados`} />
            </div>

            <Tarjeta titulo="Pendientes">
              <ul className="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
                <Pendiente cantidad={d.empresasPendientes} texto="empresas por validar" a="/admin/validaciones" />
                <Pendiente cantidad={d.estudiantesPendientes} texto="estudiantes por validar" a="/admin/validaciones?tipo=estudiantes" />
                <Pendiente cantidad={d.ofertasSinEvaluador} texto={`ofertas sin evaluador (de ${d.ofertasEnRevision} en revisión)`} a="/admin/convocatorias" />
                <Pendiente cantidad={d.practicasPorFormalizar} texto="prácticas por formalizar" a="/admin/practicas" />
              </ul>
            </Tarjeta>

            <div className="grid gap-5 lg:grid-cols-3">
              <Tarjeta titulo="Empresas validadas por municipio"><Barras datos={d.empresasPorMunicipio} /></Tarjeta>
              <Tarjeta titulo="Áreas de mayor demanda (ofertas)"><Barras datos={d.ofertasPorArea} /></Tarjeta>
              <Tarjeta titulo="Postulaciones por estado">
                <Barras datos={d.postulacionesPorEstado.map((c) => ({ ...c, etiqueta: ESTADO[c.etiqueta]?.[0] ?? c.etiqueta }))} />
              </Tarjeta>
            </div>
          </>
        )}
      </Carga>
    </div>
  )
}

function Indicador({ titulo, valor, detalle }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4">
      <p className="text-xs font-medium text-slate-500">{titulo}</p>
      <p className="mt-1 text-2xl font-bold text-slate-900">{valor}</p>
      {detalle && <p className="text-xs text-slate-500">{detalle}</p>}
    </div>
  )
}

function Pendiente({ cantidad, texto, a }) {
  return (
    <li>
      <Link to={a} className={`block rounded-lg px-3 py-2 text-sm hover:bg-slate-50 ${cantidad > 0 ? 'bg-amber-50 text-amber-900' : 'text-slate-600'}`}>
        <strong className="text-lg">{cantidad}</strong> {texto}
      </Link>
    </li>
  )
}

/** Barras horizontales con la cifra al lado: se leen igual en pantalla y en el PDF impreso. */
function Barras({ datos }) {
  if (!datos.length) return <p className="text-sm text-slate-500">Sin datos en este periodo.</p>
  const maximo = Math.max(...datos.map((x) => x.cantidad))
  return (
    <ul className="space-y-2">
      {datos.map((x) => (
        <li key={x.etiqueta} className="text-sm">
          <div className="flex justify-between gap-2"><span className="truncate text-slate-700">{x.etiqueta}</span><span className="font-semibold">{x.cantidad}</span></div>
          <div className="mt-0.5 h-2 rounded-full bg-slate-100">
            <div className="h-full rounded-full bg-marca-500 print:bg-slate-700" style={{ width: `${(x.cantidad / maximo) * 100}%` }} />
          </div>
        </li>
      ))}
    </ul>
  )
}
