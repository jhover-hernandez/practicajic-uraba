// Roles, rutas de inicio y menús de cada panel, tomados del prototipo UX/UI.
// La página de cada entrada se asigna en el objeto PAGINAS de App.jsx.

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
    { ruta: 'convocatorias', texto: 'Convocatorias' },
    { ruta: 'postulaciones', texto: 'Mis postulaciones' },
    { ruta: 'perfil', texto: 'Mi perfil digital' },
    { ruta: 'practica', texto: 'Mi práctica' },
  ],
  EMPRESA: [
    { ruta: '', texto: 'Inicio' },
    { ruta: 'ofertas', texto: 'Mis ofertas' },
    { ruta: 'postulantes', texto: 'Postulantes' },
    { ruta: 'practicas', texto: 'Prácticas activas' },
    { ruta: 'perfil', texto: 'Perfil de la empresa' },
  ],
  COMITE: [
    { ruta: '', texto: 'Inicio' },
    { ruta: 'ofertas', texto: 'Ofertas pendientes' },
    { ruta: 'candidatos', texto: 'Candidatos por calificar' },
    { ruta: 'actas', texto: 'Actas de comité' },
    { ruta: 'historial', texto: 'Historial' },
  ],
  ADMIN: [
    { ruta: '', texto: 'Panel' },
    { ruta: 'validaciones', texto: 'Validaciones' },
    { ruta: 'usuarios', texto: 'Usuarios y roles' },
    { ruta: 'convocatorias', texto: 'Convocatorias' },
    { ruta: 'practicas', texto: 'Prácticas y seguimiento' },
    { ruta: 'catalogos', texto: 'Áreas y competencias' },
    { ruta: 'informes', texto: 'Informes' },
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
