package org.example.vibewall.encryption;

import java.util.Base64;

public class Encryption {
    public String encode(String str) {
        if (str == null) return null;
        return Base64.getEncoder().encodeToString(str.getBytes());
    }


    public String decode(String str) {
        if (str == null) return null;
        byte[] decodedBytes = Base64.getDecoder().decode(str);
        return new String(decodedBytes);
    }
}
