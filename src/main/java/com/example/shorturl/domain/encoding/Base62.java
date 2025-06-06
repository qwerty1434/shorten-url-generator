package com.example.shorturl.domain.encoding;

import java.math.BigInteger;

public abstract class Base62 {
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int POSITIVE_SIGNUM = 1;

    public static String encrypt(byte[] bytes) {
        BigInteger number = new BigInteger(POSITIVE_SIGNUM, bytes);
        StringBuilder sb = new StringBuilder();
        BigInteger base = BigInteger.valueOf(62);

        while (number.compareTo(BigInteger.ZERO) > 0) {
            int remainder = number.mod(base).intValue();
            sb.append(BASE62_CHARS.charAt(remainder));
            number = number.divide(base);
        }

        return sb.reverse().toString();
    }
}
