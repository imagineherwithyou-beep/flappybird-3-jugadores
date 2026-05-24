
** ESTUDIANTE: EDDUIN ESPINOZA BARRIOS
-------------------------------------------------------------
**CONTROLES: 
     
*JUGADORES           *Tecla
                     |
                     |
*JUGADOR1 (AMARILLO):| SPACE
*JUGADOR2 (AZUL)    :|  FLECHA ARRRIBA (key up)
*JUGADOR3 (BLANCO)  :|  W
--------------------------------------------------------------
## Compilación y Ejecución

Para ejecutar el proyecto, asegúrate de tener instalado **JDK 17+** y **Maven**. Ejecuta estos comandos desde la terminal en la raíz del proyecto:
### 1. Compilar y preparar dependencias
```bash
mvn clean compile dependency:copy-dependencies
java -cp "target/classes;target/dependency/*" com.graphics.FlappyBirdGame
------------------------------------------------------------------------------
****CAMBIOS DEL PROYECTO BASE****
1.Dividi el proyecto en 5 clases: Renderer,Gamelogic,Bird,Tuberias,ApFlappy.
Renderer : Clase para hacer todo el dibujado , usando un shader, 2 figuras Quad y un Triangulo para las Alas
 // Quad modificado para recibir coordenadas UV para usar textura
 //En el vertexShader aumente 1 uniform para recibir rotación para rotar la figura usando Formula de Rotación 2D, 
  luego un atributo aUV que recibe de los vértices del quad para al fragmemt shader
Fragment ahora recibe cordenadas uv de vertex. Agrege un variable booleana para controlar si va a pintar el pixel de una textura o usando Ucolor

GameLogic :Controla la jugabilidad del juego . 
Recibe los birds y crea la lista de tuverias .Actualiza los movimientos de los birds y tuberías.
 *Agrege un método para actualizar dificultad basado en el puntaje ,aumenta un 20% por cada 5 tuberías.

Bird:Contiene el estado actual de si mismo + modifique actualizarfisica para que reciba el nivel actual de gamelogic.
Contiene método de para saber sus coliciones con tuberías y fuera de limites
Tuberias: Ahora tiene atributo de puntuadas.

AppFlappy: Instancia gamelogic y los 3 pájaros. Loop principal actualiza inputs teclados, actuliza gamelogic y luego renderisa(render recibe 3 pájaros y sus estados).
             














