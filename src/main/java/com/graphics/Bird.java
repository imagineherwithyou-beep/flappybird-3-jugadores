package com.graphics;

public class Bird {
    // Posición fija en X
    public static final float BIRD_X = -0.45f;
    public static final float BIRD_ANCHO = 0.1f;
    public static final float BIRD_ALTO = 0.1f;
    private static final float GRAVEDAD = -1.9f;
    private static final float IMPULSO_SALTO = 0.9f;
    private static final float VELOCIDAD_MAX_CAIDA = -1.8f;
    private float birdy;
    private float velY;
        // Para animación y rotación
    private float inclinacion;          // rotación del pájaro
    private float anguloAla;            // ángulo del ala
    private float tiempoAla;            // acumulador para animación
    
    public Bird() {//posición inicial del pájaro
        this.birdy = 0.0f;
        this.velY = 0.0f;
    }
    
    public void saltar() {
        velY = IMPULSO_SALTO  ;// impulso hacia arriba basado 
          // Refuerzo visual del aleteo al saltar
        anguloAla = -0.9f;
        tiempoAla = 0;
    }
    //
    public void actualizarFisica(float dt,int nivel) {
    velY += GRAVEDAD * dt;//reduce la velocidad cada vez
    if (velY < VELOCIDAD_MAX_CAIDA) {
        velY = VELOCIDAD_MAX_CAIDA;
    }
    //if(nivel>=2)
   //     birdy +=  2* dt;  
    birdy += velY * dt;

    // inclinación basada en velocidad

    inclinacion = velY * 0.7f;

    // limite
    if (inclinacion > 0.5f) {
        inclinacion = 0.5f;
    }
    if (inclinacion < -0.7f) {
        inclinacion = -0.7f;
    }
    tiempoAla += dt * 5.0f;
    anguloAla =

(float) Math.sin(tiempoAla) * 0.9f;

} 
    
    public boolean colisionaConBordes() {//verificar colisión con los bordes superior e inferior
        float birdTop = birdy + (BIRD_ALTO * 0.5f);
        float birdBottom = birdy - (BIRD_ALTO * 0.5f);
        return birdTop >= 1.0f || birdBottom <= -1.0f;
    }
    
    public boolean colisionaConTuberia(Tuberia tuberia) {
        float birdLeft = BIRD_X - (BIRD_ANCHO * 0.5f);
        float birdRight = BIRD_X + (BIRD_ANCHO * 0.5f);
        float birdBottom = birdy - (BIRD_ALTO * 0.5f);
        float birdTop = birdy + (BIRD_ALTO * 0.5f);
        
        float pipeLeft = tuberia.x - (Tuberia.ANCHO * 0.5f);
        float pipeRight = tuberia.x + (Tuberia.ANCHO * 0.5f);
        boolean overlapX = birdRight > pipeLeft && 
                           birdLeft < pipeRight;
        
        if (!overlapX) {
            return false;
        }
        
        float gapTop = tuberia.gapCentroY + (Tuberia.GAP_ALTO * 0.5f);
        float gapBottom = tuberia.gapCentroY - (Tuberia.GAP_ALTO * 0.5f);
        return birdTop > gapTop || birdBottom < gapBottom;
    }
    
    // Getters
    public float getY() { return birdy; }
    public float getInclinacion() { return inclinacion; }
    public float getAnguloAla() { return anguloAla; }

    public void reset() {
        birdy = 0.0f;
        velY = 0.0f;
                inclinacion = 0.0f;
        anguloAla = 0.0f;
        tiempoAla = 0.0f;
    }
}