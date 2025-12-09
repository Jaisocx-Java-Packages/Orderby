package com.jaisocx.orderby.helpers;

import com.jaisocx.orderby.exceptions.ComparableLengthException;

/**
 * @Author: ILLIA POLIANSKYI
 * @Date: Sun, the 21st of July 2024
 * @Description: 
 * This class idea:
 * the x64 CPU handles 64bit long values comparations in 1 CPU core tick (in 2024 3B comparations in a second with 3GG CPU core)
 * the same as 8bit values, bytes, how the Strings are stored in RAM
 * the class transforms a byte array of the encoded by charset String, to array of long values each 64bits.
 * so, later a CPU can compare the strings 8 letters with 8 other letters in 1 single CPU core tick
 */
public class TransformerToComparable {
    public static long[] transformByteArrayToComparable(byte[] byteArray) {
        long sourceByteCount = byteArray.length;
        long comparableLength = 0;
        try {
            comparableLength = TransformerToComparable.getComparableLength(sourceByteCount);
        } catch (ComparableLengthException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long[] comparableValue = new long[(int) comparableLength];

        int positionInLongArray = 0;
        for (int i=0; i < sourceByteCount; i+=8) {
            long comparableLongItem = 0;
            for (int innerByteCount = 0; innerByteCount < 8; innerByteCount++) {
                if (!((i+innerByteCount) < sourceByteCount)) {
                    break;
                }

                int shift = (7 - innerByteCount) << 3;

                comparableLongItem |= ((byteArray[i + innerByteCount] & 0xFFL) << shift);
            }
            comparableValue[positionInLongArray] = comparableLongItem;
            positionInLongArray++;
        }

        return comparableValue;
    }

    public static long getComparableLength(long byteArrayLength) throws ComparableLengthException {
        long comparableLength = 0;

        long dividedBy8Floored = byteArrayLength >> 3;
        long flooredMultipliedBy8 = dividedBy8Floored << 3;

        if (byteArrayLength > flooredMultipliedBy8) {
            comparableLength = dividedBy8Floored + 1;
        } else if (byteArrayLength == flooredMultipliedBy8) {
            comparableLength = dividedBy8Floored;
        } else {
            throw new ComparableLengthException();
        }

        return comparableLength;
    }
}
