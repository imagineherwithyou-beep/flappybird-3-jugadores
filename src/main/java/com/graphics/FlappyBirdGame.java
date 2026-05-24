package com.graphics;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class FlappyBirdGame {
    private static final int ANCHO = 1100;
    private static final int ALTO = 900;

    private long window;
    private Renderer renderer;
    private Bird bird;
    private Bird bird2; // para el modo 2 jugadore
     private Bird bird3; // para el modo 2 jugadore
    private GameLogic gameLogic;
    private float ultimoTiempo;

    public void run() {
        init();
        loop();
        cleanup();
    }
   //Creamos la ventaja e inicializamos los objetos que se van a usar:renderer, birds y gamelogic con los birds instanciados
   //+tomamos el tiempo actual para usarlo en el bucle principal
    private void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo iniciar GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);//
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Flappy Bird OpenGL", 0, 0);
        if (window == 0) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glBlendFunc(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA);
        renderer = new Renderer();
        bird = new Bird();
        bird2 = new Bird();
        bird3 = new Bird();
        gameLogic = new GameLogic(bird, bird2,bird3);
        ultimoTiempo = (float) GLFW.glfwGetTime();
    }
     
// El bucle principal del juego . Primero recibe los inputs,luego actualiza en gamelogic
//  y recien hace render pasandole los birds y el estado del juego
    private void loop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            float ahora = (float) GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;

            if (dt > 0.033f) {
                dt = 0.033f;
            }

            procesarInput(); 
            gameLogic.actualizar(dt);
            
            // Calculamos el estado de Game Over con los 3 jugadores
            boolean gameOver = gameLogic.isGameOver(gameLogic.EstadoPlayer1(), gameLogic.EstadoPlayer2(), gameLogic.EstadoPlayer3());
            
            // Pasar el tiempo para animaciones y los datos de los 3 pájaros
            renderer.render(
                bird, bird2, bird3, 
                gameLogic.getTuberias(), 
                gameOver,
                gameLogic.isStarted(), 
                ahora, 
                gameLogic.getPuntaje(), 
                gameLogic.getPuntaje2(), 
                gameLogic.getPuntaje3()
            ); 
            
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }
    // metodo que recibe los inputs de teclado saltos(W,SPACE,UP) Y REINICIO R 
    private void procesarInput() {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        boolean saltoPlayer1 = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        boolean saltoPlayer2 = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
        boolean saltoPlayer3 = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS; // Tecla W para el blanco
        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        
        // Se le pasan los 3 pájaros y los 3 booleanos a la lógica
        gameLogic.procesarInput(bird, bird2, bird3, saltoPlayer1, saltoPlayer2, saltoPlayer3, rAhora);
        actualizarTitulo();
    }
    //actualiza el titulo de la ventana usando los puntajes y el nivel que contiene gamelogic
    private void actualizarTitulo() {
        String tituloBase = "Flappy Bird OpenGL | NIVEL :  " + gameLogic.getNivel() 
                + " | P1: " + gameLogic.getPuntaje() 
                + "  |  P2: " + gameLogic.getPuntaje2()
                + "  |  P3: " + gameLogic.getPuntaje3(); // Agregado P3 al título
                
        boolean gameOver = gameLogic.isGameOver(gameLogic.EstadoPlayer1(), gameLogic.EstadoPlayer2(), gameLogic.EstadoPlayer3());

        if (!gameLogic.isStarted()) {
            GLFW.glfwSetWindowTitle(window, tituloBase + " | SPACE para empezar");
        } else if (gameOver) {
            GLFW.glfwSetWindowTitle(window, tituloBase + " | GAME OVER - SPACE o R para reiniciar");
        } else {
            GLFW.glfwSetWindowTitle(window, tituloBase);
        }
    }

    private void cleanup() {
        renderer.cleanup();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public static void main(String[] args) {
        new FlappyBirdGame().run();
    }
}