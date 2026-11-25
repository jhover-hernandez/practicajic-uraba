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
  const esArchivo = cuerpo instanceof FormData
  if (cuerpo !== undefined && !esArchivo) encabezados['Content-Type'] = 'application/json'
  if (tokenAcceso) encabezados.Authorization = `Bearer ${tokenAcceso}`
  try {
    return await fetch(`/api${ruta}`, {
      method: metodo,
      headers: encabezados,
      credentials: 'same-origin',
      body: cuerpo === undefined ? undefined : esArchivo ? cuerpo : JSON.stringify(cuerpo),
    })
  } catch {
    throw new ErrorApi(0, 'No hay conexión con el servidor. Revisa tu internet e intenta de nuevo.')
  }
}

/** Repite la petición una vez si el token de acceso venció. */
async function conRenovacion(metodo, ruta, cuerpo) {
  let respuesta = await ejecutar(metodo, ruta, cuerpo)
  if (respuesta.status === 401 && tokenAcceso && !ruta.startsWith('/auth/')) {
    try {
      await renovarSesion()
      respuesta = await ejecutar(metodo, ruta, cuerpo)
    } catch {
      tokenAcceso = null
      alExpirarSesion()
    }
  }
  return respuesta
}

/** Descarga un archivo protegido (PDF o Excel) como Blob, con el token de la sesión. */
async function archivo(ruta) {
  const respuesta = await conRenovacion('GET', ruta)
  if (!respuesta.ok) {
    const datos = await respuesta.json().catch(() => null)
    throw new ErrorApi(respuesta.status, datos?.mensaje ?? 'No se pudo abrir el archivo.')
  }
  return respuesta.blob()
}

async function solicitar(metodo, ruta, cuerpo) {
  const respuesta = await conRenovacion(metodo, ruta, cuerpo)
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
  delete: (ruta) => solicitar('DELETE', ruta),
  /** Envía un archivo en el campo `archivo` (multipart). */
  subir: (ruta, file) => {
    const datos = new FormData()
    datos.append('archivo', file)
    return solicitar('POST', ruta, datos)
  },

  /** Abre un PDF protegido en una pestaña nueva. */
  async abrirPdf(ruta) {
    // La pestaña se abre antes de la descarga para que el navegador no la bloquee como ventana emergente.
    const pestana = window.open('', '_blank')
    try {
      const url = URL.createObjectURL(await archivo(ruta))
      if (pestana) pestana.location.href = url
      else window.location.href = url
      setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } catch (e) {
      pestana?.close()
      throw e
    }
  },

  /** Descarga un archivo protegido con el nombre indicado. */
  async descargar(ruta, nombre) {
    const url = URL.createObjectURL(await archivo(ruta))
    const enlace = document.createElement('a')
    enlace.href = url
    enlace.download = nombre
    enlace.click()
    setTimeout(() => URL.revokeObjectURL(url), 10_000)
  },
}
