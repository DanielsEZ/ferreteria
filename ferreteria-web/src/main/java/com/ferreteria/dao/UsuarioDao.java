package com.ferreteria.dao;

import com.ferreteria.config.Db;
import com.ferreteria.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao {

    public Usuario findByUsername(String username) throws Exception {
        String sql = "SELECT u.id, u.username, u.password_hash, u.nombre_completo, u.activo, r.nombre AS rol " +
                "FROM usuarios u JOIN roles r ON r.id = u.rol_id WHERE u.username = ?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setNombreCompleto(rs.getString("nombre_completo"));
                    u.setRol(rs.getString("rol"));
                    u.setActivo(rs.getBoolean("activo"));
                    return u;
                }
                return null;
            }
        }
    }

    public List<Usuario> listar(String q) throws Exception {
        String base = "SELECT u.id, u.username, u.nombre_completo, u.activo, r.nombre AS rol FROM usuarios u JOIN roles r ON r.id=u.rol_id";
        String where = (q != null && !q.trim().isEmpty()) ? " WHERE u.username LIKE ? OR u.nombre_completo LIKE ?" : "";
        String order = " ORDER BY u.id DESC";
        String sql = base + where + order;
        List<Usuario> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (!where.isEmpty()) {
                String like = "%" + q.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setNombreCompleto(rs.getString("nombre_completo"));
                    u.setRol(rs.getString("rol"));
                    u.setActivo(rs.getBoolean("activo"));
                    out.add(u);
                }
            }
        }
        return out;
    }

    public int insertar(String username, String passwordHash, String nombreCompleto, int rolId, boolean activo) throws Exception {
        String sql = "INSERT INTO usuarios (username, password_hash, nombre_completo, rol_id, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, nombreCompleto);
            ps.setInt(4, rolId);
            ps.setBoolean(5, activo);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void setActivo(int id, boolean activo) throws Exception {
        String sql = "UPDATE usuarios SET activo=? WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
