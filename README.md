# JAVA-JuegoAhorcadoV3
Versión mejorada del Juego del Ahorcado:
1. Lee las palabras a descubrir desde un fichero de recurso. Se trata de un fichero de texto con más de 8000 palabras extraídas de una novela.

Un fichero de recurso se guarda dentro del path de la aplicación. Cuando se genera el fichero jar los ficheros de recursos se incorporan dentro, junto con los class y demás componentes de la aplicación.
Java tiene clases especificas para manejar (leer) ficheros de recurso (ver código fuente).

2. Lee los Strings que representan el dibujo de las horcas de un fichero de texto. Esto permite que estos strings no formen parte del código pudiendose gestionar, cambiar, etc. directamente sobre un fichero de texto independiente. 

3. Comprueba que la letra escrita por consola para completar la palabra sea un caracter válido del alfabeto español.No se admiten tildes. En caso de dar un caracter no válido informa de ello con un mensaje.
