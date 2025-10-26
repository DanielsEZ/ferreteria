package com.ferreteria.dao;

import com.ferreteria.config.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MetodoPagoDao {
    public static class MetodoPago {
        public int id;
        public String nombre;

        public int getId() { return id; }
        public String getNombre() { return nombre; }
    }

    public List<MetodoPago> listar() throws Exception {
        String sql = "SELECT id, nombre FROM metodos_pago ORDER BY nombre";
        List<MetodoPago> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MetodoPago m = new MetodoPago();
                m.id = rs.getInt("id");
                m.nombre = rs.getString("nombre");
                out.add(m);
            }
        }
        return out;
    }

    public MetodoPago obtenerPorId(int id) throws Exception {
        String sql = "SELECT id, nombre FROM metodos_pago WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MetodoPago m = new MetodoPago();
                    m.id = rs.getInt("id");
                    m.nombre = rs.getString("nombre");
                    return m;
                }
            }
        }
        return null;
    }
}
