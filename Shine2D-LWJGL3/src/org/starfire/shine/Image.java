package org.starfire.shine;

import org.starfire.shine.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.BufferUtils.*;

/**
 * Created by haplo on 12/14/2015.
 */
public class Image {
    /** The width of the image */
    public int width;
    /** The height of the image */
    public int height;
    /** The width of the texture */
    public int texWidth;
    /** The height of the texture */
    public int texHeight;
    /** The GL ID of the texture */
    public int texID;
    /** The Graphics object that draws to this image */
    private Graphics graphics;
    /** The GL TexType of the texture */
    final int texType = GL_TEXTURE_2D;

    public Image(int width, int height){
        this.width = width;
        this.height = height;

        // textures on the gfx card must have power of 2 dimensions.
        texWidth = getPow2(width);
        texHeight = getPow2(height);

        // prevent it from trying to create textures that are too large
        int max = glGetInteger(GL_MAX_TEXTURE_SIZE);
        if (texWidth > max || texHeight > max){
            throw new IllegalStateException("Tried to create a texture too large for the graphics card.");
        }

        // create some filler data for our texture
        byte[] rawData = new byte[texWidth * texHeight * 4]; // here 4 is a colormodel constant for RGBA

        // convert the raw data array into a bytebuffer to bind to the GL texture handle.
        ByteBuffer imgData = createByteBuffer(rawData.length).put(rawData);
        imgData.flip(); // bytebuffers are fucking stupid.

        // get a new GL texture handle to put our texture in, and set our image with it.
        texID = glGenTextures();
        bind();

        // load the texture data into a GL texture and create a Graphics object to draw to it.
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texWidth, texHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, imgData);

        // unbind the texture since we don't need it to be bound unless we're actually drawing it on something.
        unbind();

        graphics = new Graphics(this);
    }

    public void bind(){
        glEnable(texType);
        glBindTexture(texType, texID);
    }

    public void unbind(){
        glDisable(texType);
    }

    public void setTexID(int tid){
        texID = tid;
    }

    public void draw(float offsetX, float offsetY, float scaleX, float scaleY, float angle){
        bind();
        glTranslatef(offsetX, offsetY, 0);

        float swidth = width * scaleX;
        float sheight = height * scaleY;

        if (angle != 0) {
            float centerX = 0.5f * swidth;
            float centerY = 0.5f * sheight;
            glTranslatef(centerX, centerY, 0.0f);
            glRotatef(angle, 0.0f, 0.0f, 1.0f);
            glTranslatef(-centerX, -centerY, 0.0f);
        }

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex3f(offsetX, offsetY, 0);
        glTexCoord2f(0, texHeight);
        glVertex3f(offsetX, offsetY + sheight, 0);
        glTexCoord2f(texWidth, texHeight);
        glVertex3f(offsetX + swidth, offsetY + sheight, 0);
        glTexCoord2f(texWidth, 0);
        glVertex3f(offsetX + swidth, offsetY, 0);
        glEnd();

        glLoadIdentity();
        unbind();
    }

    public void destroy(){
        graphics.destroy();
        glDeleteTextures(texID);
    }

    public Graphics getGraphics(){
        return graphics;
    }

    private int getPow2(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    }
}
