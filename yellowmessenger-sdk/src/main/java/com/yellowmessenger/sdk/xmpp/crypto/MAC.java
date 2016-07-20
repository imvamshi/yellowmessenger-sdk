package com.yellowmessenger.sdk.xmpp.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

/**
 * Created by kishore on 18/07/16.
 */
public class MAC {

    public static final String HMACSHA1 = "HmacSHA1";

    private static Mac HMAC_SHA1;

    static {
        try {
            HMAC_SHA1 = Mac.getInstance(HMACSHA1);
        }
        catch (NoSuchAlgorithmException e) {
            // Smack wont be able to function normally if this exception is thrown, wrap it into
            // an ISE and make the user aware of the problem.
            throw new IllegalStateException(e);
        }
    }


    public static synchronized byte[] hmacsha1(KeySpec key, byte[] input) throws InvalidKeyException {
        HMAC_SHA1.init(key);
        return HMAC_SHA1.doFinal(input);
    }

    public static byte[] hmacsha1(byte[] keyBytes, byte[] input) throws InvalidKeyException {
        KeySpec key = new KeySpec(keyBytes, HMACSHA1);
        return hmacsha1(key, input);
    }

}