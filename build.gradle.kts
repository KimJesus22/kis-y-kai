// Archivo de construcción de nivel superior donde puedes agregar opciones de configuración comunes a todos los subproyectos/módulos.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.kotlin.serialization) apply false
}
