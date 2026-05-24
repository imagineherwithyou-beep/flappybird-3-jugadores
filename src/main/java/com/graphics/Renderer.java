package com.graphics;

import org.lwjgl.opengl.GL11;//GL11 tiene las funciones basicas de OpenGL para dibujar formas y configurar el estado de renderizado
import org.lwjgl.opengl.GL13;//GL13 tiene funciones para trabajar con texturas, como activar unidades de textura y configurar parámetros de textura
import org.lwjgl.opengl.GL20;//GL20 tiene funciones para trabajar con shaders, como crear, compilar y usar programas de shaders
import org.lwjgl.opengl.GL30;//GL30 tiene funciones para trabajar con VAOs (Vertex Array Objects) y otras características avanzadas de OpenGL
import org.lwjgl.opengl.GL15;//GL15 tiene funciones para trabajar con VBOs (Vertex Buffer Objects) y otras operaciones de buffer
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

import org.lwjgl.stb.STBImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
public class Renderer {
    // Identificadores de recursos en la GPU
    private int programa; // El ID del programa de sombreadores (Shaders)
    private int vao; // Objeto que organiza los datos de los vértices
    private int vbo; // Buffer que contiene las coordenadas físicas
    private int uOffsetLocation; // Ubicación del uniforme para la posición (X, Y)
    private int uScaleLocation; // Ubicación del uniforme para el tamaño (Ancho, Alto)
    private int uColorLocation; // Ubicación del uniforme para el color (R, G, B)
    private int uRotacionLocation; // uniform para rotación
    private int vaoTriangulo; // VAO y VBO adicionales para el triángulo
    private int vboTriangulo;
    private int texturaGameOver;
  //  private int texturaStart;
    //private int texturaGanador;
    private int texturaTuberia;
    private int texturaTuberia1;
    private int texturaTitle;
    private int textureSpaceToStart;
    private int uTextureLocation;
    private int uUsarTexturaLocation;

    public Renderer() {
        crearShaders(); // Prepara los shaders
        crearQuadBase(); // Crea el cuadrado geométrico que usaremos como molde
      //  texturaStart = cargarTextura("res/titles/start.png");
        texturaGameOver = cargarTextura("res/titles/game-over.png");
        texturaTuberia = cargarTextura("res/titles/tuberia.png");
        texturaTuberia1 = cargarTextura("res/titles/tuberia1.png");
        texturaTitle = cargarTextura("res/titles/flappybirdTitle.png");
        textureSpaceToStart= cargarTextura("res/titles/SpaceToStart.png");
       // texturaGanador=cargarTextura("res/titles/1st_place.png");
    }

