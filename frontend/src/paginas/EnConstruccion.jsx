/** Marcador para secciones de módulos que se construyen en otras tareas del tablero. */
export default function EnConstruccion({ titulo, tarea }) {
  return (
    <div className="max-w-2xl rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-center">
      <h1 className="text-xl font-bold text-slate-900">{titulo}</h1>
      <p className="mt-2 text-sm text-slate-600">Esta sección está en construcción (Tarea {tarea} del tablero).</p>
    </div>
  )
}
