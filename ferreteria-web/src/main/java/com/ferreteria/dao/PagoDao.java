package com.ferreteria.dao;

import com.ferreteria.config.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PagoDao {
    public void insertarPago(int ventaId, int metodoPagoId, double monto, String referencia) throws Exception {
        String sql = "INSERT INTO pagos (venta_id, metodo_pago_id, monto, referencia) VALUES (?, ?, ?, ?)";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            ps.setInt(2, metodoPagoId);
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(monto));
            ps.setString(4, referencia);
            ps.executeUpdate();
        }
    }

    public double totalPagado(int ventaId) throws Exception {
        String sql = "SELECT COALESCE(SUM(monto),0) AS pagado FROM pagos WHERE venta_id=?";
        try (Connection cn = Db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("pagado").doubleValue();
            }
        }
        return 0.0;
    }
}
