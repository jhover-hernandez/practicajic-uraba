// Formatos para mostrar datos en pantalla (es-CO).
// Las fechas llegan como "2026-11-07" o "2026-11-07T10:30:00"; se leen como hora local, nunca como UTC.

const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

export function fecha(iso) {
  if (!iso) return '—'
  const [a, m, d] = iso.slice(0, 10).split('-')
  return `${Number(d)} ${MESES[Number(m) - 1]} ${a}`
}

export function fechaHora(iso) {
  if (!iso) return '—'
  const [h, min] = iso.slice(11, 16).split(':')
  const hora = Number(h)
  return `${fecha(iso)}, ${hora % 12 || 12}:${min} ${hora < 12 ? 'a. m.' : 'p. m.'}`
}

export function dinero(valor) {
  if (valor === null || valor === undefined || valor === '') return '—'
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor)
}

export function hoyIso() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** Convierte el texto de un campo numérico en número, o null si está vacío. */
export function num(valor) {
  return valor === '' || valor === null || valor === undefined ? null : Number(valor)
}

export const NOMBRE_MODALIDAD = { PRESENCIAL: 'Presencial', REMOTA: 'Remota', HIBRIDA: 'Híbrida' }

export const NOMBRE_NIVEL = { BASICO: 'Básico', INTERMEDIO: 'Intermedio', AVANZADO: 'Avanzado' }

export const NOMBRE_TAMANO = { MICRO: 'Microempresa', PEQUENA: 'Pequeña', MEDIANA: 'Mediana', GRANDE: 'Grande' }

export const NOMBRE_TIPO_PORTAFOLIO = { PROYECTO: 'Proyecto', REPOSITORIO: 'Repositorio', CERTIFICACION: 'Certificación' }

export const NOMBRE_TIPO_SEGUIMIENTO = {
  VISITA: 'Visita', INFORME: 'Informe', EVALUACION_PARCIAL: 'Evaluación parcial', EVALUACION_FINAL: 'Evaluación final',
}

// Estados con el color fijo de las convenciones del prototipo:
// gris Postulado, amarillo En revisión, azul Entrevista, verde Aprobada o Preseleccionado, rojo Ajustes o Rechazada.
export const ESTADO = {
  // Postulación
  POSTULADO: ['Postulado', 'gris'],
  EN_REVISION: ['En revisión', 'amarillo'],
  PRESELECCIONADO: ['Preseleccionado', 'verde'],
  ENTREVISTA: ['Entrevista', 'azul'],
  SELECCIONADO: ['Seleccionado', 'verde'],
  NO_SELECCIONADO: ['No seleccionado', 'rojo'],
  PRACTICA_INICIADA: ['Práctica iniciada', 'azul'],
  PRACTICA_FINALIZADA: ['Práctica finalizada', 'gris'],
  // Oferta
  BORRADOR: ['Borrador', 'gris'],
  AJUSTES_SOLICITADOS: ['Ajustes solicitados', 'rojo'],
  APROBADA: ['Aprobada (abierta)', 'verde'],
  CERRADA: ['Cerrada', 'gris'],
  RECHAZADA: ['Rechazada', 'rojo'],
  // Empresa y estudiante
  PENDIENTE: ['Pendiente', 'amarillo'],
  VALIDADA: ['Validada', 'verde'],
  VALIDADO: ['Validado', 'verde'],
  RECHAZADO: ['Rechazado', 'rojo'],
  INACTIVA: ['Inactiva', 'gris'],
  INACTIVO: ['Inactivo', 'gris'],
  // Práctica
  INICIADA: ['En curso', 'azul'],
  FINALIZADA: ['Finalizada', 'verde'],
  CANCELADA: ['Cancelada', 'rojo'],
}
