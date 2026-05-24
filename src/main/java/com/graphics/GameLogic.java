package com.graphics;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;


public class GameLogic {
private static final float GAP_MIN_CENTRO = -0.45f;
    private static final float GAP_MAX_CENTRO = 0.45f;
    private float tiempoEntreTuberias;
    private float velocidadTuberias;
    private int nivel;
    private static final float TIEMPO_BASE = 1.5f;
    private static final float VELOCIDAD_BASE = 0.5f;
    private Bird bird;
    private Bird bird2;
    private Bird bird3;
    private List<Tuberia> tuberias;
    private Random random;
    private float timerSpawn;
    private int puntaje;
    private int puntaje2;
    private int puntaje3;
    private boolean started;
    private boolean gameOver;
    private boolean player1;
    private boolean player2;
    private boolean player3;
    private boolean prevSpace;
    private boolean prevUp;
    private boolean prevW;
    private boolean prevR;


     // Recibe los 3 pajaros 
    public GameLogic(Bird bird, Bird bird2, Bird bird3) {
       this.bird = bird;
        this.bird2 = bird2;
          this.bird3 = bird3;
        this.tuberias = new ArrayList<>();
        this.random = new Random();
        resetGame();
    }
//reincia los estados del juego a los valores iniciales para empezar una nueva partida
    public void resetGame() {
        bird.reset();
        bird2.reset();
        bird3.reset();
        timerSpawn = 0.0f;
        puntaje = 0;
        puntaje2 = 0;
         puntaje3 = 0;
        started = false;
        gameOver = false;
        tuberias.clear();
        prevSpace = false;
        prevUp = false;
        prevR = false;
        prevW = false;
        player1 = true;
        player2 = true;
         player3 = true;
        nivel = 1;
        tiempoEntreTuberias = TIEMPO_BASE;
        velocidadTuberias = VELOCIDAD_BASE;
    }
    /**
     * Procesa las entradas del teclado (inputs) para ambos jugadores de forma independiente.
     * Controla el salto de los pájaros, el inicio del juego y el reinicio tras un Game Over.
     * Usa banderas "prev" para evitar que el pájaro vuele infinito si dejas la tecla apretada
     */
    public void procesarInput(Bird bird, Bird bird2,Bird bird3, boolean saltoAhoraPlayer1, boolean saltoAhoraPlayer2,boolean saltoAhoraPlayer3, boolean rAhora) {

        // --- CONTROL JUGADOR 1 (TECLA ESPACIO) ---
        // Si SPACE está presionado AHORA y NO estaba presionado en el frame anterior, es un "Click" nuevo
        if (saltoAhoraPlayer1 && !prevSpace) {       
            if (gameOver) {

                // Si el juego terminó, cualquier salto reinicia la partida completa
                resetGame();
                started = true;
                reproducirSonido("res/sonido/jump.wav");
                bird.saltar();
                bird2.saltar();
            } else {
                // Si la partida no ha empezado, el primer click la arranca
                started = true;
                if (player1) { // Solo salta si el Player 1 sigue vivo (capo)
                    reproducirSonido("res/sonido/jump.wav");
                    bird.saltar();
             }
            }
        }

        if (saltoAhoraPlayer2 && !prevUp) {
            if (gameOver) {
                // Al igual que P1, si hay Game Over, reestablece todo
                resetGame();
              started = true;
                reproducirSonido("res/sonido/jump.wav");
                bird.saltar();
                bird2.saltar();bird3.saltar();
            } else {
                started = true;
                if (player2) { // Solo salta si el Player 2 sigue vivo
                    reproducirSonido("res/sonido/jump.wav");
                    bird2.saltar();    
                }
            }
        }

        if (saltoAhoraPlayer3 && !prevW) {
            if (gameOver) {
                // Al igual que P1, si hay Game Over, reestablece todo
                resetGame();
                started = true;
                reproducirSonido("res/sonido/jump.wav");
                bird.saltar();
                bird2.saltar();
                bird3.saltar();
            } else {
                started = true;
                if (player3) { // Solo salta si el Player 2 sigue vivo
                    reproducirSonido("res/sonido/jump.wav");
                    bird3.saltar();
             
                }
            }
        }
        // Guarda los estados de este frame para usarlos como "historial" en el próximo frame
        prevUp = saltoAhoraPlayer2;
        prevSpace = saltoAhoraPlayer1;
        prevW = saltoAhoraPlayer3;
        // --- CONTROL REINICIO ALTERNATIVO (TECLA R) ---

        // Detecta si R recién fue presionada y el juego ya se perdió estructuralmente

        if (rAhora && !prevR && gameOver) {

            resetGame();
        }
        // Guarda el estado del historial para la tecla R
        prevR = rAhora;
    }
    /**

     * Actualiza toda la lógica interna del juego en cada fotograma (física, tuberías, colisiones, puntajes).

     * usa parametro deltatime El tiempo que pasó desde el último frame, garantiza velocidad constante.

     */

