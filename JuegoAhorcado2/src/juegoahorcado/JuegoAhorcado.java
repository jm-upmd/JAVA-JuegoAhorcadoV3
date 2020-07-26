
package juegoahorcado;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeSet;


public class JuegoAhorcado {

	// Array de strings. Cada string representa un estado de la horca pintada con
	// caracteres.
	// Cada string de cada dibujo de la horca se ha escrito en varivas líneas
	// concatenadas para que
	// visualmente sea más legible el patrón de dibujo que representan.
	// Por ejemplo, la primera horca podría representarse con el siguiente string en
	// una sola línea:
	// " +---+ \n | | \n | \n | \n | \n | \n=========\n"
	
	/* PROPUESTA DE MEJORA
	 * Extraer estos string a un fichero de texto externo y cargar el array a partir
	 * de él. Así no "ensuciamos" el códgo y lo podemos manejar mejor las cadenas 
	 * sobre el fichero con un editor de texto de forma independiente.
	 */

	static final int MAX_FALLOS = 6;
	
	private static String[] imagenesAhorcado = new String[MAX_FALLOS + 1]; 

	static final char[] saltos = new char[200];

	static Scanner sc;

	static ArrayList<String> palabras;

	static String palabraADescubrir; // Palabra a descubrir.

	// Palabra en construcción. Array donde se irá construyendo la palabra a medida
	// que el usuario da letras.
	static char[] palabraEnConstruccion;

	// Colección para almacenar las letras ya usadas y mostrarlas en la partida.
	// Usamos una TreeSet porque no almacena duplicados y además ordena los
	// elementos.
	// En el caso de guardar Characters los ordena alfabéticamente por defectro.
	// Ojo, por defecto no ordena bien la 'ñ' . Habría que usar un objeto Comparator
	// para establecer el orden correcto (eso lo veremos más adelante)

	static TreeSet<Character> letrasUtilizadas;

	static int contFallos = 0; // Contador de fallos. Se incrementa por cada letra suministrada que no
								// existenta en la palabra a descubrir.
	static int contTiradas = 0; // Contador del número de tiradas (letras suministradas) en la partida.
	static boolean acierto; // Indica si una jugada a sido existosa o no.

	static char letra; // Letra a comprobar si existe en palabraADescrubrir .

	public static void main(String[] args) {

		letrasUtilizadas = new TreeSet<>(); // Crea colección para guardar letras utilizadas.

		// Crea scanner si le damos charset como parametro lo utiliza en vez del de por
		// defecto

		if (args.length > 0)
			sc = new Scanner(System.in, args[0]);
		else
			sc = new Scanner(System.in);

		// Utilizamos para escribir 200 saltos de linea por consola y simular así
		// que la hemos borrado. Lo haremos en cada tirada para reescribir el contenido
		// mostrado.
		Arrays.fill(saltos, '\n'); // Llena array con saltos de linea.
		
		cargaMapasHorcas();

		cargaPalabras(); // Carga lista de palabras desde el fichero de recurso

		// Crea una nueva partida.(Pinta horca inicial y patrón (huecos)
		// de la palabra a descubrir, inicializa contadores, ...)

		nuevaPartida();

		// Bucle sin condición de parada ni contadores.
		// También se puede poner while (true)

		for (;;) {

			letra = pideLetra("Escribe la letra: "); // Pide letra por consola.

			if (!letraPermitida(letra)) {
				System.out.println("Solo se adminten letras del alfabeto.");
				continue; // Salta al comienzo del bucle.
			}

			// Comprueba ocurencias de la letra en la palabra a descubrir, las añade a la
			// palabra en construcción,
			// pinta la palabra en construcción actualizada y devuelve verdadero si la letra
			// de la jugada existe en la palabra a descubrir

			hazJugada();

			if (partidaTerminada()) { // Si la partida ha terminado preguntamos si quiere jugar otra
				// Pregunta por consola si jugar otra partida
				if (!jugarOtraPartida())
					break; // Si no se quiere jugar otra partida entonces sale del bucle while
			}

		} // fin for

		System.out.println("\n¡Hasta pronto!");

	} // fin main()

