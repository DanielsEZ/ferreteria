package com.ferreteria.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hash(String raw) {
        if (raw == null) return null;
        return BCrypt.hashpw(raw, BCrypt.gensalt(10));
    }

    public static boolean matches(String raw, String hash) {
        if (hash == null || hash.isEmpty()) return false;
        // Si el valor almacenado parece un hash BCrypt (prefijo $2), usar BCrypt
        if (hash.startsWith("$2")) {
            try {
                return BCrypt.checkpw(raw, hash);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        // Si no es BCrypt, comparar como texto plano (modo sencillo para proyecto académico)
        return raw != null && raw.equals(hash);
    }
}
