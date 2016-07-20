package com.yellowmessenger.sdk.xmpp.crypto;

import java.io.Serializable;

import javax.crypto.SecretKey;

/**
 * Created by kishore on 18/07/16.
 */
public class KeySpec implements SecretKey, java.security.spec.KeySpec, Serializable {
    private static final long serialVersionUID = 6577238317307289933L;

    private final byte[] key;
    private final String algorithm;

    public KeySpec(byte[] key, String algorithm) {
        this.key = key;
        this.algorithm = algorithm;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getFormat() {
        return "RAW";
    }

    @Override
    public int hashCode() {
        int result = algorithm.length();
        for (byte element : key) {
            result += element;
        }
        return result;
    }

    @Override
    public byte[] getEncoded() {
        byte[] result = new byte[key.length];
        System.arraycopy(key, 0, result, 0, key.length);
        return result;
    }
}