	/**
	 * Pregunta si se quiere jugar otra partida y en caso afirmativo inicializa las
	 * varibles palabra, palabraParcial, y pinta por consola la horca inicial y el
	 * patrón con guiones bajos de la palabra a descubrir
	 * 
	 * @return 0 si hemos pulsado S para jugar otra partida; -1 en caso contrario
	 */
	private static boolean jugarOtraPartida() {
		if (pideLetra("\n¿Quieres jugar otra partida? [S/N]: ") == 'S') {
			nuevaPartida();
			return true;
		} else
			return false;

	}

	/**
	 * Crea una nueva partida. Para ello inicializa las varibles palabra,
	 * palabraParcial, y pinta por consola la horca inicial y el patrón con guiones
	 * bajos de la palabra a descubrir
	 */
	private static void nuevaPartida() {
		borraConsola();
		palabraADescubrir = damePalabra();
		palabraEnConstruccion = new char[palabraADescubrir.length()];
		inicializaPalADescubrir(); // Rellena palabraEnConstrucción con '_'.
		letrasUtilizadas.clear(); // Limpia colección de letras utilizadas.
		letra = '_';
		contFallos = contTiradas = 0;
		pintaPalabraEnConstruccion();
		pintaHorca(0);
		pintaContadoresVidasYTiradas();
	}

	/**
	 * Evalua la jugada y escribe por consola mensaje de derrota o victoria en el
	 * caso de haber agotado el número de intentos o haber completado la palabra
	 * respectivamente.
	 * 
	 * @return true en caso de que la partida haya terminado, bien por derrota o
	 *         victoria. false en caso contrario (la partida continua).
	 */
	private static boolean partidaTerminada() {
		if (!acierto && contFallos == MAX_FALLOS) {
			System.out.println("Ooooh, fallaste");
			System.out.println("La palabra era: " + palabraADescubrir);
			return true;
		} else if (String.valueOf(palabraEnConstruccion).equals(palabraADescubrir)) {
			System.out.println("¡ENHORABUENA, has resuelto la palabra!");
			return true;
		} else
			return false;

	}

	/**
	 * Pide un caracter por consola.
	 * 
	 * @param t Texto a escribir por consola para pedir una letra.
	 * @return Caracter escrito por consola convertido a mayúscula.
	 */
	private static char pideLetra(String t) {
		System.out.print(t);
		sc.reset();
		// Si por error teclamos mas de una letra consecutiva, solo coje la primera,
		// y la convierte a mayuscula
		char c = sc.next().toUpperCase().charAt(0);

		return c;
	}

	/**
	 * Si la letra esta en palabraADescubrir la incluye en palabraEnConstruccion
	 * y la añade a letrasUtilizadas.
	 * Si la letra no está en palabraADescubir incrementa el contador de fallos
	 * Incrementa contador de tiradas.
	 * Repinta consola con los nuevos valores: contadores, horca, letras usadas
	 * 
	 */

	private static  void hazJugada() {
		boolean acierto = false;

		// Si la letra ya  ha sido utilizada no hace nada, solo repinta consola 
		if (!letrasUtilizadas.contains(letra)) {

			// Pone en palabraEnConstruccion los caracteres coincidentes en
			// palabraADescubrir

			for (int i = 0; i < palabraADescubrir.length(); i++) {
				if (palabraADescubrir.charAt(i) == letra) { // Si la letra está en palabraADescubrir
					palabraEnConstruccion[i] = letra; // la añade a palabraEnConstruccion
					acierto = true; // ha habido acierto
				}
			}

			letrasUtilizadas.add(letra); // Añade letra utilizada a la colección.
			contTiradas++;

			// Si letra no está en la palabra a descubrir incrementa contador de fallos.
			contFallos = acierto ? contFallos : contFallos + 1;
		}

		borraConsola();
		pintaPalabraEnConstruccion();
		pintaHorca(contFallos); // Repinta horca según número de fallos.
		pintaContadoresVidasYTiradas();
		pintaListaLetrasUsadas();

		
	}

