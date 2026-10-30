import { Navigate, Route, Routes } from 'react-router-dom'
import RutaProtegida from './auth/RutaProtegida.jsx'
import { MENU_ROL, RUTA_ROL } from './auth/roles.js'
import PanelLayout from './componentes/PanelLayout.jsx'
import EnConstruccion from './paginas/EnConstruccion.jsx'
import InicioPanel from './paginas/InicioPanel.jsx'
import Usuarios from './paginas/admin/Usuarios.jsx'
import AccesoDenegado from './paginas/publicas/AccesoDenegado.jsx'
import Inicio from './paginas/publicas/Inicio.jsx'
import PoliticaDatos from './paginas/publicas/PoliticaDatos.jsx'
import Recuperar from './paginas/publicas/Recuperar.jsx'
import RegistroEmpresa from './paginas/publicas/RegistroEmpresa.jsx'
import RegistroEstudiante from './paginas/publicas/RegistroEstudiante.jsx'
import Restablecer from './paginas/publicas/Restablecer.jsx'

// Páginas ya construidas por rol; el resto del menú muestra "en construcción".
const PAGINAS = {
  ADMIN: { usuarios: <Usuarios /> },
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
            <Route index element={<InicioPanel />} />
            {MENU_ROL[rol].filter((item) => item.ruta).map((item) => (
              <Route key={item.ruta} path={item.ruta}
                     element={PAGINAS[rol]?.[item.ruta] ?? <EnConstruccion titulo={item.texto} tarea={item.tarea} />} />
            ))}
          </Route>
        </Route>
      ))}

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
