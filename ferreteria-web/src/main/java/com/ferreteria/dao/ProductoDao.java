package com.ferreteria.dao;

import com.ferreteria.config.Db;
import com.ferreteria.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDao {

    public List<Producto> listar(String q) throws Exception {
        String base = "SELECT p.id, p.sku, p.nombre, p.categoria_id, c.nombre AS categoria_nombre, " +
                "p.precio_compra, p.precio_venta, p.activo, COALESCE(v.stock_actual,0) AS stock_actual FROM productos p " +
                "JOIN categorias c ON c.id = p.categoria_id " +
                "LEFT JOIN vw_existencias v ON v.producto_id = p.id";
        String where = (q != null && !q.isBlank()) ? " WHERE p.nombre LIKE ? OR p.sku LIKE ?" : "";
        String order = " ORDER BY p.nombre";
        String sql = base + where + order;
        List<Producto> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
            PreparedStatement ps = cn.prepareStatement(sql)) {
            if (!where.isEmpty()) {
                String like = "%" + q.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = map(rs);
                    out.add(p);
                }
            }
        }
        return out;
    }

    public List<Producto> listarActivos() throws Exception {
        String sql = "SELECT p.id, p.sku, p.nombre, p.categoria_id, c.nombre AS categoria_nombre, " +
                "p.precio_compra, p.precio_venta, p.activo, COALESCE(v.stock_actual,0) AS stock_actual " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON c.id = p.categoria_id " +
                "LEFT JOIN vw_existencias v ON v.producto_id = p.id " +
                "WHERE p.activo=1 ORDER BY p.nombre";
        List<Producto> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
            PreparedStatement ps = cn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
        }
        return out;
    }

    public Producto obtener(int id) throws Exception {
        String sql = "SELECT p.id, p.sku, p.nombre, p.categoria_id, c.nombre AS categoria_nombre, " +
                "p.precio_compra, p.precio_venta, p.activo, COALESCE(v.stock_actual,0) AS stock_actual FROM productos p " +
                "JOIN categorias c ON c.id = p.categoria_id " +
                "LEFT JOIN vw_existencias v ON v.producto_id = p.id " +
                "WHERE p.id = ?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    public double stockActual(int productoId) throws Exception {
        String sql = "SELECT COALESCE(v.stock_actual,0) AS stock_actual FROM vw_existencias v WHERE v.producto_id = ?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("stock_actual").doubleValue();
                }
            }
        }
        return 0.0;
    }

    public int insertar(Producto p) throws Exception {
        String sql = "INSERT INTO productos (sku, nombre, categoria_id, precio_compra, precio_venta, activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getSku());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getCategoriaId());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(p.getPrecioCompra()));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(p.getPrecioVenta()));
            ps.setBoolean(6, p.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void actualizar(Producto p) throws Exception {
        String sql = "UPDATE productos SET sku=?, nombre=?, categoria_id=?, precio_compra=?, precio_venta=?, activo=? WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getSku());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getCategoriaId());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(p.getPrecioCompra()));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(p.getPrecioVenta()));
            ps.setBoolean(6, p.isActivo());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws Exception {
        String sql = "UPDATE productos SET activo=0 WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void activar(int id) throws Exception {
        String sql = "UPDATE productos SET activo=1 WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws Exception {
        String sql = "DELETE FROM productos WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            // Re-lanzar para que el Servlet muestre un mensaje amigable
            throw e;
        }
    }

    /**
     * Elimina un producto de forma definitiva eliminando antes sus dependencias
     * en tablas de detalle e inventario. Peligroso para historial.
     */
    public void eliminarForzado(int id) throws Exception {
        try (Connection cn = Db.getConnection()) {
            boolean oldAuto = cn.getAutoCommit();
            cn.setAutoCommit(false);
            try {
                // Borrar movimientos de inventario
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM inventario_mov WHERE producto_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // Borrar detalles de compras
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM compras_detalle WHERE producto_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // Borrar detalles de ventas
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM ventas_detalle WHERE producto_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // Finalmente borrar el producto
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM productos WHERE id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                cn.commit();
                cn.setAutoCommit(oldAuto);
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            }
        }
    }

    private Producto map(ResultSet rs) throws Exception {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setSku(rs.getString("sku"));
        p.setNombre(rs.getString("nombre"));
        p.setCategoriaId(rs.getInt("categoria_id"));
        p.setCategoriaNombre(rs.getString("categoria_nombre"));
        p.setPrecioCompra(rs.getBigDecimal("precio_compra").doubleValue());
        p.setPrecioVenta(rs.getBigDecimal("precio_venta").doubleValue());
        p.setActivo(rs.getBoolean("activo"));
        // stock_actual es opcional según el SELECT
        try {
            Object stockObj = rs.getObject("stock_actual");
            if (stockObj != null) {
                p.setStockActual(rs.getBigDecimal("stock_actual").doubleValue());
            }
        } catch (Exception ignore) { /* columna no presente en algunos SELECTs */ }
        return p;
    }
}
