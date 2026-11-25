import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { ESTADO } from './formato.js'

export function Logo({ subtitulo }) {
  return (
    <Link to="/" className="flex items-center gap-2">
      <span className="grid h-9 w-9 place-items-center rounded-lg bg-marca-600 text-sm font-bold text-white">PJ</span>
      <span className="leading-tight">
        <span className="block font-bold tracking-tight text-slate-900">PRACTICAJIC URABÁ</span>
        {subtitulo && <span className="block text-xs text-slate-500">{subtitulo}</span>}
      </span>
    </Link>
  )
}

const estiloControl =
  'mt-1 block w-full rounded-lg border bg-white px-3 py-2 text-sm shadow-sm outline-none ' +
  'focus:border-marca-500 focus:ring-2 focus:ring-marca-100'

/** Campo de formulario con etiqueta y mensaje de error bajo el control (flujo alterno de los casos de uso). */
export function Campo({ etiqueta, error, como = 'input', children, className = '', ...props }) {
  const Control = como
  const borde = error ? 'border-red-400' : 'border-slate-300'
  return (
    <label className={`block text-sm ${className}`}>
      <span className="font-medium text-slate-700">{etiqueta}</span>
      <Control className={`${estiloControl} ${borde}`} aria-invalid={!!error} {...props}>
        {children}
      </Control>
      {error && <span className="mt-1 block text-xs text-red-600">{error}</span>}
    </label>
  )
}

export function Boton({ variante = 'primario', className = '', ...props }) {
  const estilos = {
    primario: 'bg-marca-600 text-white hover:bg-marca-700 disabled:bg-marca-500/60',
    secundario: 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50',
    peligro: 'border border-red-200 bg-white text-red-700 hover:bg-red-50',
  }
  return (
    <button
      className={`inline-flex items-center justify-center rounded-lg px-4 py-2 text-sm font-semibold transition disabled:cursor-not-allowed ${estilos[variante]} ${className}`}
      {...props}
    />
  )
}

export function Aviso({ tipo = 'info', children }) {
  const estilos = {
    info: 'border-sky-200 bg-sky-50 text-sky-800',
    exito: 'border-marca-100 bg-marca-50 text-marca-800',
    error: 'border-red-200 bg-red-50 text-red-700',
    alerta: 'border-amber-200 bg-amber-50 text-amber-800',
  }
  return (
    <div role={tipo === 'error' ? 'alert' : 'status'} className={`rounded-lg border px-4 py-3 text-sm ${estilos[tipo]}`}>
      {children}
    </div>
  )
}

/** Colores fijos por estado definidos en las convenciones del prototipo. */
export function Insignia({ color = 'gris', children }) {
  const estilos = {
    gris: 'bg-slate-100 text-slate-700',
    amarillo: 'bg-amber-100 text-amber-800',
    azul: 'bg-sky-100 text-sky-800',
    verde: 'bg-marca-100 text-marca-800',
    rojo: 'bg-red-100 text-red-700',
  }
  return <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-semibold ${estilos[color]}`}>{children}</span>
}

export function EstadoInsignia({ estado }) {
  const [texto, color] = ESTADO[estado] ?? [estado, 'gris']
  return <Insignia color={color}>{texto}</Insignia>
}

/** Título de cada sección del panel, con acciones a la derecha. */
export function Encabezado({ titulo, descripcion, children }) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">{titulo}</h1>
        {descripcion && <p className="text-sm text-slate-600">{descripcion}</p>}
      </div>
      {children && <div className="flex flex-wrap gap-2 print:hidden">{children}</div>}
    </div>
  )
}

export function Tarjeta({ titulo, acciones, children, className = '' }) {
  return (
    <section className={`rounded-xl border border-slate-200 bg-white p-4 sm:p-5 ${className}`}>
      {(titulo || acciones) && (
        <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
          {titulo && <h2 className="font-semibold text-slate-900">{titulo}</h2>}
          {acciones}
        </div>
      )}
      {children}
    </section>
  )
}

/** Muestra "Cargando…", el error o el contenido. */
export function Carga({ cargando, error, children }) {
  if (error) return <Aviso tipo="error">{error}</Aviso>
  if (cargando) return <p className="py-6 text-center text-sm text-slate-500">Cargando…</p>
  return children
}

export function Vacio({ children }) {
  return <p className="rounded-xl border border-dashed border-slate-300 bg-white px-4 py-8 text-center text-sm text-slate-500">{children}</p>
}

/** Ventana modal. Se cierra con Escape o con el botón Cancelar del contenido. */
export function Modal({ titulo, onCerrar, children, ancho = 'max-w-lg' }) {
  useEffect(() => {
    const alPresionar = (e) => e.key === 'Escape' && onCerrar()
    document.addEventListener('keydown', alPresionar)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', alPresionar)
      document.body.style.overflow = ''
    }
  }, [onCerrar])

  return (
    <div className="fixed inset-0 z-20 overflow-y-auto bg-slate-900/40 p-4" role="dialog" aria-modal="true" aria-label={titulo}>
      <div className={`mx-auto my-8 w-full ${ancho} rounded-2xl bg-white p-6 shadow-xl`}>
        <div className="mb-4 flex items-start justify-between gap-3">
          <h2 className="text-lg font-bold text-slate-900">{titulo}</h2>
          <button onClick={onCerrar} className="rounded-lg px-2 text-xl leading-none text-slate-400 hover:bg-slate-100"
                  aria-label="Cerrar">×</button>
        </div>
        {children}
      </div>
    </div>
  )
}

/** Barra de coincidencia de competencias (pantallas 2 y 3 del prototipo). */
export function BarraCoincidencia({ coincidentes, total }) {
  const porcentaje = total ? Math.round((coincidentes / total) * 100) : 0
  return (
    <div className="min-w-32">
      <div className="h-2 overflow-hidden rounded-full bg-slate-100">
        <div className="h-full rounded-full bg-marca-500" style={{ width: `${porcentaje}%` }} />
      </div>
      <p className="mt-1 text-xs text-slate-600">{coincidentes} de {total} competencias coinciden</p>
    </div>
  )
}

/** Fila "etiqueta: valor" para fichas de detalle. */
export function Dato({ etiqueta, children }) {
  return (
    <div>
      <dt className="text-xs font-medium uppercase tracking-wide text-slate-500">{etiqueta}</dt>
      <dd className="mt-0.5 text-sm text-slate-800">{children ?? '—'}</dd>
    </div>
  )
}

export function TarjetaFormulario({ titulo, descripcion, children }) {
  return (
    <div className="min-h-screen px-4 py-8">
      <div className="mx-auto max-w-xl">
        <div className="mb-6">
          <Logo subtitulo="Politécnico Colombiano Jaime Isaza Cadavid" />
        </div>
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
          <h1 className="text-xl font-bold text-slate-900">{titulo}</h1>
          {descripcion && <p className="mt-1 text-sm text-slate-600">{descripcion}</p>}
          <div className="mt-6">{children}</div>
        </div>
      </div>
    </div>
  )
}
