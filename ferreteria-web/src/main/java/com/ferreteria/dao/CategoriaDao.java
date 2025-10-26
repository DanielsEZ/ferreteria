package com.ferreteria.dao;

import com.ferreteria.config.Db;
import com.ferreteria.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDao {
    public List<Categoria> listar() throws Exception {
        String sql = "SELECT id, nombre FROM categorias ORDER BY nombre";
        List<Categoria> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setId(rs.getInt("id"));
                c.setNombre(rs.getString("nombre"));
                out.add(c);
            }
        }
        return out;
    }
}
