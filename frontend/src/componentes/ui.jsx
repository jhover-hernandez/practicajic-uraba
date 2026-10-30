import { Link } from 'react-router-dom'

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
