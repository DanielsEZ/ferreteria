package com.ferreteria.dao;

import com.ferreteria.config.Db;
import com.ferreteria.model.Venta;
import com.ferreteria.model.VentaDetalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDao {

    public int crearVenta(Venta v) throws Exception {
        String sql = "INSERT INTO ventas (cliente_id, vendedor_id, fecha, estado, total, observaciones) VALUES (?, ?, NOW(), 'PENDIENTE', 0, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (v.getClienteId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, v.getClienteId());
            if (v.getVendedorId() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, v.getVendedorId());
            ps.setString(3, v.getObservaciones());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void insertarDetalle(VentaDetalle d) throws Exception {
        String sql = "INSERT INTO ventas_detalle (venta_id, producto_id, cantidad, precio_unitario, descuento) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, d.getVentaId());
            ps.setInt(2, d.getProductoId());
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(d.getCantidad()));
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(d.getPrecioUnitario()));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(d.getDescuento()));
            ps.executeUpdate();
        }
    }

    public void actualizarTotal(int ventaId) throws Exception {
        String sql = "UPDATE ventas v SET total = (SELECT COALESCE(SUM(subtotal),0) FROM ventas_detalle WHERE venta_id=v.id) WHERE v.id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            ps.executeUpdate();
        }
    }

    public List<Venta> listarPorEstado(String estado) throws Exception {
        String sql = "SELECT v.id, v.fecha, v.total, v.estado, COALESCE(c.nombre,'Consumidor final') AS cliente " +
                "FROM ventas v LEFT JOIN clientes c ON c.id=v.cliente_id WHERE v.estado = ? ORDER BY v.fecha DESC";
        List<Venta> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta();
                    v.setId(rs.getInt("id"));
                    v.setFecha(rs.getTimestamp("fecha"));
                    v.setTotal(rs.getBigDecimal("total").doubleValue());
                    v.setEstado(estado);
                    v.setClienteNombre(rs.getString("cliente"));
                    out.add(v);
                }
            }
        }
        return out;
    }

    public Venta obtenerConDetalles(int ventaId) throws Exception {
        Venta v = null;
        String sql = "SELECT v.id, v.fecha, v.total, v.estado, v.observaciones, c.id AS cliente_id, COALESCE(c.nombre,'Consumidor final') AS cliente_nombre " +
                "FROM ventas v LEFT JOIN clientes c ON c.id=v.cliente_id WHERE v.id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = new Venta();
                    v.setId(rs.getInt("id"));
                    v.setFecha(rs.getTimestamp("fecha"));
                    v.setTotal(rs.getBigDecimal("total").doubleValue());
                    v.setEstado(rs.getString("estado"));
                    v.setObservaciones(rs.getString("observaciones"));
                    v.setClienteId((Integer) rs.getObject("cliente_id"));
                    v.setClienteNombre(rs.getString("cliente_nombre"));
                }
            }
        }
        if (v != null) v.setDetalles(listarDetalles(ventaId));
        return v;
    }

    public List<VentaDetalle> listarDetalles(int ventaId) throws Exception {
        String sql = "SELECT vd.id, vd.venta_id, vd.producto_id, p.nombre AS producto_nombre, vd.cantidad, vd.precio_unitario, vd.descuento " +
                "FROM ventas_detalle vd JOIN productos p ON p.id=vd.producto_id WHERE vd.venta_id=?";
        List<VentaDetalle> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaDetalle d = new VentaDetalle();
                    d.setId(rs.getInt("id"));
                    d.setVentaId(rs.getInt("venta_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("producto_nombre"));
                    d.setCantidad(rs.getBigDecimal("cantidad").doubleValue());
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario").doubleValue());
                    d.setDescuento(rs.getBigDecimal("descuento").doubleValue());
                    out.add(d);
                }
            }
        }
        return out;
    }

    public void actualizarEstado(int ventaId, String estado) throws Exception {
        String sql = "UPDATE ventas SET estado=? WHERE id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, ventaId);
            ps.executeUpdate();
        }
    }

    public List<Venta> listarPorRango(Timestamp desde, Timestamp hasta) throws Exception {
        String sql = "SELECT v.id, v.fecha, v.total, v.estado, COALESCE(c.nombre,'Consumidor final') AS cliente " +
                "FROM ventas v LEFT JOIN clientes c ON c.id=v.cliente_id " +
                "WHERE v.fecha BETWEEN ? AND ? ORDER BY v.fecha ASC";
        List<Venta> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, desde);
            ps.setTimestamp(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta();
                    v.setId(rs.getInt("id"));
                    v.setFecha(rs.getTimestamp("fecha"));
                    v.setTotal(rs.getBigDecimal("total").doubleValue());
                    v.setEstado(rs.getString("estado"));
                    v.setClienteNombre(rs.getString("cliente"));
                    out.add(v);
                }
            }
        }
        return out;
    }
}