    public void actualizar(float dt) {


        if (!started || gameOver) {
            return;
        }
        bird.actualizarFisica(dt,nivel);

        bird2.actualizarFisica(dt,nivel);

        bird3.actualizarFisica(dt,nivel);
        if (bird.colisionaConBordes()) {
        player1 = false; // Player 1 muere
        }
        if (bird2.colisionaConBordes()) {
            player2 = false; // Player 2 muere
        }
     if (bird3.colisionaConBordes()) {
            player3 = false; // Player3 muere
        }
        if (isGameOver(player1, player2,player3))
            return;
        timerSpawn += dt;
        if (timerSpawn >= tiempoEntreTuberias) {
            timerSpawn = 0.0f; // Reinicia el cronómetro
            spawnTuberia();    // Lanza un nuevo par de tubos
     }

        // 4. Bucle principal para mover, puntuar y verificar colisiones tubo por tubo
        Iterator<Tuberia> it = tuberias.iterator();
        while (it.hasNext()) {
           Tuberia tuberia = it.next();
            // Recalcula dinámicamente la velocidad según el puntaje actual
            actualizarDificultad();
            // Mueve la tubería hacia la izquierda
            tuberia.mover(dt, velocidadTuberias);
            // Si el tubo cruzó al P1, no fue sumado antes y el P1 está vivo: da punto

            if (tuberia.estaDetrasDe(bird) && !tuberia.getpuntuada() && EstadoPlayer1()) {
                tuberia.puntuada = true; // Bloquea el tubo para que no sume infinitos puntos
                puntaje++;
            }
            // Mismo control exacto para el Player 2

            if (tuberia.estaDetrasDe(bird2) && !tuberia.getPuntuada2() && EstadoPlayer2()) {
                tuberia.Puntuada2 = true;
                puntaje2++;
            }

             if (tuberia.estaDetrasDe(bird3) && !tuberia.getPuntuada3() && EstadoPlayer3()) {
                tuberia.Puntuada3 = true;
                puntaje3++;
            }
            // --- DETECCIÓN DE COLISIONES CON LOS TUBOS ---
            if (bird.colisionaConTuberia(tuberia)) {
                player1 = false; // El P1 choca y muere
            }
            if (bird2.colisionaConTuberia(tuberia)) {
                player2 = false; // El P2 choca y muere
            }
             if (bird3.colisionaConTuberia(tuberia)) {
              player3 = false; // El P3 choca y muere
            }
            // Si tras estos choques mueren ambos, cancela el procesamiento para optimizar recursos de la GPU

            if (isGameOver(player1, player2,player3))
                return;
            // 5. Limpieza de memoria: si el tubo ya salió por la izquierda de la pantalla, se destruye

            if (tuberia.estaFueraDePantalla()) {
                it.remove();//borra la tuveria que se esta checkeando
            }
        }

    }
    /**

     * Genera un nuevo obstáculo de tuberías en el borde derecho de la pantalla.

     * Calcula una abertura (GAP) aleatoria para que el espacio de paso cambie de altura.

     */

    private void spawnTuberia() {

        // Elige una altura del centro del hueco de forma aleatoria usando un rango delimitado

        float gapCentro = GAP_MIN_CENTRO + random.nextFloat() * (GAP_MAX_CENTRO - GAP_MIN_CENTRO);
        tuberias.add(new Tuberia(1.2f, gapCentro));
    }

    /**
     * Ajusta la dificultad del juego de forma progresiva según el rendimiento de los jugadores.
     * Controla el escalado de niveles, velocidad de avance y frecuencia de generación.
     */
    private void actualizarDificultad() {
        int puntajeMax = Math.max(puntaje, puntaje2);
        nivel = 1 + (puntajeMax / 5);    
        if (nivel > 5) {
        nivel = 5;
        }
        velocidadTuberias = VELOCIDAD_BASE + (nivel - 1) * 0.20f;
        tiempoEntreTuberias = Math.max(0.7f, TIEMPO_BASE - (nivel - 1) * 0.15f);
    }

    /**
     * Carga y reproduce archivos de sonido en formato WAV de forma asíncrona.
     * Utiliza la API nativa de Java Sound (AudioSystem y Clip).
     */
    private void reproducirSonido(String ruta) {
        try {
            File archivo = new File(ruta);
            // Abre el flujo de datos del archivo de audio
            AudioInputStream audio = AudioSystem.getAudioInputStream(archivo);
            Clip clip = AudioSystem.getClip();
            // Carga los bytes en la memoria intermedia de sonido y lo reproduce inmediatamente
            clip.open(audio);
            clip.start();
        } catch (Exception e) {

            // Si el archivo no existe o está corrupto, imprime el error sin colgar el hilo gráfico principal
            e.printStackTrace();
        }
    }
    // Getters
    public int getPuntaje() {
        return puntaje;
    }
    public int getPuntaje2() {
        return puntaje2;
    }

public int getPuntaje3() {

        return puntaje3;

    }

    public boolean isStarted() {

        return started;

    }
    public boolean isGameOver(Boolean player1, Boolean player2, Boolean player3) {

        if (!player1 && !player2&&!player3) {
            gameOver = true;
        }
        return gameOver;
    }

    public List<Tuberia> getTuberias() {
        return tuberias;
    }

    public boolean EstadoPlayer1() {
        return player1;
    }

   public boolean EstadoPlayer2() {
        return player2;
    }
    public boolean EstadoPlayer3() {
        return player3;
    }

    public int getNivel() {
        return nivel;
    }

}

