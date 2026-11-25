import { api } from '../../api/cliente.js'
import FichaPractica from '../../componentes/FichaPractica.jsx'
import { Carga, Encabezado, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

/** RF-11 y RF-36: la empresa evalúa al practicante durante y al final de la práctica. */
export default function Practicas() {
  const { datos, setDatos, error, cargando } = useCarga(() => api.get('/empresa/practicas'))
  const reemplazar = (nueva) => setDatos((l) => l.map((p) => (p.id === nueva.id ? nueva : p)))
  return (
    <div className="max-w-4xl space-y-5">
      <Encabezado titulo="Prácticas activas" descripcion="Registra evaluaciones parciales y la evaluación final de cada practicante." />
      <Carga cargando={cargando} error={error}>
        {datos?.length === 0
          ? <Vacio>Aún no tienes prácticas formalizadas.</Vacio>
          : datos?.map((p) => (
            <FichaPractica key={p.id} practica={p} rutaSeguimiento={`/empresa/practicas/${p.id}/seguimientos`} onActualizada={reemplazar} />
          ))}
      </Carga>
    </div>
  )
}