    private void crearShaders() {
        // Código fuente del Vertex Shader: procesa posiciones
        String vertexSrc = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                layout(location = 1) in vec2 aUV; 
                uniform vec2 uOffset;
                uniform vec2 uScale;
                uniform float uRotacion;
                 out vec2 vUV; 
                void main() {
                    float angulo = uRotacion;
                    float cosA = cos(angulo);
                    float sinA = sin(angulo);
                    vec2 rotado = vec2(
                    aPos.x * cosA - aPos.y * sinA,
                     aPos.x * sinA + aPos.y * cosA
                    );
                    // Multiplica el molde por la escala y suma el nuevo recorrido
                     vec2 finalPos = rotado * uScale + uOffset;
                    gl_Position = vec4(finalPos, aPos.z, 1.0);
                    vUV = aUV; //
                }
                """;

        // Código fuente del Fragment Shader: procesa colores
        String fragmentSrc = """
                #version 330 core
                in vec2 vUV;
                uniform sampler2D uTexture;
                out vec4 fragColor;
                uniform vec4 uColor;
                uniform bool usarTextura;
                void main() {

                 if(usarTextura) {
                 fragColor = texture(uTexture, vUV);
                  } else {
                   fragColor = uColor;
                  }

                }
                """;// Rasterazer calcula que fragmentos(pixeles) ,interpola u,v para crear nuestros
                    // fragmentos y el fragment shader asigna un color a cada fragmento usando la
                    // textura y las coordenadas interpoladas
                    // texture() es una funcion q recibe una textura(completa) y coordenadas UV

        // Compilación y vinculación de Shaders
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader); // Compila el código del vértice
        comprobarShader(vertexShader, "Vertex");

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);// Envía el código del color al shader
        GL20.glCompileShader(fragmentShader); // Compila el código del color
        comprobarShader(fragmentShader, "Fragment");

        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader); // Une ambos shaders
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa); // Crea el ejecutable final para la GPU

        if (GL20.glGetProgrami(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar programa: " + GL20.glGetProgramInfoLog(programa));
        }

        // Obtiene las direcciones de las variables Uniform del shader
        uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        uScaleLocation = GL20.glGetUniformLocation(programa, "uScale");
        uColorLocation = GL20.glGetUniformLocation(programa, "uColor");
        uRotacionLocation = GL20.glGetUniformLocation(programa, "uRotacion");
        uTextureLocation = GL20.glGetUniformLocation(programa, "uTexture");
        uUsarTexturaLocation = GL20.glGetUniformLocation(programa, "usarTextura");
        // Limpieza de shaders individuales (ya están en el programa)
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private void comprobarShader(int shader, String tipo) {
        // Verifica si hubo errores de sintaxis en el código GLSL de los shaders
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(tipo + " shader: " + GL20.glGetShaderInfoLog(shader));
        }
    }

    private void crearQuadBase() {
        // 1. Definición de la geometría en la CPU (RAM)
        float[] vertices = {

                // x y z u v

                -0.5f, -0.5f, 0.0f, 0f, 1f,
                0.5f, -0.5f, 0.0f, 1f, 1f,
                0.5f, 0.5f, 0.0f, 1f, 0f,

                -0.5f, -0.5f, 0.0f, 0f, 1f,
                0.5f, 0.5f, 0.0f, 1f, 0f,
                -0.5f, 0.5f, 0.0f, 0f, 0f
        };

        // 2. Gestión del VAO (El Organizador)
        vao = GL30.glGenVertexArrays(); // Pide a la GPU un ID libre para un nuevo "Organizador" (VAO)
        GL30.glBindVertexArray(vao); // "Activa" este VAO. Todo lo que configuremos abajo se guardará dentro de él.

        // 3. Gestión del VBO (El Almacén)
        vbo = GL15.glGenBuffers(); // Pide a la GPU un ID para un nuevo "Almacén" de datos (VBO)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo); // Selecciona este almacén para empezar a llenarlo

        // 4. Preparación de datos para la GPU
        // Creamos un buffer de memoria directa (fuera del control de Java) porque la
        // GPU no puede leer arreglos de Java directamente
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip(); // Metemos los números al buffer y lo preparamos (flip) para ser leído

        // Enviamos los datos al VBO de la GPU. Usamos GL_STATIC_DRAW porque el cuadrado
        // no cambiará de forma
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        // 5. El "Manual de Instrucciones" (Atributos)
        // Le decimos a OpenGL cómo interpretar los números que acabamos de subir:
        // - El índice 0 coincide con 'layout (location = 0)' en tu Vertex Shader.
        // - Usamos 3 números por vértice (X, Y, Z).
        // - Son de tipo FLOAT.
        // - El primer vértice empieza en el byte 0.
        // atributo posicion
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        // atributo UV
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        GL20.glEnableVertexAttribArray(1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // "Soltamos" el buffer para evitar cambios accidentales
        GL30.glBindVertexArray(0); // "Cerramos" el VAO. Ya guardó toda la configuración anterior.
        crearTrianguloBase();
    }

    // Método auxiliar para dibujar cualquier rectángulo configurando sus uniformes
    private void dibujarRectRotado(float x, float y, float ancho, float alto,
            float rotacion, float r, float g, float b) {
        // Llama al nuevo método con alpha = 1.0f
        dibujarRectRotado(x, y, ancho, alto, rotacion, r, g, b, 1.0f);
    }

    // Nuevo método (con alpha)
    private void dibujarRectRotado(float x, float y, float ancho, float alto, float rotacion, float r, float g, float b,
            float alpha) {
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vao);

        // Transformaciones
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform1f(uRotacionLocation, rotacion);

        // Color (con alpha para transparencia)
        GL20.glUniform4f(uColorLocation, r, g, b, alpha);
        GL20.glUniform1i(uUsarTexturaLocation, 0);
        // Dibujar 2 triángulos = 1 rectángulo
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

        // Limpiar
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }

    private void dibujarRectangulo(float x, float y, float ancho, float alto, float rotacion, float r, float g, float b,
            float alfa) {
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vao);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform1f(uRotacionLocation, rotacion);
        GL20.glUniform4f(uColorLocation, r, g, b, alfa);
        GL20.glUniform1i(uUsarTexturaLocation, 0);
        // Aquí se podría agregar un uniforme adicional para el alfa si el shader lo
        // soportara
        // Por simplicidad, vamos a ignorar el alfa en este ejemplo, pero se podría
        // modificar el shader para usarlo.
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }
    // El pájaro compuesto
  private void dibujarPajaros(Bird bird1, Bird bird2, Bird bird3) {
    dibujarPajaro(bird1, 0.98f, 0.85f, 0.20f); // Amarillo
    dibujarPajaro(bird2, 0.20f, 0.50f, 0.95f); // Azul
    dibujarPajaro(bird3, 1.0f, 1.0f, 1.0f);   // Blanco (Jugador 3)
}
 

private void dibujarPajaro(Bird bird, float r, float g, float b) {
    float x = Bird.BIRD_X;
    float y = bird.getY();
    float rot = bird.getInclinacion();
    float anguloAla = bird.getAnguloAla();
    // 1. Calculamos Seno y Coseno UNA VEZ (Operación súper ligera)
    // Esto calculará la órbita de todas las piezas alrededor del cuerpo central
    float cosRot = (float) Math.cos(rot);
    float sinRot = (float) Math.sin(rot);

    // CUERPO (Este se queda igual, es el centro del universo del pájaro)
    dibujarRectRotado(x, y, 0.11f, 0.11f, 0.95f +rot, r, g, b, 1f);

    // PICO (Distancias originales: dx = 0.065f, dy = 0.02f)
    float picoX = x + (0.065f * cosRot - 0.02f * sinRot);
    float picoY = y + (0.065f * sinRot + 0.02f * cosRot);
    dibujarTrianguloRotado(picoX, picoY, 0.05f, 0.035f, rot, 1f, 0.5f, 0f, 1f);

    // OJO (Distancias originales: dx = 0.045f, dy = 0.028f)
    float ojoX = x + (0.045f * cosRot - 0.028f * sinRot);
    float ojoY = y + (0.045f * sinRot + 0.028f * cosRot);
    dibujarRectRotado(ojoX, ojoY, 0.021f, 0.021f, rot, 1f, 1f, 1f, 1f);

    // PUPILA (Distancias originales: dx = 0.048f, dy = 0.028f)
    float pupilaX = x + (0.048f * cosRot - 0.028f * sinRot);
    float pupilaY = y + (0.048f * sinRot + 0.028f * cosRot);
    dibujarRectRotado(pupilaX, pupilaY, 0.008f, 0.008f, rot, 0f, 0f, 0f, 1f);
    // ALA (Distancias originales: dx = -0.02f, dy = -0.02f + offsetAla)
    float offsetAla = (float) Math.sin(anguloAla) * 0.03f;
    float dyAla = -0.02f + offsetAla;
    float alaX = x + (-0.02f * cosRot - dyAla * sinRot);
    float alaY = y + (-0.02f * sinRot + dyAla * cosRot);
    dibujarTrianguloRotado(alaX, alaY, 0.09f, 0.04f, rot + 0.3f, r * 0.7f, g * 0.7f, b * 0.7f, 1f);

    // COLA (Distancias originales: dx = -0.067f, dy = -0.01f)
    float colaX = x + (-0.07f * cosRot - (-0.01f) * sinRot);
    float colaY = y + (-0.07f * sinRot + (-0.01f) * cosRot);
    dibujarTrianguloRotado(colaX, colaY, 0.095f, 0.035f, rot - 0.65f, r * 0.6f, g * 0.5f, b * 0.3f, 1f);
}
    private void crearTrianguloBase() {
        // Vértices de un triángulo equilátero centrado en origen
        float[] vertices = {
                -0.3f, 0.5f, 0.0f, // vértice superior
                -0.3f, -0.5f, 0.0f, // vértice inferior izquierdo
                0.5f, -0.25f, 0.0f // vértice inferior derecho
        };

        vaoTriangulo = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoTriangulo);

        vboTriangulo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTriangulo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void dibujarTrianguloRotado(float x, float y, float ancho, float alto, float rotacion, float r, float g,
            float b) {
        dibujarTrianguloRotado(x, y, ancho, alto, rotacion, r, g, b, 1.0f);
    }

    private void dibujarTrianguloRotado(float x, float y, float ancho, float alto,
            float rotacion, float r, float g, float b, float alfa) {
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vaoTriangulo);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform1f(uRotacionLocation, rotacion);
        GL20.glUniform4f(uColorLocation, r, g, b, alfa); // Asegúrate de que el shader soporte el alfa si lo usas
        GL20.glUniform1i(uUsarTexturaLocation, 0);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3); // 3 vértices = 1 triángulo
    }

    private void dibujarFondoDegradado() {
        // Colores: arriba (Y = +1.0) a abajo (Y = -1.0)
        float rArriba = 0.2f; // Componente rojo arriba
        float gArriba = 0.5f; // Componente verde arriba
        float bArriba = 0.8f; // Componente azul arriba

        float rAbajo = 0.6f; // Componente rojo abajo
        float gAbajo = 0.9f; // Componente verde abajo
        float bAbajo = 1.0f; // Componente azul abajo

        int numeroFranjas = 100; // Más franjas = degradado más suave
        float altoFranja = 2.0f / numeroFranjas; // Alto total = 2.0 (de -1 a +1)

        // Dibujar de arriba hacia abajo
        for (int i = 0; i < numeroFranjas; i++) {
            // Calcular Y del centro de esta franja
            // Primera franja: Y = +1.0 - (altoFranja/2)
            // Última franja: Y = -1.0 + (altoFranja/2)
            float y = 1.0f - (i * altoFranja) - (altoFranja / 2.0f);

            // Calcular t (progreso de 0 a 1, donde 0 = arriba, 1 = abajo)
            float t = (float) i / (numeroFranjas - 1);

            // Interpolar colores
            float r = rArriba + (rAbajo - rArriba) * t;
            float g = gArriba + (gAbajo - gArriba) * t;
            float b = bArriba + (bAbajo - bArriba) * t;

            // Dibujar la franja
            dibujarRectRotado(0.0f, y, 2.0f, altoFranja, 0f, r, g, b);
        }
    }

    private void dibujarSuelo() {
        float sueloAncho = 2.0f; // Cubre todo el ancho

        // ===== 1. CAPA INFERIOR (tierra profunda) =====
        float tierraY = -0.925f;
        float tierraAlto = 0.15f;
        dibujarRectRotado(0.0f, tierraY, sueloAncho, tierraAlto, 0f, 0.35f, 0.25f, 0.15f);

        // ===== 2. CAPA MEDIA (tierra más clara) =====
        float tierraMediaY = -0.88f;
        float tierraMediaAlto = 0.03f;
        dibujarRectRotado(0.0f, tierraMediaY, sueloAncho, tierraMediaAlto, 0f, 0.5f, 0.5f, 0.0f);

        // ===== 3. CÉSPED (verde) =====
        float cespedY = -0.84f;
        float cespedAlto = 0.07f;
        dibujarRectRotado(0.0f, cespedY, sueloAncho, cespedAlto, 0f, 0.25f, 0.65f, 0.25f);

        // ===== 4. LÍNEA NEGRA SUPERIOR DEL CÉSPED =====
        float lineaSuperiorY = -0.805f;
        float lineaAlto = 0.0065f;
        dibujarRectRotado(0.0f, lineaSuperiorY, sueloAncho, lineaAlto, 0f, 0.3f, 0.2f, 0.2f);

        // ===== 5. LÍNEA NEGRA INFERIOR DEL CÉSPED =====
        float lineaInferiorY = -0.875f;
        dibujarRectRotado(0.0f, lineaInferiorY, sueloAncho, lineaAlto, 0f, 0.3f, 0.2f, 0.2f);

        // ===== 6. DETALLES OPCIONALES (manchas en el césped) =====
        for (float x = -0.9f; x < 1.0f; x += 0.25f) {
            // Manchas verde oscuro en el césped
            dibujarRectRotado(x, -0.84f, 0.04f, 0.02f, 0f, 0.25f, 0.8f, 0.15f);
        }
    }

    public void dibujarPantallaInicio(float tiempo) {// aumentar
        // 1. Fondo oscuro transparente
        dibujarRectRotado(0.0f, 0.0f, 2.0f, 2.0f, 0f, 0.0f, 0.0f, 0.0f, 0.7f);

        // 2. Animación vertical del título principal
        float animacionY = (float) Math.sin(tiempo * 3.0f) * 0.03f;

        dibujarRectRotado(0.0f, 0.65f + animacionY, 1.4f, 0.25f, 0f, 0.98f, 0.85f, 0.20f, 1.0f);
        dibujarTextura(0.0f, 0.65f + animacionY,1.2f,0.20f, 0, texturaTitle);//flappyBird text
        // 3. BOTÓN VERDE START (con pulsación)
        //float pulsacion = (float) Math.sin(tiempo * 5.0f) * 0.02f;
         dibujarTextura(0.0f, 0f, 0.6f +  animacionY, 0.23f, 0.0f, textureSpaceToStart);//spacetostart
        dibujarRectRotado(0.0f, -0.24f, 0.8f + animacionY, 0.10f +  animacionY, 0f, 0.15f, 0.40f, 0.18f, 1.0f);
        dibujarRectRotado(0.0f, -0.24f, 0.75f +  animacionY, 0.07f +  animacionY, 0f, 0.22f, 0.76f, 0.25f, 1.0f);
       // dibujarTextura(0.0f, -0.24f, 0.5f + pulsacion, 0.12f, 0.0f, texturaStart);//start text
    }

    public void dibujarPantallaGameOver(float tiempo,Bird bird1,Bird bird2,Bird bird3,int puntaje1,int puntaje2,int puntaje3) {
        dibujarRectRotado(0.0f, 0.0f, 2.0f, 2.0f, 0f, 0.0f, 0.0f, 0.0f, 0.3f);
        float animacionY = (float) Math.sin(tiempo * 3.0f) * 0.04f;
        dibujarRectRotado(0.0f, 0.0f + animacionY, 1.8f, 0.4f, 0f, 0.0f, 0.0f, 0.0f, 0.8f);
        dibujarRectRotado(0.035f, -0.035f + animacionY, 1.8f, 0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.6f);
        dibujarTextura(0.0f, 0.0f + animacionY, 1.5f, 0.3f, 0.0f, texturaGameOver);
    }

    private void dibujarTextura(float x, float y, float ancho, float alto, float rotacion, int Textura) {
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vao);

        // Transformaciones
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, -alto);
        GL20.glUniform1f(uRotacionLocation, rotacion);

        // Activar textura en el shader
        GL20.glUniform1i(uUsarTexturaLocation, 1);

        // Activar unidad de textura 0
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Textura);
        GL20.glUniform1i(uTextureLocation, 0);
        // Dibujar 2 triángulos = 1 rectángulo
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

        // Limpiar
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }

    public void render(Bird bird1, Bird bird2, Bird bird3, List<Tuberia> tuberias, boolean gameOver, boolean started, float tiempo, int puntaje1, int puntaje2, int puntaje3) {

    GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f); // configurar el color para q este listo
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT); // limpia la pantalla con el color de fondo configurado

    dibujarFondoDegradado();
    // dibujarMontanas();
    dibujarSuelo();
    
    // Tuberías
    for (Tuberia t : tuberias) {
        float gapTop = t.gapCentroY + (Tuberia.GAP_ALTO * 0.5f); // calcular la posición del borde superior del gap (centro + mitad del gap)
        float gapBottom = t.gapCentroY - (Tuberia.GAP_ALTO * 0.5f); // calcular la posición del borde inferior del gap

        float altoSuperior = 1.0f - gapTop; // altura del rectangulo de arriba
        if (altoSuperior > 0.0f) { // verificar que el rectángulo superior tenga altura positiva antes de dibujar
            float yCentroSup = gapTop + (altoSuperior * 0.5f);
            dibujarRectRotado(t.x, yCentroSup, Tuberia.ANCHO, altoSuperior, 0f, 0.18f, 0.70f, 0.25f);
            dibujarTextura(t.x, yCentroSup, Tuberia.ANCHO, altoSuperior, 0, texturaTuberia);
        }

        float altoInferior = gapBottom + 1.0f; // altura del rectagulo de abajo
        if (altoInferior > 0.0f) { // verificar que el rectángulo inferior tenga altura positiva
            float yCentroInf = -1.0f + (altoInferior * 0.5f);
            dibujarRectRotado(t.x, yCentroInf, Tuberia.ANCHO, altoInferior, 0f, 0.18f, 0.70f, 0.25f);
            dibujarTextura(t.x, yCentroInf, Tuberia.ANCHO, altoInferior, 0, texturaTuberia1);
        }
    }

    // Dibujar los tres pájaros en pantalla
    dibujarPajaros(bird1, bird2, bird3); 

    if (!started && !gameOver) {
        dibujarPantallaInicio(tiempo);
    }
    
    if (gameOver) {
        // Pantalla de Game Over con los 3 pájaros y sus 3 puntajes
        dibujarPantallaGameOver(tiempo, bird1, bird2, bird3, puntaje1, puntaje2, puntaje3);
    }
}

    private int cargarTextura(String ruta) {
        int textura;
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer canales = BufferUtils.createIntBuffer(1);

        STBImage.stbi_set_flip_vertically_on_load(true);

        ByteBuffer imagen = STBImage.stbi_load(
                ruta,
                w,
                h,
                canales,
                4);

        if (imagen == null) {
            throw new RuntimeException(
                    "No se pudo cargar textura: " + ruta);
        }

        textura = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textura);

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER,
                GL11.GL_LINEAR);

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER,
                GL11.GL_LINEAR);

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,
                w.get(),
                h.get(),
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                imagen);

        STBImage.stbi_image_free(imagen);
        return textura;
    }

    public void cleanup() {
        // Libera los recursos de la GPU al cerrar el programa
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vaoTriangulo); //
        GL15.glDeleteBuffers(vboTriangulo); //
        GL20.glDeleteProgram(programa);

    }
   
}
