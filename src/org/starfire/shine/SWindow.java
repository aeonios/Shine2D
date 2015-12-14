package org.starfire.shine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

/**
 * Created by haplo on 12/14/2015.
 *
 * This is simply a wrapper for GLFW's window interface,
 * to make it easier to manage window state and to get the gl context of the window.
 */
public class SWindow {
    long windowID;
    static long NULL = MemoryUtil.NULL;

    public SWindow(int width, int height, String name, boolean resizeable){
        if (resizeable){
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);
        }else{
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
        }

        this.windowID = GLFW.glfwCreateWindow(width, height, name, NULL, NULL);
        GLFW.glfwMakeContextCurrent(windowID);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();
    }

    public void hide(){
        GLFW.glfwHideWindow(windowID);
    }

    public void show(){
        GLFW.glfwShowWindow(windowID);
    }

    public void setSize(int width, int height){
        GLFW.glfwSetWindowSize(windowID, width, height);
    }

    public void destroy(){
        GLFW.glfwDestroyWindow(windowID);
    }
}
