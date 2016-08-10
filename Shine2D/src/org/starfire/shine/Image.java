package org.starfire.shine;

import org.starfire.shine.render.Graphics;
import org.starfire.shine.render.ImageGraphics;

import java.nio.ByteBuffer;

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
    private ImageGraphics graphics;
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

        for (int i = 0; i < rawData.length; i++){
            rawData[i] = -1;
        }

        // convert the raw data array into a bytebuffer to bind to the GL texture handle.
        ByteBuffer imgData = createByteBuffer(rawData.length).put(rawData);
        imgData.flip();

        // get a new GL texture handle to put our texture in, and set our image with it.
        glEnable(texType);
        texID = glGenTextures();
        glBindTexture(texType, texID);

        // load the texture data into a GL texture and create a Graphics object to draw to it.
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texWidth, texHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, imgData);
        glDisable(texType);

        graphics = new ImageGraphics(this);
    }

    public void draw(float offsetX, float offsetY, float scaleX, float scaleY, float angle){
        glEnable(texType);
        glBindTexture(texType, texID);

        glTranslatef(offsetX, offsetY, 0);

        float swidth = width * scaleX;
        float sheight = height * scaleY;

        // Texcoords range from 0 to 1, so we need to set our max coords relative to the power-of-2 texture size
        // so that only our canvas area is drawn.
        float texh = ((float) height)/((float) texHeight);
        float texw = ((float) width)/((float) texWidth);

        if (angle != 0) {
            float centerX = 0.5f * swidth;
            float centerY = 0.5f * sheight;
            glTranslatef(centerX, centerY, 0.0f);
            glRotatef(angle, 0.0f, 0.0f, 1.0f);
            glTranslatef(-centerX, -centerY, 0.0f);
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex3f(offsetX, offsetY, 0);
        glTexCoord2f(0, texh);
        glVertex3f(offsetX, offsetY + sheight, 0);
        glTexCoord2f(texw, texh);
        glVertex3f(offsetX + swidth, offsetY + sheight, 0);
        glTexCoord2f(texw, 0);
        glVertex3f(offsetX + swidth, offsetY, 0);
        glEnd();

        glLoadIdentity();
        glDisable(texType);
    }

    public void destroy(){
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
