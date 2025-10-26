package com.ferreteria.dao;

import com.ferreteria.config.Db;
import com.ferreteria.model.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDao {
    public List<Proveedor> listar() throws Exception {
        String sql = "SELECT id, nombre FROM proveedores ORDER BY nombre";
        List<Proveedor> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                out.add(p);
            }
        }
        return out;
    }

    public List<Proveedor> listar(String q) throws Exception {
        String base = "SELECT id, nombre FROM proveedores";
        String where = (q != null && !q.trim().isEmpty()) ? " WHERE nombre LIKE ?" : "";
        String order = " ORDER BY nombre";
        String sql = base + where + order;
        List<Proveedor> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (!where.isEmpty()) {
                ps.setString(1, "%" + q.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Proveedor p = new Proveedor();
                    p.setId(rs.getInt("id"));
                    p.setNombre(rs.getString("nombre"));
                    out.add(p);
                }
            }
        }
        return out;
    }

    public Proveedor obtener(int id) throws Exception {
        String sql = "SELECT id, nombre FROM proveedores WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Proveedor p = new Proveedor();
                    p.setId(rs.getInt("id"));
                    p.setNombre(rs.getString("nombre"));
                    return p;
                }
            }
        }
        return null;
    }

    public int insertar(Proveedor p) throws Exception {
        String sql = "INSERT INTO proveedores (nombre) VALUES (?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void actualizar(Proveedor p) throws Exception {
        String sql = "UPDATE proveedores SET nombre=? WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws Exception {
        String sql = "DELETE FROM proveedores WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
