package com.ferreteria.web;

import com.ferreteria.dao.ClienteDao;
import com.ferreteria.dao.ProductoDao;
import com.ferreteria.dao.VentaDao;
import com.ferreteria.model.Venta;
import com.ferreteria.model.VentaDetalle;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

public class VentaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path) || "/pedidos".equals(path)) {
            listarPendientes(req, resp);
            return;
        }
        if ("/pedidos/nuevo".equals(path)) {
            mostrarFormulario(req, resp);
            return;
        }
        if ("/pedidos/anular".equals(path)) {
            String idStr = req.getParameter("id");
            try {
                int id = Integer.parseInt(idStr);
                new VentaDao().actualizarEstado(id, "ANULADA");
                resp.sendRedirect(req.getContextPath() + "/ventas/pedidos");
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if ("/pedidos/guardar".equals(path)) {
            guardar(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void listarPendientes(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Venta> pendientes = new VentaDao().listarPorEstado("PENDIENTE");
            req.setAttribute("pendientes", pendientes);
            req.getRequestDispatcher("/WEB-INF/jsp/ventas.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("clientes", new ClienteDao().listar());
            req.setAttribute("productos", new ProductoDao().listarActivos());
            req.getRequestDispatcher("/WEB-INF/jsp/venta_form.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String clienteIdStr = req.getParameter("clienteId");
        String clienteNombreLibre = req.getParameter("clienteNombreLibre");
        String observaciones = req.getParameter("observaciones");
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades = req.getParameterValues("cantidad");
        String[] precios = req.getParameterValues("precioUnitario");
        String[] descuentos = req.getParameterValues("descuento");
        try {
            HttpSession s = req.getSession(false);
            Integer vendedorId = (s != null && s.getAttribute("usuarioId") != null) ? (Integer) s.getAttribute("usuarioId") : null;
            Venta venta = new Venta();
            venta.setVendedorId(vendedorId);
            // Si se escribió un nombre libre de cliente, crear el cliente al vuelo y usar su ID
            if (clienteNombreLibre != null && !clienteNombreLibre.isBlank()) {
                int nuevoClienteId = new ClienteDao().insertar(clienteNombreLibre.trim());
                if (nuevoClienteId > 0) {
                    venta.setClienteId(nuevoClienteId);
                }
            } else if (clienteIdStr != null && !clienteIdStr.isBlank()) {
                venta.setClienteId(Integer.parseInt(clienteIdStr));
            }
            venta.setObservaciones(observaciones);

            // Validación de stock antes de crear la venta
            if (productoIds != null) {
                ProductoDao pdao = new ProductoDao();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < productoIds.length; i++) {
                    if (productoIds[i] == null || productoIds[i].isBlank()) continue;
                    int prodId = Integer.parseInt(productoIds[i]);
                    double cant = Double.parseDouble(cantidades[i]);
                    if (cant <= 0) continue;
                    double stock = pdao.stockActual(prodId);
                    if (cant > stock) {
                        // Buscar nombre de producto para mensaje claro
                        var prod = pdao.obtener(prodId);
                        String nombre = (prod != null ? prod.getNombre() : ("ID " + prodId));
                        sb.append("- ").append(nombre).append(": solicitado ").append(cant)
                          .append(", disponible ").append(stock).append("\n");
                    }
                }
                if (sb.length() > 0) {
                    req.setAttribute("error", "No se puede guardar el pedido. Stock insuficiente en:\n" + sb.toString());
                    // Re-cargar datos necesarios para el formulario
                    req.setAttribute("clientes", new ClienteDao().listar());
                    req.setAttribute("productos", new ProductoDao().listarActivos());
                    req.getRequestDispatcher("/WEB-INF/jsp/venta_form.jsp").forward(req, resp);
                    return;
                }
            }

            VentaDao dao = new VentaDao();
            int ventaId = dao.crearVenta(venta);

            if (productoIds != null) {
                for (int i = 0; i < productoIds.length; i++) {
                    if (productoIds[i] == null || productoIds[i].isBlank()) continue;
                    int prodId = Integer.parseInt(productoIds[i]);
                    double cant = Double.parseDouble(cantidades[i]);
                    double precio = Double.parseDouble(precios[i]);
                    double desc = (descuentos != null && descuentos.length > i && descuentos[i] != null && !descuentos[i].isBlank()) ? Double.parseDouble(descuentos[i]) : 0.0;
                    if (cant <= 0 || precio < 0 || desc < 0) continue;
                    VentaDetalle d = new VentaDetalle();
                    d.setVentaId(ventaId);
                    d.setProductoId(prodId);
                    d.setCantidad(cant);
                    d.setPrecioUnitario(precio);
                    d.setDescuento(desc);
                    dao.insertarDetalle(d);
                }
            }
            dao.actualizarTotal(ventaId);
            resp.sendRedirect(req.getContextPath() + "/ventas/pedidos");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
