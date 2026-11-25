import { useCallback, useEffect, useState } from 'react'

/** Carga datos de la API al montar y cada vez que cambian las dependencias. `recargar` repite la consulta. */
export function useCarga(consulta, dependencias = []) {
  const [datos, setDatos] = useState(null)
  const [error, setError] = useState(null)
  const [cargando, setCargando] = useState(true)

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const ejecutar = useCallback(consulta, dependencias)

  const recargar = useCallback(async () => {
    setCargando(true)
    try {
      setDatos(await ejecutar())
      setError(null)
    } catch (e) {
      setError(e.message)
    } finally {
      setCargando(false)
    }
  }, [ejecutar])

  useEffect(() => {
    recargar()
  }, [recargar])

  return { datos, setDatos, error, cargando, recargar }
}
