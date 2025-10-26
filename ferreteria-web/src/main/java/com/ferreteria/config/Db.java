package com.ferreteria.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Db {
    private static String url;
    private static String user;
    private static String password;
    private static String driver;
    private static volatile boolean initialized = false;

    private static void init() {
        if (initialized) return;
        synchronized (Db.class) {
            if (initialized) return;
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                Properties p = new Properties();
                if (is == null) throw new RuntimeException("No se encuentra db.properties en el classpath");
                p.load(is);
                url = p.getProperty("jdbc.url");
                user = p.getProperty("jdbc.user");
                password = p.getProperty("jdbc.password");
                driver = p.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver");
                Class.forName(driver);
                initialized = true;
            } catch (Exception e) {
                throw new RuntimeException("Error cargando configuración de BD: " + e.getMessage(), e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) init();
        return DriverManager.getConnection(url, user, password);
    }
}
