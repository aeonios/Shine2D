package org.starfire.shine.opengl.pbuffer;

import java.util.HashMap;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.EXTPixelBufferObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.starfire.shine.Graphics;
import org.starfire.shine.Image;
import org.starfire.shine.SlickException;
import org.starfire.shine.util.Log;

/**
 * A factory to produce an appropriate render to texture graphics context based on current
 * hardware
 *
 * @author kevin
 */
public class GraphicsFactory {
	/** The graphics list of graphics contexts created */
	private static HashMap graphics = new HashMap();
	/** True if pbuffers are supported */
	/** True if fbo are supported */
	private static boolean fbo = true;
	private static GLCapabilities glcap = null;
	/** True if we've initialised */
	private static boolean init = false;
	
	/**
	 * Initialise offscreen rendering by checking what buffers are supported
	 * by the card
	 * 
	 * @throws SlickException Indicates no buffers are supported
	 */
	private static void init() throws SlickException {
		init = true;

		if (glcap == null) {
			glcap = GL.createCapabilities();
		}
		
		if (fbo) {
			fbo = glcap.GL_EXT_framebuffer_object;
		}

		if (!fbo){
			throw new SlickException("Framebuffer objects not supported!");
		}
		
		Log.info("Offscreen Buffers FBO="+fbo);
	}
	
	/**
	 * Force FBO use on or off
	 * 
	 * @param useFBO True if we should try and use FBO for offscreen images
	 */
	public static void setUseFBO(boolean useFBO) {
		fbo = useFBO;
	}
	
	/**
	 * Check if we're using FBO for dynamic textures
	 * 
	 * @return True if we're using FBOs
	 */
	public static boolean usingFBO() {
		return fbo;
	}

	/**
	 * Check if we're using PBuffer for dynamic textures
	 * 
	 * @return True if we're using PBuffer
	 */
	
	/**
	 * Get a graphics context for a particular image
	 * 
	 * @param image The image for which to retrieve the graphics context
	 * @return The graphics context
	 * @throws SlickException Indicates it wasn't possible to create a graphics context
	 * given available hardware.
	 */
	public static Graphics getGraphicsForImage(Image image) throws SlickException {
		Graphics g = (Graphics) graphics.get(image.getTexture());
		
		if (g == null) {
			g = createGraphics(image);
			graphics.put(image.getTexture(), g);
		}
		
		return g;
	}
	
	/**
	 * Release any graphics context that is assocaited with the given image
	 * 
	 * @param image The image to release
	 * @throws SlickException Indicates a failure to release the context
	 */
	public static void releaseGraphicsForImage(Image image) throws SlickException {
		Graphics g = (Graphics) graphics.remove(image.getTexture());
		
		if (g != null) {
			g.destroy();
		}
	}
	
	/** 
	 * Create an underlying graphics context for the given image
	 * 
	 * @param image The image we want to render to
	 * @return The graphics context created
	 * @throws SlickException
	 */
	private static Graphics createGraphics(Image image) throws SlickException {
		init();
		
		if (fbo) {
			try {
				return new FBOGraphics(image);
			} catch (Exception e) {
				throw new SlickException(e.getMessage());
			}
		}
		Log.error("Framebuffer objects not supported!");
		System.exit(-2);
		return new Graphics(); // java is dumb, this will never be reached.
	}
}
