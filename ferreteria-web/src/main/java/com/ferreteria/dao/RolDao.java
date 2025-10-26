package com.ferreteria.dao;

import com.ferreteria.config.Db;
import com.ferreteria.model.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RolDao {
    public List<Rol> listar() throws Exception {
        String sql = "SELECT id, nombre FROM roles ORDER BY nombre";
        List<Rol> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rol r = new Rol();
                r.setId(rs.getInt("id"));
                r.setNombre(rs.getString("nombre"));
                out.add(r);
            }
        }
        return out;
    }
}
