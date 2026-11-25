import { TarjetaFormulario } from '../../componentes/ui.jsx'

/**
 * Resumen de la política de tratamiento de datos. El texto oficial lo define el Politécnico;
 * este contenido debe reemplazarse por el aprobado antes del piloto.
 */
export default function PoliticaDatos() {
  return (
    <TarjetaFormulario titulo="Política de tratamiento de datos personales"
                       descripcion="Ley 1581 de 2012 y políticas del Politécnico Colombiano Jaime Isaza Cadavid">
      <div className="space-y-3 text-sm text-slate-700">
        <p>
          Al registrarte en PRACTICAJIC URABÁ autorizas al Politécnico Colombiano Jaime Isaza Cadavid a recolectar,
          almacenar y usar tus datos personales con el único fin de gestionar el proceso de prácticas profesionales:
          validación de tu cuenta, publicación de ofertas, postulaciones, evaluación y seguimiento.
        </p>
        <p>
          Los datos de los estudiantes solo son visibles para las empresas en las convocatorias a las que el estudiante
          se postula. Las contraseñas se guardan cifradas y nunca en texto plano.
        </p>
        <p>
          Como titular puedes conocer, actualizar, rectificar y solicitar la supresión de tus datos, así como revocar
          esta autorización, comunicándote con el Politécnico – Regional Urabá.
        </p>
        <p className="rounded-lg bg-amber-50 px-3 py-2 text-amber-800">
          Texto provisional para el piloto: se reemplazará por la política oficial aprobada por el Politécnico.
        </p>
      </div>
    </TarjetaFormulario>
  )
}
