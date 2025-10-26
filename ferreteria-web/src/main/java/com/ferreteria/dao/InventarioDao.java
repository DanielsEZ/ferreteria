package com.ferreteria.dao;

import com.ferreteria.config.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class InventarioDao {
    public void insertarMovimientoIN(int productoId, double cantidad, double valorUnitario, int referenciaId, int usuarioId) throws Exception {
        String sql = "INSERT INTO inventario_mov (producto_id, tipo, referencia_tipo, referencia_id, cantidad, valor_unitario, usuario_id) " +
                "VALUES (?, 'IN', 'COMPRA', ?, ?, ?, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ps.setInt(2, referenciaId);
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(cantidad));
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(valorUnitario));
            ps.setInt(5, usuarioId);
            ps.executeUpdate();
        }
    }

    public void insertarMovimientoOUT(int productoId, double cantidad, double valorUnitario, int referenciaId, int usuarioId) throws Exception {
        String sql = "INSERT INTO inventario_mov (producto_id, tipo, referencia_tipo, referencia_id, cantidad, valor_unitario, usuario_id) " +
                "VALUES (?, 'OUT', 'VENTA', ?, ?, ?, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ps.setInt(2, referenciaId);
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(cantidad));
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(valorUnitario));
            ps.setInt(5, usuarioId);
            ps.executeUpdate();
        }
    }

    public void insertarMovimientoAjuste(int productoId, String tipoInOut, double cantidad, double valorUnitario, int usuarioId) throws Exception {
        String tipo = ("IN".equalsIgnoreCase(tipoInOut)) ? "IN" : "OUT";
        String sql = "INSERT INTO inventario_mov (producto_id, tipo, referencia_tipo, referencia_id, cantidad, valor_unitario, usuario_id) " +
                "VALUES (?, ?, 'AJUSTE', 0, ?, ?, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ps.setString(2, tipo);
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(cantidad));
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(valorUnitario));
            ps.setInt(5, usuarioId);
            ps.executeUpdate();
        }
    }

    public boolean existeMovimientoCompra(int compraId) throws Exception {
        String sql = "SELECT 1 FROM inventario_mov WHERE referencia_tipo='COMPRA' AND referencia_id=? LIMIT 1";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