	static private void inicializaPalADescubrir() {
		for (int i = 0; i < palabraEnConstruccion.length; i++)
			palabraEnConstruccion[i] = '_';
	}

	private static void pintaContadoresVidasYTiradas() {
		System.out.printf("Tiradas: %d\tVidas restantes: %d\n", contTiradas, MAX_FALLOS - contFallos);
	}

	private static void pintaPalabraEnConstruccion() {
		for (int i = 0; i < palabraEnConstruccion.length; i++)
			System.out.printf("%s ", palabraEnConstruccion[i]);
		System.out.println("\n");

	}

	private static void pintaHorca(int i) {
		System.out.println(imagenesAhorcado[i]);
	}

	/**
	 * Escribe 200 líneas en blanco para simular un borrado de consola
	 */
	private static void borraConsola() {

		System.out.print(saltos);
	}

	/**
	 * Devuelve una palabra del ArrayList palabras. La palabra corresponede a una
	 * posición aleatoria en el rango 0 - (n-1), donde n es el número de palabras
	 * almacenadas en el array PALABRAS
	 * 
	 * @return La palabra seleccionada aleatoriamente.
	 */
	private static String damePalabra() {
		int n = palabras.size();

		// Genero entero aleatorio entre 0 y n - 1;

		n = (int) (Math.random() * n);

		return palabras.get(n);

	}

	/**
	 * Pinta por consola la lista de letras que ya han sido utilizadas
	 */
	private static void pintaListaLetrasUsadas() {
		System.out.print("Letras usadas: ");
		System.out.println(letrasUtilizadas.toString());
	}

	private static boolean letraPermitida(char l) {
		if ((l >= '\u0041' && l <= '\u005A') || (l >= '\u0061' && l <= '\u007A') || l == '\u00D1' || l == '\u00F1') {
			return true;
		}
		return false;
	}

	static void cargaPalabras() {

		InputStream is = JuegoAhorcado.class.getResourceAsStream("/palabras.txt");

		/*
		 * Otra forma de obtener referencia al ficnero de recurso. Es decir, de hacer lo mismo de arriba 
		 * 
		 * ClassLoader loader = JuegoAhorcado.class.getClassLoader();
		 * InputStream is=null; 
		 * try { 
		 * 	is = loader.getResource("palabras.txt").openStream(); 
		 * } catch (IOException e) { 
		 * 	e.printStackTrace(); 
		 * }
		 */

		if (is != null) {
			palabras = new ArrayList<>();
			Scanner sc = new Scanner(is);

			while (sc.hasNextLine())
				palabras.add(sc.nextLine());

			sc.close();
		} else {
			System.out.println("Fichro de recurso no existe o no se puede abrir: palabras.txt");
		}

	}
	
	static void cargaMapasHorcas() {
		InputStream is = JuegoAhorcado.class.getResourceAsStream("/mapas_horcas.txt");
		if (is != null) {

			Scanner sc = new Scanner(is);
			StringBuilder linea = new StringBuilder();
			String str;
			int i = -1; // pos del array de horcas

			while (sc.hasNextLine()) {
				str = sc.nextLine();
				
				if (str.length() == 0) continue;

				if (str.charAt(0) == '@') {
					if(i != -1) {
						imagenesAhorcado[i] = linea.toString();
						linea.setLength(0);
					}
					
					i++;
				} else {
					linea.append(str).append('\n');
				}
			}
			imagenesAhorcado[i] = linea.toString();

			sc.close();
		} else {
			System.out.println("Fichro de recurso no existe o no se puede abrir: mapas_horcas.txt");
		}

	}

}
