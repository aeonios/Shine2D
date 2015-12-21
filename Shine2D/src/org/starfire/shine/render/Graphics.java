package org.starfire.shine.render;

import org.starfire.shine.Color;
import org.starfire.shine.Image;
import org.starfire.shine.Window;
import org.starfire.shine.geometry.FastTrig;
import org.starfire.shine.geometry.Line;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.BufferUtils.*;

import java.nio.FloatBuffer;

/**
 * Created by haplo on 12/15/2015.
 */
public abstract class Graphics {
    /** The last active graphics object, used to reduce redundant GL-state setup between draw calls */
    static Graphics current;

    /** Various stored graphics state settings */
    byte currentDrawingMode = 0;
    boolean useAA = true;

    Color bgColor;
    Color activeColor;

    float lineWidth = 1f;
    int lineScale = 1;
    short linePattern = Line.STIPPLE_SOLID;

    /** Scale and rotation variables */
    float scaleWidth = 1;
    float scaleHeight = 1;
    float rotAngle = 0;
    boolean mirrorVert = false;
    boolean mirrorHoriz = false;

    /** Constants */
    static final int DEFAULT_SEGMENTS = 64; // default number of segments to use for drawing arcs.

    public static byte MODE_NORMAL = 0;
    public static byte MODE_ADD = 1;
    public static byte MODE_MULTIPLY = 2;
    public static byte MODE_SCREEN = 3;
    public static byte MODE_ALPHA_MAP = 4;

    abstract void bind();

    abstract protected void predraw();

    public static void reset(){
        //clear the cached graphics instance to force it to reset the openGL state
        current = null;
    }

    ////////////////////////////
    // Interface/Drawing Stuff
    //

    public void setDrawMode(byte mode) {
        currentDrawingMode = mode;
        if (currentDrawingMode == MODE_NORMAL) {
            // normal drawing with alpha blending
            glEnable(GL_BLEND);
            glColorMask(true, true, true, true);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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

    public abstract Color getPixel(int x, int y);

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

    public void setLinePattern(short pattern, int scale, int phase) {
        if (linePattern != Line.STIPPLE_SOLID) {
            pattern = Line.rotr(pattern, -1 * phase);
            this.lineScale = scale;
            glEnable(GL_LINE_STIPPLE);
            glLineStipple(scale, pattern);
        }else{
            glDisable(GL_LINE_STIPPLE);
        }
        this.linePattern = pattern;
    }

    public void mirrorVert(boolean mirror){
        mirrorVert = mirror;
        reset();
    }

    public void mirrorHoriz(boolean mirror){
        mirrorHoriz = mirror;
        reset();
    }

    public void clear(){
        predraw();
        glClearColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
        glClear(GL_COLOR_BUFFER_BIT);
    }

    public void setColor(Color c){
        activeColor = c;
        activeColor.bind();
    }

    public void setScale(float scaleX, float scaleY){
        scaleWidth = scaleX;
        scaleHeight = scaleY;
    }

    public void setRotation(float angleInDegrees){
        rotAngle = angleInDegrees;
    }

    public void antiAlias(boolean aa){
        this.useAA = aa;
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
        if (useAA) {
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
        }
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
