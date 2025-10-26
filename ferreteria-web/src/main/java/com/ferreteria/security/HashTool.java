package com.ferreteria.security;

public class HashTool {
    public static void main(String[] args) {
        String pwd = (args != null && args.length > 0) ? args[0] : "admin123";
        String hash = PasswordUtil.hash(pwd);
        System.out.println("Password: " + pwd);
        System.out.println("BCrypt:   " + hash);
        System.out.println("Copia y pega este hash en la columna password_hash de la tabla usuarios.");
    }
}
