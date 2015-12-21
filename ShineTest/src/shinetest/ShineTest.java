package shinetest;

import org.starfire.shine.Color;
import org.starfire.shine.render.Graphics;
import org.starfire.shine.Image;
import org.starfire.shine.Window;
import org.starfire.shine.geometry.Line;
import org.starfire.shine.util.Log;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

/**
 * Created by haplo on 12/12/2015.
 */
public class ShineTest{
    static {LibLoader.load();}
    private Window window;
    private Image graphImage;
    private Graphics graphGraphics;
    private Graphics windowGraphics;

    private long nextFrame = 0;

    public ShineTest() {
        try {
            window = new Window(Window.NORESIZE ,"Shine Test", 1024, 768);
        }catch (Exception e){
            Log.error(e.getMessage());
        }
        windowGraphics = window.getGraphics();
        graphImage = new Image(1024, 768);
        graphGraphics = graphImage.getGraphics();

        window.show();
    }

    private void run(){

        while (!window.requestedToClose()){

            long frame = System.currentTimeMillis();
            if (frame > nextFrame){
                nextFrame = frame + 30; // target ~30 fps
                windowGraphics.setScale(1, 1);
                windowGraphics.mirrorHoriz(false);
                drawToFBO(frame);
                drawToWindow(frame);
                windowGraphics.setDrawMode(Graphics.MODE_NORMAL);
                windowGraphics.setScale(0.5f, 0.5f);
                windowGraphics.mirrorHoriz(true);
                windowGraphics.drawImage(graphImage);

                window.redraw();
            }
        }
        graphImage.destroy();
        window.destroy();
    }

    private void drawToWindow(long frame){
        windowGraphics.setBackground(new Color(0, 0, 0, 1));
        windowGraphics.setLineWidth(4);
        windowGraphics.setDrawMode(Graphics.MODE_ADD);
        windowGraphics.antiAlias(false); // it draws 2x the number of circles with AA enabled, which is the default

        windowGraphics.clear();

        // draw some animated lines
        windowGraphics.setColor(new Color(0, 1, 0));
        windowGraphics.setLinePattern(Line.STIPPLE_DOT, 2, -1 * (int) (frame % 16));
        windowGraphics.drawLine(0, 0, window.getWidth(), window.getHeight());

        windowGraphics.setColor(new Color(1, 0, 0));
        windowGraphics.setLinePattern(Line.STIPPLE_DASH, 2, -1 * (int) (frame % 16));
        windowGraphics.drawLine(0, 0, window.getWidth()/2, window.getHeight());

        windowGraphics.setColor(new Color(0, 0, 1));
        windowGraphics.setLinePattern(Line.STIPPLE_DOTDASH, 2, -1 * (int) (frame % 16));
        windowGraphics.drawLine(0, 0, window.getWidth(), window.getHeight()/2);

        int n = 5;

        // draw n random red circles
        windowGraphics.setColor(new Color(0.5f, 0, 0, 1));
        for (int i = 0 ; i < n ; i++) {
            windowGraphics.fillOval((int) (Math.random() * window.getWidth()), (int) (Math.random() * window.getHeight()), 250, 250);
        }

        // draw n random green circles
        windowGraphics.setColor(new Color(0, 0.5f, 0, 1));
        for (int i = 0 ; i < n ; i++) {
            windowGraphics.fillOval((int) (Math.random() * window.getWidth()), (int) (Math.random() * window.getHeight()), 250, 250);
        }

        // draw n random blue circles
        windowGraphics.setColor(new Color(0, 0, 0.5f, 1));
        for (int i = 0 ; i < n ; i++) {
            windowGraphics.fillOval((int) (Math.random() * window.getWidth()), (int) (Math.random() * window.getHeight()), 250, 250);
        }

        // draw n random yellow circles
        windowGraphics.setColor(new Color(0.5f, 0.5f, 0, 1));
        for (int i = 0 ; i < n ; i++) {
            windowGraphics.fillOval((int) (Math.random() * window.getWidth()), (int) (Math.random() * window.getHeight()), 250, 250);
        }

        // draw n random cyan circles
        windowGraphics.setColor(new Color(0, 0.5f, 0.5f, 1));
        for (int i = 0 ; i < n ; i++) {
            windowGraphics.fillOval((int) (Math.random() * window.getWidth()), (int) (Math.random() * window.getHeight()), 250, 250);
        }

        // draw n random magenta circles
        windowGraphics.setColor(new Color(0.5f, 0, 0.5f, 1));
        for (int i = 0 ; i < n ; i++) {
            windowGraphics.fillOval((int) (Math.random() * window.getWidth()), (int) (Math.random() * window.getHeight()), 250, 250);
        }
    }

