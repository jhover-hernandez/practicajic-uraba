import { Navigate, Route, Routes } from 'react-router-dom'
import RutaProtegida from './auth/RutaProtegida.jsx'
import { MENU_ROL, RUTA_ROL } from './auth/roles.js'
import PanelLayout from './componentes/PanelLayout.jsx'
import InicioPanel from './paginas/InicioPanel.jsx'
import CatalogosAdmin from './paginas/admin/Catalogos.jsx'
import ConvocatoriasAdmin from './paginas/admin/Convocatorias.jsx'
import Informes from './paginas/admin/Informes.jsx'
import Panel from './paginas/admin/Panel.jsx'
import PracticasAdmin from './paginas/admin/Practicas.jsx'
import Usuarios from './paginas/admin/Usuarios.jsx'
import Validaciones from './paginas/admin/Validaciones.jsx'
import Actas from './paginas/comite/Actas.jsx'
import Candidatos from './paginas/comite/Candidatos.jsx'
import Historial from './paginas/comite/Historial.jsx'
import OfertasComite from './paginas/comite/Ofertas.jsx'
import OfertasEmpresa from './paginas/empresa/Ofertas.jsx'
import PerfilEmpresa from './paginas/empresa/Perfil.jsx'
import Postulantes from './paginas/empresa/Postulantes.jsx'
import PracticasEmpresa from './paginas/empresa/Practicas.jsx'
import Convocatorias from './paginas/estudiante/Convocatorias.jsx'
import MiPractica from './paginas/estudiante/MiPractica.jsx'
import PerfilEstudiante from './paginas/estudiante/Perfil.jsx'
import Postulaciones from './paginas/estudiante/Postulaciones.jsx'
import AccesoDenegado from './paginas/publicas/AccesoDenegado.jsx'
import Inicio from './paginas/publicas/Inicio.jsx'
import PoliticaDatos from './paginas/publicas/PoliticaDatos.jsx'
import Recuperar from './paginas/publicas/Recuperar.jsx'
import RegistroEmpresa from './paginas/publicas/RegistroEmpresa.jsx'
import RegistroEstudiante from './paginas/publicas/RegistroEstudiante.jsx'
import Restablecer from './paginas/publicas/Restablecer.jsx'

// Página de cada entrada del menú (roles.js). La clave '' es la página de inicio del panel.
const PAGINAS = {
  ESTUDIANTE: {
    '': <InicioPanel />,
    convocatorias: <Convocatorias />,
    postulaciones: <Postulaciones />,
    perfil: <PerfilEstudiante />,
    practica: <MiPractica />,
  },
  EMPRESA: {
    '': <InicioPanel />,
    ofertas: <OfertasEmpresa />,
    postulantes: <Postulantes />,
    practicas: <PracticasEmpresa />,
    perfil: <PerfilEmpresa />,
  },
  COMITE: {
    '': <InicioPanel />,
    ofertas: <OfertasComite />,
    candidatos: <Candidatos />,
    actas: <Actas />,
    historial: <Historial />,
  },
  ADMIN: {
    '': <Panel />,
    validaciones: <Validaciones />,
    usuarios: <Usuarios />,
    convocatorias: <ConvocatoriasAdmin />,
    practicas: <PracticasAdmin />,
    catalogos: <CatalogosAdmin />,
    informes: <Informes />,
  },
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Inicio />} />
      <Route path="/registro/estudiante" element={<RegistroEstudiante />} />
      <Route path="/registro/empresa" element={<RegistroEmpresa />} />
      <Route path="/recuperar" element={<Recuperar />} />
      <Route path="/restablecer" element={<Restablecer />} />
      <Route path="/politica-datos" element={<PoliticaDatos />} />
      <Route path="/acceso-denegado" element={<AccesoDenegado />} />

      {Object.keys(RUTA_ROL).map((rol) => (
        <Route key={rol} element={<RutaProtegida roles={[rol]} />}>
          <Route path={RUTA_ROL[rol]} element={<PanelLayout rol={rol} />}>
            <Route index element={PAGINAS[rol]['']} />
            {MENU_ROL[rol].filter((item) => item.ruta).map((item) => (
              <Route key={item.ruta} path={item.ruta} element={PAGINAS[rol][item.ruta]} />
            ))}
          </Route>
        </Route>
      ))}

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
