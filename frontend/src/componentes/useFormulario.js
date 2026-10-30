import { useState } from 'react'

/** Estado de un formulario que envía a la API y muestra los errores por campo que devuelve el backend. */
export function useFormulario(inicial, enviar) {
  const [valores, setValores] = useState(inicial)
  const [errores, setErrores] = useState({})
  const [mensajeError, setMensajeError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  function cambiar(e) {
    const { name, type, checked, value } = e.target
    setValores((v) => ({ ...v, [name]: type === 'checkbox' ? checked : value }))
    setErrores((er) => ({ ...er, [name]: undefined }))
  }

  async function alEnviar(e) {
    e.preventDefault()
    setMensajeError(null)
    setErrores({})
    setEnviando(true)
    try {
      await enviar(valores)
    } catch (err) {
      setErrores(err.campos ?? {})
      setMensajeError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  // Atajo para conectar un <Campo name="..."> con su valor, cambio y error.
  const campo = (name) => ({ name, value: valores[name] ?? '', onChange: cambiar, error: errores[name] })

  return { valores, setValores, errores, mensajeError, enviando, cambiar, alEnviar, campo }
}