    private void drawToFBO(long frame){
        graphGraphics.setBackground(new Color(0, 0, 0, 1));
        graphGraphics.setLineWidth(3);
        graphGraphics.setDrawMode(Graphics.MODE_ADD);
        graphGraphics.antiAlias(false); // it draws 2x the number of circles with AA enabled, which is the default

        graphGraphics.clear();

        // draw some animated lines
        graphGraphics.setColor(new Color(0, 1, 0));
        graphGraphics.setLinePattern(Line.STIPPLE_DOT, 2, -1 * (int) (frame % 16));
        graphGraphics.drawLine(0, 0, graphImage.width, graphImage.height);

        graphGraphics.setColor(new Color(1, 0, 0));
        graphGraphics.setLinePattern(Line.STIPPLE_DASH, 2, -1 * (int) (frame % 16));
        graphGraphics.drawLine(0, 0, graphImage.width/2, graphImage.height);

        graphGraphics.setColor(new Color(0, 0, 1));
        graphGraphics.setLinePattern(Line.STIPPLE_DOTDASH, 2, -1 * (int) (frame % 16));
        graphGraphics.drawLine(0, 0, graphImage.width, graphImage.height/2);

        int n = 2;

        // draw n random red circles
        graphGraphics.setColor(new Color(0.5f, 0, 0, 1));
        for (int i = 0 ; i < n ; i++) {
            graphGraphics.fillOval((int) (Math.random() * graphImage.width), (int) (Math.random() * graphImage.height), 250, 250);
        }

        // draw n random green circles
        graphGraphics.setColor(new Color(0, 0.5f, 0, 1));
        for (int i = 0 ; i < n ; i++) {
            graphGraphics.fillOval((int) (Math.random() * graphImage.width), (int) (Math.random() * graphImage.height), 250, 250);
        }

        // draw n random blue circles
        graphGraphics.setColor(new Color(0, 0, 0.5f, 1));
        for (int i = 0 ; i < n ; i++) {
            graphGraphics.fillOval((int) (Math.random() * graphImage.width), (int) (Math.random() * graphImage.height), 250, 250);
        }

        // draw n random yellow circles
        graphGraphics.setColor(new Color(0.5f, 0.5f, 0, 1));
        for (int i = 0 ; i < n ; i++) {
            graphGraphics.fillOval((int) (Math.random() * graphImage.width), (int) (Math.random() * graphImage.height), 250, 250);
        }

        // draw n random cyan circles
        graphGraphics.setColor(new Color(0, 0.5f, 0.5f, 1));
        for (int i = 0 ; i < n ; i++) {
            graphGraphics.fillOval((int) (Math.random() * graphImage.width), (int) (Math.random() * graphImage.height), 250, 250);
        }

        // draw n random magenta circles
        graphGraphics.setColor(new Color(0.5f, 0, 0.5f, 1));
        for (int i = 0 ; i < n ; i++) {
            graphGraphics.fillOval((int) (Math.random() * graphImage.width), (int) (Math.random() * graphImage.height), 250, 250);
        }
    }

    public static void main(String[] args)
    {
        ShineTest test = new ShineTest();
        test.run();
    }

}
