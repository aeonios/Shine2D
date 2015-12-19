package shinetest;

import org.starfire.shine.Color;
import org.starfire.shine.Graphics;
import org.starfire.shine.Image;
import org.starfire.shine.Window;
import org.starfire.shine.geometry.Line;
import org.starfire.shine.util.Log;

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
            window = new Window(Window.NORESIZE,"Shine Test", 640, 480);
        }catch (Exception e){
            Log.error(e.getMessage());
        }
        windowGraphics = window.getGraphics();
        /*graphImage = new Image(640, 480);
        graphGraphics = graphImage.getGraphics();


        graphGraphics.setBackground(new Color(0, 0, 0, 1));
        graphGraphics.clear();

        graphGraphics.setColor(new Color(1, 0, 0, 1));
        graphGraphics.fillOval(320, 240, 100, 100);*/

        window.show();
    }

    private void run(){
        windowGraphics.setBackground(new Color(0, 1, 0, 1));
        windowGraphics.setLineWidth(3);
        while (!window.requestedToClose()){
            long frame = System.currentTimeMillis();
            if (frame > nextFrame){
                windowGraphics.setLinePattern(1, Line.STIPPLE_SOLID, 0);
                windowGraphics.clear();
                windowGraphics.setColor(new Color(1, 0, 0, 1));
                windowGraphics.drawLine(0, 0, 640, 480);
                window.redraw();
                nextFrame = frame + 30; // target ~30 fps
            }
        }
    }

    public static void main(String[] args)
    {
        ShineTest test = new ShineTest();
        test.run();
    }

}
