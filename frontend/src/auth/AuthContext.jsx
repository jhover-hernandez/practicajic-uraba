import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { alPerderSesion, api, fijarToken, renovarSesion } from '../api/cliente.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [cargando, setCargando] = useState(true)

  // Al abrir la página se intenta recuperar la sesión con la cookie de renovación.
  useEffect(() => {
    alPerderSesion(() => setUsuario(null))
    renovarSesion()
      .then((sesion) => setUsuario(sesion.usuario))
      .catch(() => setUsuario(null))
      .finally(() => setCargando(false))
  }, [])

  const iniciarSesion = useCallback(async (correo, contrasena) => {
    const sesion = await api.post('/auth/login', { correo, contrasena })
    fijarToken(sesion.accessToken)
    setUsuario(sesion.usuario)
    return sesion.usuario
  }, [])

  const cerrarSesion = useCallback(async () => {
    try {
      await api.post('/auth/logout')
    } finally {
      fijarToken(null)
      setUsuario(null)
    }
  }, [])

  return (
    <AuthContext.Provider value={{ usuario, cargando, iniciarSesion, cerrarSesion }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
