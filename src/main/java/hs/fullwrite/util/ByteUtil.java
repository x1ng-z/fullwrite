package hs.fullwrite.util;

import java.math.BigDecimal;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/13 8:32
 */
public class ByteUtil {

    public static enum CType {
        /**int*/
        Int(4),
        /**long*/
        Long(8);

        private int bytenum;


        CType(int bytenum) {
            this.bytenum = bytenum;
        }


        public int getBytenum() {
            return bytenum;
        }

        public long shiftLong(byte[] input, int offset, boolean littleEndian) {
            long value = 0;
            for (int count = 0; count < getBytenum(); ++count) {
                int shift = (littleEndian ? count : ((getBytenum() - 1) - count)) << 3;
                value |= ((long) 0xff << shift) & ((long) input[offset + count] << shift);
            }
            return value;
        }
    }

    public static long byteToLong(byte[] input, int offset, boolean littleEndian, CType cType) throws RuntimeException {
        long value = 0;
        int bytenum = cType.getBytenum();
        if (input.length - offset >= bytenum) {
            value = cType.shiftLong(input, offset, littleEndian);
        } else {
            throw new RuntimeException("byte array has not enough space");
        }
        return value;
    }


}


