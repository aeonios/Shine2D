package org.starfire.shine.geometry;

/**
 * Created by haplo on 12/15/2015.
 */
public class Line {
    // this class provides constants and utils for lines.
    public static final short STIPPLE_SOLID = Short.MIN_VALUE;
    public static final short STIPPLE_DOT = 0x0101;
    public static final short STIPPLE_DASH = 0x00FF;
    public static final short STIPPLE_DOTDASH = 0x1C47;

    static final int ROTATE_LEFT = 1;
    static final int ROTATE_RIGHT = 2;

    // Stuff used to implement bitrotate for phase shifting stipple patterns
    /////////////////////////////////////////////////////////////////////////

    public static short rotr(short value, int bitstorotate) {
        if (bitstorotate < 0){
            bitstorotate = 16 - ((-1 * bitstorotate) % 16);
        }else{
            bitstorotate %= 16;
        }
        int translate = (int) value;
        translate &= 0xFFFF;
        translate = (translate >> bitstorotate) | (translate << (16 - bitstorotate));

        return (short) translate;
    }
}
