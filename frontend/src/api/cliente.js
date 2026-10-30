// Cliente HTTP de la API.
// El token de acceso vive solo en memoria; el de renovación lo maneja el navegador en una cookie HttpOnly.

let tokenAcceso = null
let renovacionEnCurso = null
let alExpirarSesion = () => {}

export class ErrorApi extends Error {
  constructor(estado, mensaje, campos) {
    super(mensaje)
    this.estado = estado
    this.campos = campos ?? {}
  }
}

export function fijarToken(token) {
  tokenAcceso = token
}

export function alPerderSesion(callback) {
  alExpirarSesion = callback
}

/** Pide un token nuevo. Si varias peticiones fallan a la vez, comparten una sola renovación. */
export function renovarSesion() {
  if (!renovacionEnCurso) {
    renovacionEnCurso = fetch('/api/auth/refresh', { method: 'POST', credentials: 'same-origin' })
      .then(async (r) => {
        if (!r.ok) throw new ErrorApi(r.status, 'Tu sesión expiró. Inicia sesión de nuevo.')
        const sesion = await r.json()
        tokenAcceso = sesion.accessToken
        return sesion
      })
      .finally(() => {
        renovacionEnCurso = null
      })
  }
  return renovacionEnCurso
}

async function ejecutar(metodo, ruta, cuerpo) {
  const encabezados = {}
  if (cuerpo !== undefined) encabezados['Content-Type'] = 'application/json'
  if (tokenAcceso) encabezados.Authorization = `Bearer ${tokenAcceso}`
  try {
    return await fetch(`/api${ruta}`, {
      method: metodo,
      headers: encabezados,
      credentials: 'same-origin',
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    })
  } catch {
    throw new ErrorApi(0, 'No hay conexión con el servidor. Revisa tu internet e intenta de nuevo.')
  }
}

async function solicitar(metodo, ruta, cuerpo) {
  let respuesta = await ejecutar(metodo, ruta, cuerpo)

  // Token de acceso vencido: se renueva una vez y se repite la petición.
  if (respuesta.status === 401 && tokenAcceso && !ruta.startsWith('/auth/')) {
    try {
      await renovarSesion()
      respuesta = await ejecutar(metodo, ruta, cuerpo)
    } catch {
      tokenAcceso = null
      alExpirarSesion()
    }
  }

  if (respuesta.status === 204) return null
  const datos = await respuesta.json().catch(() => null)
  if (!respuesta.ok) {
    throw new ErrorApi(respuesta.status, datos?.mensaje ?? 'Ocurrió un error inesperado.', datos?.campos)
  }
  return datos
}

export const api = {
  get: (ruta) => solicitar('GET', ruta),
  post: (ruta, cuerpo) => solicitar('POST', ruta, cuerpo ?? {}),
  put: (ruta, cuerpo) => solicitar('PUT', ruta, cuerpo),
  patch: (ruta, cuerpo) => solicitar('PATCH', ruta, cuerpo),
}
