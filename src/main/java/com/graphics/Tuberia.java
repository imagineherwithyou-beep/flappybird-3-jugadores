
package com.graphics;

public class Tuberia {
    public static final float ANCHO = 0.18f;
    public static final float GAP_ALTO = 0.48f; // altura del espacio entre las tuberías donde pasan los birds
    //public static final float VELOCIDAD = 0.62f;
     
    public float x;
    public float gapCentroY;
    public boolean puntuada;
     public  boolean Puntuada2;
      public  boolean Puntuada3;
    
    public Tuberia(float x, float gapCentroY) {
        this.x = x;
        this.gapCentroY = gapCentroY;
        this.puntuada = false;
        this.Puntuada2 = false;
    }
    
    public void mover(float dt, float velocidadTuberias) {
        x -= velocidadTuberias * dt;
    }
    
    public boolean estaFueraDePantalla() {
        return x + (ANCHO * 0.5f) < -1.3f;
    }
    
    public boolean estaDetrasDe(Bird bird) {
        return x + (ANCHO * 0.5f) < Bird.BIRD_X ;
    }
    public boolean getpuntuada() {
        return puntuada;
    }
     public boolean getPuntuada2() {
        return Puntuada2;
    }
    public boolean getPuntuada3() {
        return Puntuada3;
    }

}
