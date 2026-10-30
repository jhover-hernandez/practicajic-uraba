// Roles, rutas de inicio y menús de cada panel, tomados del prototipo UX/UI.
// Las entradas con `tarea` pertenecen a módulos que se construyen en otras tareas del tablero Kanban.

export const NOMBRE_ROL = {
  ADMIN: 'Administrador institucional',
  EMPRESA: 'Empresa',
  ESTUDIANTE: 'Estudiante',
  COMITE: 'Comité evaluador',
}

export const RUTA_ROL = {
  ADMIN: '/admin',
  EMPRESA: '/empresa',
  ESTUDIANTE: '/estudiante',
  COMITE: '/comite',
}

export const MENU_ROL = {
  ESTUDIANTE: [
    { ruta: '', texto: 'Inicio' },
    { ruta: 'convocatorias', texto: 'Convocatorias', tarea: 12 },
    { ruta: 'postulaciones', texto: 'Mis postulaciones', tarea: 12 },
    { ruta: 'perfil', texto: 'Mi perfil digital', tarea: 11 },
    { ruta: 'practica', texto: 'Mi práctica', tarea: 14 },
  ],
  EMPRESA: [
    { ruta: '', texto: 'Inicio' },
    { ruta: 'ofertas', texto: 'Mis ofertas', tarea: 10 },
    { ruta: 'postulantes', texto: 'Postulantes', tarea: 12 },
    { ruta: 'practicas', texto: 'Prácticas activas', tarea: 14 },
    { ruta: 'perfil', texto: 'Perfil de la empresa', tarea: 10 },
  ],
  COMITE: [
    { ruta: '', texto: 'Inicio' },
    { ruta: 'ofertas', texto: 'Ofertas pendientes', tarea: 13 },
    { ruta: 'candidatos', texto: 'Candidatos por calificar', tarea: 13 },
    { ruta: 'actas', texto: 'Actas de comité', tarea: 13 },
    { ruta: 'historial', texto: 'Historial', tarea: 13 },
  ],
  ADMIN: [
    { ruta: '', texto: 'Panel' },
    { ruta: 'validaciones', texto: 'Validaciones', tarea: 10 },
    { ruta: 'usuarios', texto: 'Usuarios y roles' },
    { ruta: 'convocatorias', texto: 'Convocatorias', tarea: 12 },
    { ruta: 'practicas', texto: 'Prácticas y seguimiento', tarea: 14 },
    { ruta: 'catalogos', texto: 'Áreas y competencias', tarea: 12 },
    { ruta: 'informes', texto: 'Informes', tarea: 14 },
  ],
}

export const MUNICIPIOS_URABA = [
  'Apartadó', 'Arboletes', 'Carepa', 'Chigorodó', 'Murindó', 'Mutatá', 'Necoclí',
  'San Juan de Urabá', 'San Pedro de Urabá', 'Turbo', 'Vigía del Fuerte',
]

export function iniciales(nombre) {
  if (!nombre) return '?'
  return nombre.trim().split(/\s+/).slice(0, 2).map((p) => p[0]).join('').toUpperCase()
}
