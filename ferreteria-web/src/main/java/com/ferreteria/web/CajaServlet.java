package com.ferreteria.web;

import com.ferreteria.dao.*;
import com.ferreteria.model.Venta;
import com.ferreteria.model.VentaDetalle;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

public class CajaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path) || "/pendientes".equals(path)) {
            listarPendientes(req, resp);
            return;
        }
        if ("/venta".equals(path)) {
            String idStr = req.getParameter("id");
            try {
                int id = Integer.parseInt(idStr);
                Venta venta = new VentaDao().obtenerConDetalles(id);
                if (venta == null) { resp.sendError(404, "Venta no encontrada"); return; }
                req.setAttribute("venta", venta);
                req.setAttribute("metodos", new MetodoPagoDao().listar());
                req.getRequestDispatcher("/WEB-INF/jsp/caja_detalle.jsp").forward(req, resp);
            } catch (Exception e) {
                throw new ServletException(e);
            }
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if ("/venta/pago".equals(path)) {
            registrarPago(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void listarPendientes(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Venta> pendientes = new VentaDao().listarPorEstado("PENDIENTE");
            req.setAttribute("pendientes", pendientes);
            req.getRequestDispatcher("/WEB-INF/jsp/caja_pendientes.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void registrarPago(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ventaIdStr = req.getParameter("ventaId");
        String metodoIdStr = req.getParameter("metodoPagoId");
        String montoStr = req.getParameter("monto");
        String referencia = req.getParameter("referencia");
        try {
            int ventaId = Integer.parseInt(ventaIdStr);
            int metodoId = Integer.parseInt(metodoIdStr);
            double monto = Double.parseDouble(montoStr);

            PagoDao pagoDao = new PagoDao();
            pagoDao.insertarPago(ventaId, metodoId, monto, referencia);

            double pagado = pagoDao.totalPagado(ventaId);
            VentaDao ventaDao = new VentaDao();
            Venta venta = ventaDao.obtenerConDetalles(ventaId);
            if (venta == null) { resp.sendError(404, "Venta no encontrada"); return; }

            if (pagado + 1e-6 >= venta.getTotal()) {
                // marcar PAGADA y generar movimientos OUT
                ventaDao.actualizarEstado(ventaId, "PAGADA");
                HttpSession s = req.getSession(false);
                int usuarioId = (s != null && s.getAttribute("usuarioId") != null) ? (Integer) s.getAttribute("usuarioId") : 0;
                InventarioDao inv = new InventarioDao();
                for (VentaDetalle d : venta.getDetalles()) {
                    inv.insertarMovimientoOUT(d.getProductoId(), d.getCantidad(), d.getPrecioUnitario(), ventaId, usuarioId);
                }

                // Si el pago es en efectivo, calcular vuelto y mostrar factura
                MetodoPagoDao.MetodoPago mp = new MetodoPagoDao().obtenerPorId(metodoId);
                if (mp != null && mp.getNombre() != null && mp.getNombre().toUpperCase().contains("EFECTIVO")) {
                    double cambio = Math.max(0.0, pagado - venta.getTotal());
                    req.setAttribute("venta", ventaDao.obtenerConDetalles(ventaId));
                    req.setAttribute("montoRecibido", monto);
                    req.setAttribute("totalPagado", pagado);
                    req.setAttribute("cambio", cambio);
                    req.setAttribute("metodoNombre", mp.getNombre());
                    req.getRequestDispatcher("/WEB-INF/jsp/caja_factura.jsp").forward(req, resp);
                    return;
                }
            }
            resp.sendRedirect(req.getContextPath() + "/caja/pendientes");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
