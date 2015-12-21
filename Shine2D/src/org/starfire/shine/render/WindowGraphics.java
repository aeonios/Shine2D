package org.starfire.shine.render;

import org.starfire.shine.Color;
import org.starfire.shine.Image;
import org.starfire.shine.Window;
import org.starfire.shine.geometry.Line;

import java.nio.FloatBuffer;

import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by haplo on 12/20/2015.
 */
public class WindowGraphics extends Graphics {
    Window window;


    public WindowGraphics(Window win) {
        window = win;

        // generate default colors (black bg white fg)
        bgColor = new Color(0, 0, 0);
        activeColor = new Color(1, 1, 1);
        reset();
    }

    protected void predraw(){
        if (current != this){
            current = this;
            bind();
        }
    }

    void bind(){
        // configure general settings for 2d drawing
        if (linePattern != Line.STIPPLE_SOLID) {
            glEnable(GL_LINE_STIPPLE);
            glLineStipple(lineScale, linePattern);
        }else{
            glDisable(GL_LINE_STIPPLE);
        }
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);

        glShadeModel(GL_SMOOTH);

        // bind the window's backbuffer as the current drawing context
        window.makeCurrent();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

        // configure perspective settings for flat 2d drawing to the given canvas.
        glMatrixMode(GL_PROJECTION); // set projection first
        glLoadIdentity(); // and reset it

        // then set viewport second
        glViewport(0, 0, window.getWidth(), window.getHeight());

        // then set ortho third
        int w = window.getWidth();
        int h = window.getHeight();

        if (mirrorVert && !mirrorHoriz){
            glOrtho(0, w, h, 0, -1.0, 1.0);
        }else if (mirrorHoriz && !mirrorVert){
            glOrtho(w, 0, 0, h, -1.0, 1.0);
        }else if (mirrorVert && mirrorHoriz){
            glOrtho(w, 0, h, 0, -1.0, 1.0);
        }else{
            glOrtho(0, w, 0, h, -1.0, 1.0);
        }

        // finally switch to modelview
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        activeColor.bind();
        glLineWidth(lineWidth);
        setDrawMode(currentDrawingMode);
    }

    public Color getPixel(int x, int y){
        predraw(); // bind to the appropriate buffer so that glReadPixels will pull pixels from it.
        FloatBuffer pixels = createFloatBuffer(4);
        glReadPixels(x, y, 1, 1, GL_RGBA, GL_FLOAT, pixels);
        return new Color(pixels.get(0), pixels.get(1), pixels.get(2), pixels.get(3));
    }
}
