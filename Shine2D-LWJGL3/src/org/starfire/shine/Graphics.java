package org.starfire.shine;

import org.starfire.shine.geometry.FastTrig;
import org.starfire.shine.geometry.Line;
import org.starfire.shine.util.Log;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.BufferUtils.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by haplo on 12/15/2015.
 */
public class Graphics {
    /** The last active graphics object, used to reduce redundant GL-state setup between draw calls */
    static Graphics current;

    /** The Image or Window object that this graphics draws to */
    private Image image;
    private Window window;

    /** The FramebufferObject ID for the FBO this graphics uses */
    private int fboID;

    /** Various stored graphics state settings */
    private byte currentDrawingMode = 0;

    private Color bgColor;
    private Color activeColor;

    private float lineWidth = 1f;
    private int lineScale = 1;
    private short linePattern = Line.STIPPLE_SOLID;

    /** Scale and rotation variables */
    private float scaleWidth = 1;
    private float scaleHeight = 1;
    private float rotAngle = 0;

    /** Constants */
    private static final int DEFAULT_SEGMENTS = 64; // default number of segments to use for drawing arcs.

    public static byte MODE_NORMAL = 0;
    public static byte MODE_ADD = 1;
    public static byte MODE_MULTIPLY = 2;
    public static byte MODE_SCREEN = 3;
    public static byte MODE_ALPHA_MAP = 4;

    public Graphics(Image img){
        image = img;

        // get a new FBO handle
        fboID = glGenFramebuffersEXT();

        // generate default colors (black bg white fg)
        bgColor = new Color(0, 0, 0);
        activeColor = new Color(1, 1, 1);



        // bind the new framebuffer
        predraw();

        // bind the image texture to the framebuffer.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, img.texID, 0);
        checkBufferState(); // ensure that the buffer did not end up malformed for whatever reason.
        img.unbind();
    }

    public Graphics(Window win){
        window = win;
        fboID = 0; // 0 points to the window's backbuffer, works with LWJGL 2.x only!

        // generate default colors (black bg white fg)
        bgColor = new Color(0, 0, 0);
        activeColor = new Color(1, 1, 1);

        // bind to the window's backbuffer and setup GL state
        predraw();
    }

    private void bind(){
        // configure general settings for 2d drawing
        glEnable(GL_LINE_STIPPLE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_FRAMEBUFFER_EXT);

        glShadeModel(GL_SMOOTH);

        // bind our fbo (or the window's backbuffer) as the current drawing context
        if (window != null) {
            // if we have a window, set it as the current context in case it isn't already.
            window.makeCurrent();
        }
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboID);
        glDrawBuffer(GL_COLOR_ATTACHMENT0_EXT);
        glReadBuffer(GL_COLOR_ATTACHMENT0_EXT);

        // configure perspective settings for flat 2d drawing to the given canvas.
        glMatrixMode(GL_PROJECTION); // set projection first
        glLoadIdentity(); // and reset it

