import { useState } from 'react'
import { api } from '../../api/cliente.js'
import { Aviso, Boton, Campo, Carga, Encabezado, Modal, Tarjeta } from '../../componentes/ui.jsx'
import { useCarga } from '../../componentes/useCarga.js'
import { useFormulario } from '../../componentes/useFormulario.js'

/** RF-21: categorías de áreas y catálogo de competencias. */
export default function Catalogos() {
  const areas = useCarga(() => api.get('/catalogos/areas'))
  const competencias = useCarga(() => api.get('/catalogos/competencias'))
  const [edicion, setEdicion] = useState(null) // { tipo, item }
  const [aviso, setAviso] = useState(null)
  const [error, setError] = useState(null)

  async function eliminar(tipo, item) {
    if (!window.confirm(`¿Eliminar "${item.nombre}"?`)) return
    setError(null)
    try {
      await api.delete(`/admin/catalogos/${tipo}/${item.id}`)
      setAviso(`Se eliminó "${item.nombre}".`)
      ;(tipo === 'areas' ? areas : competencias).recargar()
    } catch (e) {
      setError(e.message)
    }
  }

  const fila = (tipo, item, extra) => (
    <li key={item.id} className="flex items-center justify-between gap-2 py-2">
      <span className="text-sm">{item.nombre}{extra}</span>
      <span className="flex gap-1">
        <Boton variante="secundario" className="px-2.5 py-1" onClick={() => setEdicion({ tipo, item })}>Editar</Boton>
        <Boton variante="peligro" className="px-2.5 py-1" onClick={() => eliminar(tipo, item)}>Eliminar</Boton>
      </span>
    </li>
  )

  return (
    <div className="max-w-5xl space-y-5">
      <Encabezado titulo="Áreas y competencias" descripcion="Las áreas clasifican las ofertas; las competencias se usan para calcular la coincidencia." />
      {aviso && <Aviso tipo="exito">{aviso}</Aviso>}
      {error && <Aviso tipo="error">{error}</Aviso>}
      <div className="grid gap-5 lg:grid-cols-[1fr_1.4fr]">
        <Tarjeta titulo="Áreas" acciones={<Boton variante="secundario" onClick={() => setEdicion({ tipo: 'areas', item: {} })}>+ Nueva</Boton>}>
          <Carga cargando={areas.cargando} error={areas.error}>
            <ul className="divide-y divide-slate-100">{areas.datos?.map((a) => fila('areas', a))}</ul>
          </Carga>
        </Tarjeta>
        <Tarjeta titulo="Competencias" acciones={<Boton variante="secundario" onClick={() => setEdicion({ tipo: 'competencias', item: {} })}>+ Nueva</Boton>}>
          <Carga cargando={competencias.cargando} error={competencias.error}>
            {['TECNICA', 'BLANDA'].map((cat) => (
              <div key={cat} className="mb-3">
                <h3 className="text-xs font-medium uppercase tracking-wide text-slate-500">{cat === 'TECNICA' ? 'Técnicas' : 'Blandas'}</h3>
                <ul className="divide-y divide-slate-100">
                  {competencias.datos?.filter((c) => c.categoria === cat).map((c) =>
                    fila('competencias', c, c.descripcion && <span className="block text-xs text-slate-500">{c.descripcion}</span>))}
                </ul>
              </div>
            ))}
          </Carga>
        </Tarjeta>
      </div>
      {edicion && (
        <Formulario {...edicion} onCerrar={() => setEdicion(null)}
                    onGuardado={(m) => { setEdicion(null); setAviso(m); (edicion.tipo === 'areas' ? areas : competencias).recargar() }} />
      )}
    </div>
  )
}

function Formulario({ tipo, item, onCerrar, onGuardado }) {
  const nuevo = !item.id
  const esArea = tipo === 'areas'
  const { mensajeError, enviando, alEnviar, campo } = useFormulario(
    { nombre: item.nombre ?? '', categoria: item.categoria ?? 'TECNICA', descripcion: item.descripcion ?? '' },
    async (v) => {
      const cuerpo = esArea ? { nombre: v.nombre } : v
      if (nuevo) await api.post(`/admin/catalogos/${tipo}`, cuerpo)
      else await api.put(`/admin/catalogos/${tipo}/${item.id}`, cuerpo)
      onGuardado(`Se guardó "${v.nombre}".`)
    },
  )
  return (
    <Modal titulo={`${nuevo ? 'Nueva' : 'Editar'} ${esArea ? 'área' : 'competencia'}`} onCerrar={onCerrar}>
      <form onSubmit={alEnviar} noValidate className="space-y-4">
        {mensajeError && <Aviso tipo="error">{mensajeError}</Aviso>}
        <Campo etiqueta="Nombre" {...campo('nombre')} />
        {!esArea && (
          <>
            <Campo etiqueta="Categoría" como="select" {...campo('categoria')}>
              <option value="TECNICA">Técnica</option>
              <option value="BLANDA">Blanda</option>
            </Campo>
            <Campo etiqueta="Descripción (opcional)" como="textarea" rows={2} {...campo('descripcion')} />
          </>
        )}
        <div className="flex justify-end gap-2">
          <Boton type="button" variante="secundario" onClick={onCerrar}>Cancelar</Boton>
          <Boton type="submit" disabled={enviando}>{enviando ? 'Guardando…' : 'Guardar'}</Boton>
        </div>
      </form>
    </Modal>
  )
}
