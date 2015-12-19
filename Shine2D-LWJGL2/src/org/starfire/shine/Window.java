package org.starfire.shine;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GLContext;
import org.starfire.shine.util.Log;

import static org.lwjgl.BufferUtils.*;

import java.nio.IntBuffer;

/**
 * Created by haplo on 12/14/2015.
 *
 * This is a wrapper for LWJGL 2.9's display interface
 * to make it easier to get cross-compat with 3.x
 */
public class Window {
    long windowID;
    Graphics graphics;
    int width;
    int height;

    public Window(String name, int w, int h) throws Exception{
        width = w;
        height = h;
        System.setProperty("org.lwjgl.opengl.Window.undecorated", "true"); // set windows (globally) to have no border decorations

        Display.setDisplayMode(new DisplayMode(0, 0)); // initialize the window with zero size so that it doesn't show up
        Display.setResizable(false);

        // initialize the window at fullscreen size
        Display.create();
        Display.makeCurrent();

        if (!GLContext.getCapabilities().GL_EXT_framebuffer_object){
            throw new UnsupportedOperationException("Framebuffers not supported!");
        }

        graphics = new Graphics(this);
    }

    public void hide(){
        try {
            Display.setDisplayMode(new DisplayMode(0, 0));
            graphics.reset();
        }catch (Exception e) {
            Log.error(e);
        }
    }

    public void show(){
        try {
            Display.setDisplayMode(new DisplayMode(width, height));
            graphics.reset();
        }catch (Exception e) {
            Log.error(e);
        }
    }

    public Graphics getGraphics(){
        return graphics;
    }

    public int getWidth(){
        return Display.getWidth();
    }

    public int getHeight(){
        return Display.getHeight();
    }

    public void setSize(int w, int h){
        width = w;
        height = h;
        try {
            Display.setDisplayMode(new DisplayMode(width, height));
        }catch (Exception e) {
            Log.error(e);
        }
    }

    public void destroy(){
        Display.destroy();
    }

    public void redraw(){
        Display.update();
        try {
            //Display.swapBuffers();
        }catch (Exception e){
            Log.error(e);
        }
    }

    public boolean requestedToClose(){
        return Display.isCloseRequested();
    }

    public void makeCurrent(){
        try {
            Display.makeCurrent();
            Display.getDrawable().makeCurrent();
            GLContext.getCapabilities();
        }catch (Exception e) {
            Log.error(e);
        }
    }
}
