package com.ferreteria.web;

import com.ferreteria.dao.*;
import com.ferreteria.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

public class BodegaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path) || "/compras".equals(path)) {
            listar(req, resp);
            return;
        }
        if ("/compras/nuevo".equals(path)) {
            mostrarFormulario(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if ("/compras/guardar".equals(path)) {
            guardar(req, resp);
            return;
        }
        if ("/compras/confirmar".equals(path)) {
            confirmar(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Compra> compras = new CompraDao().listarRecientes(50);
            req.setAttribute("compras", compras);
            req.getRequestDispatcher("/WEB-INF/jsp/compras.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Proveedor> proveedores = new ProveedorDao().listar();
            List<Producto> productos = new ProductoDao().listarActivos();
            // Logging simple para diagnosticar tamaños
            System.out.println("[BodegaServlet] proveedores.size=" + (proveedores != null ? proveedores.size() : -1));
            System.out.println("[BodegaServlet] productos.size=" + (productos != null ? productos.size() : -1));
            req.setAttribute("proveedores", proveedores);
            req.setAttribute("productos", productos);
            req.getRequestDispatcher("/WEB-INF/jsp/compra_form.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String proveedorIdStr = req.getParameter("proveedorId");
        String numeroFactura = req.getParameter("numeroFactura");
        String observaciones = req.getParameter("observaciones");
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades = req.getParameterValues("cantidad");
        String[] costos = req.getParameterValues("costoUnitario");
        try {
            Compra compra = new Compra();
            compra.setProveedorId(Integer.parseInt(proveedorIdStr));
            compra.setNumeroFactura(numeroFactura);
            compra.setObservaciones(observaciones);
            HttpSession s = req.getSession(false);
            Integer usuarioId = (s != null && s.getAttribute("usuarioId") != null) ? (Integer) s.getAttribute("usuarioId") : null;
            compra.setUsuarioId(usuarioId);

            int compraId = new CompraDao().insertarCompra(compra);

            if (productoIds != null) {
                for (int i = 0; i < productoIds.length; i++) {
                    if (productoIds[i] == null || productoIds[i].isBlank()) continue;
                    int prodId = Integer.parseInt(productoIds[i]);
                    double cant = Double.parseDouble(cantidades[i]);
                    double costo = Double.parseDouble(costos[i]);
                    if (cant <= 0 || costo < 0) continue;
                    CompraDetalle d = new CompraDetalle();
                    d.setCompraId(compraId);
                    d.setProductoId(prodId);
                    d.setCantidad(cant);
                    d.setCostoUnitario(costo);
                    new CompraDao().insertarDetalle(d);
                }
            }
            new CompraDao().actualizarTotal(compraId);
            resp.sendRedirect(req.getContextPath() + "/bodega/compras");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void confirmar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String compraIdStr = req.getParameter("compraId");
        try {
            int compraId = Integer.parseInt(compraIdStr);
            InventarioDao inv = new InventarioDao();
            // Idempotencia: no duplicar confirmaciones
            if (inv.existeMovimientoCompra(compraId)) {
                resp.sendRedirect(req.getContextPath() + "/bodega/compras?msg=Compra%20ya%20confirmada");
                return;
            }

            List<CompraDetalle> detalles = new CompraDao().listarDetalles(compraId);
            HttpSession s = req.getSession(false);
            int usuarioId = (s != null && s.getAttribute("usuarioId") != null) ? (Integer) s.getAttribute("usuarioId") : 0;
            for (CompraDetalle d : detalles) {
                inv.insertarMovimientoIN(d.getProductoId(), d.getCantidad(), d.getCostoUnitario(), compraId, usuarioId);
            }
            resp.sendRedirect(req.getContextPath() + "/bodega/compras?msg=Compra%20confirmada");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
