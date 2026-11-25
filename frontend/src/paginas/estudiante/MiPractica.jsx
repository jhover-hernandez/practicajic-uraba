import { api } from '../../api/cliente.js'
import FichaPractica from '../../componentes/FichaPractica.jsx'
import { Carga, Encabezado, Vacio } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'

/** "Mi práctica": vinculación formal y seguimientos registrados por la empresa y el Politécnico. */
export default function MiPractica() {
  const { datos, error, cargando } = useCarga(() => api.get('/estudiante/practicas'))
  return (
    <div className="max-w-4xl space-y-5">
      <Encabezado titulo="Mi práctica" />
      <Carga cargando={cargando} error={error}>
        {datos?.length === 0
          ? <Vacio>Aún no tienes una práctica formalizada. Cuando aceptes una y el Politécnico la formalice, aparecerá aquí.</Vacio>
          : datos?.map((p) => <FichaPractica key={p.id} practica={p} />)}
      </Carga>
    </div>
  )
}
