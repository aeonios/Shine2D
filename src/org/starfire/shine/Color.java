package org.starfire.shine;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by haplo on 12/15/2015.
 */
public class Color {
    boolean grayscale = false;
    boolean hasAlpha = false;
    float red;
    float green;
    float blue;
    float alpha;
    double gray;

    public Color(float r, float g, float b){
        if (r > 1 || g > 1 || b > 1){
            throw new IllegalArgumentException("Color value outside of 0.0-1.0 range!");
        }
        red = r;
        green = g;
        blue = b;
        alpha = 1.0f;
    }

    public Color(float r, float g, float b, float a){
        if (r > 1 || g > 1 || b > 1 || a > 1){
            throw new IllegalArgumentException("Color value outside of 0.0-1.0 range!");
        }
        red = r;
        green = g;
        blue = b;
        alpha = a;
        hasAlpha = true;
    }

    public Color(double g){
        if (g > 1){
            throw new IllegalArgumentException("Color value outside of 0.0-1.0 range!");
        }
        grayscale = true;
        gray = g;
    }

    public float getRed(){
        if (!grayscale){
            return red;
        }else{
            return (float) gray;
        }
    }

    public float getGreen(){
        if (!grayscale){
            return green;
        }else{
            return (float) gray;
        }
    }

    public float getBlue(){
        if (!grayscale){
            return blue;
        }else{
            return (float) gray;
        }
    }

    public float getAlpha(){
        if (!grayscale && hasAlpha){
            return alpha;
        }else{
            return 1f;
        }
    }

    public void bind(){
        glColor4f(getRed(), getGreen(), getBlue(), getAlpha());
    }
}
