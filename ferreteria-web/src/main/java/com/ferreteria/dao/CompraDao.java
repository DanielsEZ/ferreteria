package com.ferreteria.dao;

import com.ferreteria.config.Db;
import com.ferreteria.model.Compra;
import com.ferreteria.model.CompraDetalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraDao {

    public int insertarCompra(Compra c) throws Exception {
        String sql = "INSERT INTO compras (proveedor_id, fecha, numero_factura, total, observaciones, usuario_id) VALUES (?, NOW(), ?, 0, ?, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getProveedorId());
            ps.setString(2, c.getNumeroFactura());
            ps.setString(3, c.getObservaciones());
            if (c.getUsuarioId() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, c.getUsuarioId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void insertarDetalle(CompraDetalle d) throws Exception {
        String sql = "INSERT INTO compras_detalle (compra_id, producto_id, cantidad, costo_unitario) VALUES (?, ?, ?, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, d.getCompraId());
            ps.setInt(2, d.getProductoId());
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(d.getCantidad()));
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(d.getCostoUnitario()));
            ps.executeUpdate();
        }
    }

    public void actualizarTotal(int compraId) throws Exception {
        String sql = "UPDATE compras c SET total = (SELECT COALESCE(SUM(subtotal),0) FROM compras_detalle WHERE compra_id=c.id) WHERE c.id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            ps.executeUpdate();
        }
    }

    public List<CompraDetalle> listarDetalles(int compraId) throws Exception {
        String sql = "SELECT cd.id, cd.compra_id, cd.producto_id, p.nombre AS producto_nombre, cd.cantidad, cd.costo_unitario " +
                "FROM compras_detalle cd JOIN productos p ON p.id=cd.producto_id WHERE cd.compra_id = ?";
        List<CompraDetalle> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CompraDetalle d = new CompraDetalle();
                    d.setId(rs.getInt("id"));
                    d.setCompraId(rs.getInt("compra_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("producto_nombre"));
                    d.setCantidad(rs.getBigDecimal("cantidad").doubleValue());
                    d.setCostoUnitario(rs.getBigDecimal("costo_unitario").doubleValue());
                    out.add(d);
                }
            }
        }
        return out;
    }

    public List<Compra> listarRecientes(int limite) throws Exception {
        String sql = "SELECT c.id, c.fecha, c.numero_factura, c.total, p.nombre AS proveedor, " +
                "COALESCE(m.cnt,0) AS movs " +
                "FROM compras c " +
                "JOIN proveedores p ON p.id=c.proveedor_id " +
                "LEFT JOIN (SELECT referencia_id, COUNT(*) cnt FROM inventario_mov WHERE referencia_tipo='COMPRA' GROUP BY referencia_id) m ON m.referencia_id = c.id " +
                "ORDER BY c.fecha DESC LIMIT ?";
        List<Compra> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Compra c = new Compra();
                    c.setId(rs.getInt("id"));
                    c.setFecha(rs.getTimestamp("fecha"));
                    c.setNumeroFactura(rs.getString("numero_factura"));
                    c.setTotal(rs.getBigDecimal("total").doubleValue());
                    c.setProveedorNombre(rs.getString("proveedor"));
                    c.setConfirmada(rs.getInt("movs") > 0);
                    out.add(c);
                }
            }
        }
        return out;
    }

    public List<Compra> listarPorRango(Timestamp desde, Timestamp hasta, Integer proveedorId) throws Exception {
        String base = "SELECT c.id, c.fecha, c.numero_factura, c.total, p.nombre AS proveedor " +
                "FROM compras c JOIN proveedores p ON p.id=c.proveedor_id WHERE c.fecha BETWEEN ? AND ?";
        String whereProv = proveedorId != null ? " AND c.proveedor_id = ?" : "";
        String order = " ORDER BY c.fecha ASC";
        String sql = base + whereProv + order;
        List<Compra> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, desde);
            ps.setTimestamp(2, hasta);
            if (proveedorId != null) ps.setInt(3, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Compra c = new Compra();
                    c.setId(rs.getInt("id"));
                    c.setFecha(rs.getTimestamp("fecha"));
                    c.setNumeroFactura(rs.getString("numero_factura"));
                    c.setTotal(rs.getBigDecimal("total").doubleValue());
                    c.setProveedorNombre(rs.getString("proveedor"));
                    out.add(c);
                }
            }
        }
        return out;
    }

    public List<CompraDetalle> listarDetallesPorRango(Timestamp desde, Timestamp hasta, Integer proveedorId) throws Exception {
        String base = "SELECT c.id AS compra_id, c.fecha, c.numero_factura, p.nombre AS proveedor, cd.producto_id, pr.nombre AS producto, cd.cantidad, cd.costo_unitario " +
                "FROM compras c " +
                "JOIN compras_detalle cd ON cd.compra_id = c.id " +
                "JOIN productos pr ON pr.id = cd.producto_id " +
                "JOIN proveedores p ON p.id = c.proveedor_id " +
                "WHERE c.fecha BETWEEN ? AND ?";
        String whereProv = proveedorId != null ? " AND c.proveedor_id = ?" : "";
        String order = " ORDER BY c.fecha ASC, c.id ASC";
        String sql = base + whereProv + order;
        List<CompraDetalle> out = new ArrayList<>();
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, desde);
            ps.setTimestamp(2, hasta);
            if (proveedorId != null) ps.setInt(3, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CompraDetalle d = new CompraDetalle();
                    d.setCompraId(rs.getInt("compra_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("producto"));
                    d.setCantidad(rs.getBigDecimal("cantidad").doubleValue());
                    d.setCostoUnitario(rs.getBigDecimal("costo_unitario").doubleValue());
                    out.add(d);
                }
            }
        }
        return out;
    }
}
