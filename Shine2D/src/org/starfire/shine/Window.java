package org.starfire.shine;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libffi.Closure;
import org.starfire.shine.render.Graphics;
import org.starfire.shine.render.WindowGraphics;

import static org.lwjgl.BufferUtils.*;

import java.nio.IntBuffer;

/**
 * Created by haplo on 12/14/2015.
 *
 * This is simply a wrapper for GLFW's window interface,
 * to make it easier to manage window state and to get the gl context of the window.
 */
public class Window {
    long windowID;
    WindowGraphics graphics;
    static boolean glfw = false;
    static final long NULL = MemoryUtil.NULL;
    private GLFWErrorCallback errorCallback;
    Closure debugProc;

    // window type enum
    public static final byte NORMAL = 0;
    public static final byte NORESIZE = 1;
    public static final byte BORDERLESS = 2;
    public static final byte DEBUG = 3;

    public Window(byte windowType, String name, int w, int h) throws Exception{

        if (!glfw){
            // init GLFW if it isn't already
            if (glfwInit() != GLFW_TRUE) {
                throw new RuntimeException("GLFW failed to init!");
            }
            glfw = true;
        }

        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

        // don't flush the pipeline when unbinding contexts because it can break things.
        glfwWindowHint(GLFW_CONTEXT_RELEASE_BEHAVIOR, GLFW_RELEASE_BEHAVIOR_NONE);

        // set hints for window type
        switch (windowType) {
            case NORMAL :
                glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
                break;
            case NORESIZE :
                glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
                break;
            case BORDERLESS :
                glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
                glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
                break;
            case DEBUG :
                glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
                break;
        }
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // initialize the window in the hidden state

        GLFWVidMode vm = glfwGetVideoMode(glfwGetPrimaryMonitor());

        // set color and refresh hints
        glfwWindowHint(GLFW_RED_BITS, vm.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, vm.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, vm.blueBits());
        glfwWindowHint(GLFW_REFRESH_RATE, vm.refreshRate());
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        // initialize the window
        this.windowID = glfwCreateWindow(vm.width(), vm.height(), name, NULL, NULL);
        glfwMakeContextCurrent(windowID);
        glfwSwapInterval(1);
        glfwSetWindowSize(windowID, w, h);
        GLCapabilities cap = GL.createCapabilities();
        if (!cap.GL_EXT_framebuffer_object){
            throw new UnsupportedOperationException("The system reports that framebuffer objects are not supported.");
        }

        // initialize GL debug
        debugProc = GLUtil.setupDebugMessageCallback();

        // initialize the window's graphics instance
        graphics = new WindowGraphics(this);
    }

    public void hide(){
        glfwHideWindow(windowID);
    }

    public void show(){
        glfwShowWindow(windowID);
    }

    public Graphics getGraphics(){
        return graphics;
    }

    public int getWidth(){
        IntBuffer width = createIntBuffer(1);
        IntBuffer height = createIntBuffer(1);
        glfwGetFramebufferSize(windowID, width, height);
        return width.get(0);
    }

    public int getHeight(){
        IntBuffer width = createIntBuffer(1);
        IntBuffer height = createIntBuffer(1);
        glfwGetFramebufferSize(windowID, width, height);
        return height.get(0);
    }

    public void setSize(int width, int height){
        glfwSetWindowSize(windowID, width, height);
    }

    public void destroy(){
        glfwDestroyWindow(windowID);
    }

    public void redraw(){
        glfwSwapBuffers(windowID);
    }

    public void poll(){
        glfwPollEvents();
    }

    public boolean requestedToClose(){
        if (glfwWindowShouldClose(windowID) == GLFW_TRUE){
            return true;
        }
        return false;
    }

    public void makeCurrent(){
        glfwMakeContextCurrent(windowID);
    }

    public void unbind(){glfwMakeContextCurrent(NULL);}
}