        // then set viewport second
        if (window != null) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
        }else{
            glViewport(0, 0, image.width, image.height);
        }

        // then set ortho third
        if (window != null) {
            glOrtho(0, window.getWidth(), 0, window.getHeight(), -1.0, 1.0);
        }else{
            glOrtho(0, image.width, 0, image.height, -1.0, 1.0);
        }

        // finally switch to modelview
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        activeColor.bind();
        glLineWidth(lineWidth);
        glLineStipple(lineScale, linePattern);
        setDrawMode(currentDrawingMode);
    }

    private void predraw(){
        if (current != this){
            current = this;
            bind();
        }
    }

    public static void reset(){
        //clear the cached graphics instance to force it to reset the openGL state
        glDisable(GL_FRAMEBUFFER_EXT);
        glDisable(GL_TEXTURE_2D);
        current = null;
    }

    public void destroy(){
        glDeleteFramebuffersEXT(fboID);
    }

    private void checkBufferState(){
        if (glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) != GL_FRAMEBUFFER_COMPLETE_EXT){
            glDeleteFramebuffersEXT(fboID);
            throw new IllegalStateException("Framebuffer initialization failed.");
        }
    }

    ////////////////////////////
    // Interface/Drawing Stuff
    //

    public void setDrawMode(byte mode) {
        currentDrawingMode = mode;
        if (currentDrawingMode == MODE_NORMAL) {
            // normal drawing with alpha blending
            glEnable(GL_BLEND);
            glColorMask(true, true, true, false);
            glBlendFunc(GL_DST_ALPHA, GL_ONE_MINUS_DST_ALPHA);
        }
        if (currentDrawingMode == MODE_ALPHA_MAP) {
            // replace dst alpha with src alpha, ignore other colors
            glDisable(GL_BLEND);
            glColorMask(false, false, false, true);
        }
        if (currentDrawingMode == MODE_MULTIPLY) {
            // multiply with alpha blending
            glEnable(GL_BLEND);
            glColorMask(true, true, true, false);
            glBlendFunc(GL_ONE_MINUS_SRC_COLOR, GL_SRC_COLOR);
        }
        if (currentDrawingMode == MODE_ADD) {
            // add with alpha blending
            glEnable(GL_BLEND);
            glColorMask(true, true, true, false);
            glBlendFunc(GL_ONE, GL_ONE);
        }
        if (currentDrawingMode == MODE_SCREEN) {
            // screen with alpha blending
            glEnable(GL_BLEND);
            glColorMask(true, true, true, false);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
        }
    }

    public Color getPixel(int x, int y){
        predraw(); // bind our FBO so that glReadPixels will pull pixels from it.
        FloatBuffer pixels = createFloatBuffer(4);
        glReadPixels(x, y, 1, 1, GL_RGBA, GL_FLOAT, pixels);
        return new Color(pixels.get(0), pixels.get(1), pixels.get(2), pixels.get(3));
    }

    public void setPixel(int x, int y, Color c){
        predraw();
        c.bind();
        glEnable(GL_POINT_SIZE);
        glPointSize(1);
        glBegin(GL_POINTS);
        glVertex2i(x, y);
        glEnd();
    }

    public void setBackground(Color c){
        bgColor = c;
    }

    public Color getBackground() {
        return bgColor;
    }

    public void setLineWidth(float width) {
        lineWidth = width;
        glLineWidth(width);
    }

    public void setLinePattern(int scale, short pattern, int phase) {
        phase = phase % 16;
        //pattern = Line.rotl(pattern, phase);
        this.lineScale = scale;
        this.linePattern = pattern;
        glLineStipple(scale, pattern);
    }

    public void clear(){
        predraw();
        glClearColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void setColor(Color c){
        activeColor = c;
        activeColor.bind();
    }

    public void setScale(float scaleX, float scaleY){
        scaleWidth = scaleX;
        scaleHeight = scaleY;
    }

    public void setRotation(float rot){
        rotAngle = rot;
    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        predraw();

        // GL_LINES is terrible. Eventually this needs a real line shader.
        glBegin(GL_LINE_STRIP);
        glVertex2f(x1, y1);
        glVertex2f(x2, y2);
        glEnd();
    }

    public void drawArc(float x1, float y1, float width, float height, int segments, float start, float end) {
        predraw();

        while (end < start) {
            end += 360;
        }

        // calculate the center point
        float cx = x1;
        float cy = y1;

        if (rotAngle != 0) {
            glTranslatef(cx, cy, 0.0f);
            glRotatef(rotAngle, 0.0f, 0.0f, 1.0f);
            glTranslatef(-cx, -cy, 0.0f);
        }

        glBegin(GL_LINE_STRIP);
        int step = 360 / segments;

        for (int a = (int) start; a < (int) (end + step); a += step) {
            float ang = a;
            if (ang > end) {
                ang = end;
            }
            float x = (float) (cx + (FastTrig.cos(Math.toRadians(ang)) * width * scaleWidth / 2.0f));
            float y = (float) (cy + (FastTrig.sin(Math.toRadians(ang)) * height * scaleHeight / 2.0f));

            glVertex2f(x, y);
        }
        glEnd();
        glLoadIdentity();
    }

    public void fillArc(float x1, float y1, float width, float height, int segments, float start, float end) {
        predraw();
        activeColor.bind();

        while (end < start) {
            end += 360;
        }

        // calculate the center point
        float cx = x1;
        float cy = y1;

        if (rotAngle != 0) {
            glTranslatef(cx, cy, 0.0f);
            glRotatef(rotAngle, 0.0f, 0.0f, 1.0f);
            glTranslatef(-cx, -cy, 0.0f);
        }

        glBegin(GL_TRIANGLE_FAN);
        int step = 360 / segments;

        glVertex2f(cx, cy);

        for (int a = (int) start; a < (int) (end + step); a += step) {
            float ang = a;
            if (ang > end) {
                ang = end;
            }

            float x = (float) (cx + (FastTrig.cos(Math.toRadians(ang)) * width * scaleWidth / 2.0f));
            float y = (float) (cy + (FastTrig.sin(Math.toRadians(ang)) * height * scaleHeight / 2.0f));

            glVertex2f(x, y);
        }
        glEnd();

        // anti-aliasing

        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(cx, cy);
        if (end != 360) {
            end -= 10;
        }

        for (int a = (int) start; a < (int) (end + step); a += step) {
            float ang = a;
            if (ang > end) {
                ang = end;
            }

            float x = (float) (cx + (FastTrig.cos(Math.toRadians(ang + 10)) * width * scaleWidth / 2.0f));
            float y = (float) (cy + (FastTrig.sin(Math.toRadians(ang + 10)) * height * scaleHeight / 2.0f));

            glVertex2f(x, y);
        }
        glEnd();
        glLoadIdentity();
    }

    public void drawArc(float x1, float y1, float width, float height,
                        float start, float end) {
        drawArc(x1, y1, width, height, DEFAULT_SEGMENTS, start, end);
    }

    public void fillArc(float x1, float y1, float width, float height,
                        float start, float end) {
        fillArc(x1, y1, width, height, DEFAULT_SEGMENTS, start, end);
    }

    public void drawOval(float x1, float y1, float width, float height) {
        drawOval(x1, y1, width, height, DEFAULT_SEGMENTS);
    }

    public void drawOval(float x1, float y1, float width, float height,
                         int segments) {
        drawArc(x1, y1, width, height, segments, 0, 360);
    }

    public void fillOval(float x1, float y1, float width, float height) {
        fillOval(x1, y1, width, height, DEFAULT_SEGMENTS);
    }

    public void fillOval(float x1, float y1, float width, float height, int segments) {
        fillArc(x1, y1, width, height, segments, 0, 360);
    }

    public void drawImage(Image image) {
        drawImage(image, 0, 0, new Color(1, 1, 1, 1));
    }

    public void drawImage(Image image, float x, float y) {
        drawImage(image, x, y, new Color(1, 1, 1, 1));
    }

    public void drawImage(Image image, float x, float y, Color filter) {
        predraw();
        filter.bind();
        image.draw(x, y, scaleWidth, scaleHeight, rotAngle);
        activeColor.bind();
    }
}
