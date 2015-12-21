package org.starfire.shine.render;

import org.starfire.shine.Color;
import org.starfire.shine.Image;
import org.starfire.shine.Window;
import org.starfire.shine.geometry.Line;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.BufferUtils.*;

/**
 * Created by haplo on 12/20/2015.
 */
public class ImageGraphics extends Graphics {
    static int fboID = 0;
    Image image;
    boolean dirty = true;
    float[] pixelData;

    public ImageGraphics(Image img) {
        image = img;

        // get a new FBO handle
        if (fboID == 0) {
            fboID = glGenFramebuffersEXT();
        }

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboID);

        // bind the image texture to the framebuffer.
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, img.texID, 0);

        if (glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) != GL_FRAMEBUFFER_COMPLETE_EXT){
            glDeleteFramebuffersEXT(fboID);
            throw new IllegalStateException("Framebuffer initialization failed.");
        }

        // generate default colors (black bg white fg)
        bgColor = new Color(0, 0, 0);
        activeColor = new Color(1, 1, 1);
        reset();
    }

    protected void predraw(){
        dirty = true;
        if (current != this){
            bind();
            current = this;
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

        // bind our fbo as the current drawing context if necessary.
        if (current instanceof WindowGraphics || current == null) {
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboID);
        }
        // attach the the texture this graphics manages to the shared FBO.
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, image.texID, 0);

        if (glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) != GL_FRAMEBUFFER_COMPLETE_EXT){
            glDeleteFramebuffersEXT(fboID);
            throw new IllegalStateException("Framebuffer initialization failed.");
        }

        // configure perspective settings for flat 2d drawing to the given canvas.
        glMatrixMode(GL_PROJECTION); // set projection first
        glLoadIdentity(); // and reset it

        // then set viewport second
        glViewport(0, 0, image.texWidth, image.texHeight);

        // then set ortho third
        int w = image.texWidth;
        int h = image.texHeight;

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
        // cache pixel data for faster reading
        // as reading directly from openGL is unbelievably slow.
        if (dirty){
            predraw(); // bind to the appropriate buffer and load the texture so that glReadPixels will pull pixels from it.
            if (pixelData == null) {
                pixelData = new float[image.width * image.height * 4];
            }
            FloatBuffer pixels = createFloatBuffer(image.width * image.height * 4);
            glReadPixels(0, 0, image.width, image.height, GL_RGBA, GL_FLOAT, pixels);
            pixels.get(pixelData);
            dirty = false;
        }

        int i = (y * image.width * 4) + (x * 4);

        return new Color(pixelData[i], pixelData[i+1], pixelData[i+2], pixelData[i+3]);
    }
}
