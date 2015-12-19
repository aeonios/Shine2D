package org.starfire.shine.geometry;

/**
 * Created by haplo on 12/15/2015.
 */
public class Line {
    // this class provides constants and utils for lines.
    public static final short STIPPLE_SOLID = Short.MAX_VALUE;
    public static final short STIPPLE_DOT = 0x0101;
    public static final short STIPPLE_DASH = 0x00FF;
    public static final short STIPPLE_DOTDASH = 0x1C47;

    static final int ROTATE_LEFT = 1;
    static final int ROTATE_RIGHT = 2;

    // Stuff used to implement bitrotate for phase shifting stipple patterns
    /////////////////////////////////////////////////////////////////////////
    private static int calcmask(int bitstorotate, int direction) {
        int mask = 0;
        int c;

        if (bitstorotate == 0)
            return 0;

        c = 0x80000000;
        mask = (c >> bitstorotate);
        if (direction == ROTATE_RIGHT)
        {
            mask = (c >> (32 - bitstorotate));
            mask = ~mask;
        }
        else
            mask = (c >> bitstorotate);

        return mask;
    }

    private static int rotr(int value, int bitstorotate, int sizet)
    {
        int tmprslt =0;
        int mask=0;;
        int target=0;

        bitstorotate %= sizet;
        target = value;

        // determine which bits will be impacted by the rotate
        mask = calcmask(bitstorotate, ROTATE_RIGHT);

        // save off the bits which will be impacted
        tmprslt = value & mask;

        // perform the actual rotate right
        target = (value  >>> bitstorotate);

        // now rotate the saved off bits so they are in the proper place
        tmprslt <<= (sizet - bitstorotate);

        // now add the saved off bits
        target |= tmprslt;

        // and return the result
        return target;
    }

    private static int rotl(int value, int bitstorotate, int sizet)
    {
        int tmprslt =0;
        int mask=0;;
        int target=0;

        bitstorotate %= sizet;

        // determine which bits will be impacted by the rotate
        mask = calcmask(bitstorotate, ROTATE_LEFT);
        // shift the mask into the correct place (i.e. if we are delaying with a byte rotate, we
        // need to ensure we have the mask setup for a byte or 8 bits)
        mask >>>= (32 - sizet);

        // save off the affected bits
        tmprslt = value & mask;

        // perform the actual rotate
        target = (value  << bitstorotate);

        // now shift the saved off bits
        tmprslt >>>= (sizet - bitstorotate);

        // add the rotated bits back in (in the proper location)
        target |= tmprslt;

        // now return the result
        return target;
    }

    public static short rotr(short value, int bitstorotate) {
        short result;

        result = (short) rotr((0x0000ffff & value), bitstorotate, 16);

        return result;
    }

    public static short rotl(short value, int bitstorotate) {
        short result;

        result = (short) rotl((0x0000ffff & value), bitstorotate, 16);

        return result;

    }
}
