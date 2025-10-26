package com.ferreteria.web;

import com.ferreteria.dao.CategoriaDao;
import com.ferreteria.dao.ProductoDao;
import com.ferreteria.dao.InventarioDao;
import com.ferreteria.model.Categoria;
import com.ferreteria.model.Producto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

public class ProductoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo(); // puede ser null
        if (path == null || "/".equals(path) || "/lista".equals(path)) {
            listar(req, resp);
            return;
        }
        if ("/nuevo".equals(path)) {
            mostrarFormulario(req, resp, null);
            return;
        }
        if (path.startsWith("/editar")) {
            String idStr = req.getParameter("id");
            try {
                if (idStr == null || idStr.isBlank()) { req.setAttribute("error", "ID de producto faltante"); listar(req, resp); return; }
                int id = Integer.parseInt(idStr);
                Producto p = new ProductoDao().obtener(id);
                if (p == null) {
                    resp.sendError(404, "Producto no encontrado");
                    return;
                }
                mostrarFormulario(req, resp, p);
            } catch (Exception e) {
                throw new ServletException(e);
            }
            return;
        }
        if (path.startsWith("/desactivar")) {
            String idStr = req.getParameter("id");
            try {
                if (idStr == null || idStr.isBlank()) { req.setAttribute("error", "ID de producto faltante"); listar(req, resp); return; }
                int id = Integer.parseInt(idStr);
                new ProductoDao().desactivar(id);
                resp.sendRedirect(req.getContextPath() + "/admin/productos");
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        if (path.startsWith("/activar")) {
            String idStr = req.getParameter("id");
            try {
                if (idStr == null || idStr.isBlank()) { req.setAttribute("error", "ID de producto faltante"); listar(req, resp); return; }
                int id = Integer.parseInt(idStr);
                new ProductoDao().activar(id);
                resp.sendRedirect(req.getContextPath() + "/admin/productos?msg=Producto%20activado");
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        if (path.startsWith("/eliminar")) {
            String idStr = req.getParameter("id");
            try {
                if (idStr == null || idStr.isBlank()) { req.setAttribute("error", "ID de producto faltante"); listar(req, resp); return; }
                int id = Integer.parseInt(idStr);
                // Eliminación definitiva: borra dependencias y luego el producto
                new ProductoDao().eliminarForzado(id);
                resp.sendRedirect(req.getContextPath() + "/admin/productos?msg=Producto%20eliminado%20definitivamente");
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        if (path.startsWith("/ajustar")) {
            String idStr = req.getParameter("id");
            try {
                if (idStr == null || idStr.isBlank()) { req.setAttribute("error", "ID de producto faltante"); listar(req, resp); return; }
                int id = Integer.parseInt(idStr);
                Producto p = new ProductoDao().obtener(id);
                if (p == null) { resp.sendError(404, "Producto no encontrado"); return; }
                req.setAttribute("producto", p);
                req.getRequestDispatcher("/WEB-INF/jsp/ajuste_form.jsp").forward(req, resp);
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
        if ("/guardar".equals(path)) {
            String idStr = req.getParameter("id");
            String sku = req.getParameter("sku");
            String nombre = req.getParameter("nombre");
            String categoriaIdStr = req.getParameter("categoriaId");
            String precioCompraStr = req.getParameter("precioCompra");
            String precioVentaStr = req.getParameter("precioVenta");
            String activoStr = req.getParameter("activo");
            String stockInicialStr = req.getParameter("stockInicial");

            try {
                Producto p = new Producto();
                if (idStr != null && !idStr.isBlank()) p.setId(Integer.parseInt(idStr));
                p.setSku(sku != null ? sku.trim() : "");
                p.setNombre(nombre != null ? nombre.trim() : "");
                p.setCategoriaId(Integer.parseInt(categoriaIdStr));
                p.setPrecioCompra(Double.parseDouble(precioCompraStr));
                p.setPrecioVenta(Double.parseDouble(precioVentaStr));
                p.setActivo("on".equalsIgnoreCase(activoStr) || "1".equals(activoStr));

                // Validaciones básicas
                if (p.getSku().isEmpty() || p.getNombre().isEmpty()) {
                    req.setAttribute("error", "SKU y Nombre son obligatorios");
                    req.setAttribute("producto", p);
                    cargarCategorias(req);
                    req.getRequestDispatcher("/WEB-INF/jsp/producto_form.jsp").forward(req, resp);
                    return;
                }
                if (p.getPrecioCompra() < 0 || p.getPrecioVenta() < 0) {
                    req.setAttribute("error", "Los precios no pueden ser negativos");
                    req.setAttribute("producto", p);
                    cargarCategorias(req);
                    req.getRequestDispatcher("/WEB-INF/jsp/producto_form.jsp").forward(req, resp);
                    return;
                }

                ProductoDao dao = new ProductoDao();
                if (p.getId() > 0) {
                    dao.actualizar(p);
                } else {
                    int nuevoId = dao.insertar(p);
                    // Stock inicial como ajuste IN (opcional)
                    if (stockInicialStr != null && !stockInicialStr.isBlank()) {
                        double stockInicial = Double.parseDouble(stockInicialStr);
                        if (stockInicial > 0) {
                            HttpSession s = req.getSession(false);
                            int usuarioId = (s != null && s.getAttribute("usuarioId") != null) ? (Integer) s.getAttribute("usuarioId") : 0;
                            new InventarioDao().insertarMovimientoAjuste(nuevoId, "IN", stockInicial, p.getPrecioCompra(), usuarioId);
                        }
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/admin/productos");
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        if ("/ajustar".equals(path)) {
            String productoIdStr = req.getParameter("productoId");
            String tipo = req.getParameter("tipo"); // IN / OUT
            String cantidadStr = req.getParameter("cantidad");
            String valorStr = req.getParameter("valorUnitario");
            try {
                int productoId = Integer.parseInt(productoIdStr);
                double cantidad = Double.parseDouble(cantidadStr);
                double valor = (valorStr != null && !valorStr.isBlank()) ? Double.parseDouble(valorStr) : 0.0;
                if (cantidad <= 0) {
                    req.setAttribute("error", "La cantidad debe ser mayor a 0");
                    Producto p = new ProductoDao().obtener(productoId);
                    req.setAttribute("producto", p);
                    req.getRequestDispatcher("/WEB-INF/jsp/ajuste_form.jsp").forward(req, resp);
                    return;
                }
                HttpSession s = req.getSession(false);
                int usuarioId = (s != null && s.getAttribute("usuarioId") != null) ? (Integer) s.getAttribute("usuarioId") : 0;
                new InventarioDao().insertarMovimientoAjuste(productoId, tipo, cantidad, valor, usuarioId);
                resp.sendRedirect(req.getContextPath() + "/admin/productos");
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q");
        try {
            List<Producto> lista = new ProductoDao().listar(q);
            req.setAttribute("lista", lista);
            req.setAttribute("q", q);
            req.getRequestDispatcher("/WEB-INF/jsp/productos.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, Producto p) throws ServletException, IOException {
        try {
            if (p != null) req.setAttribute("producto", p);
            cargarCategorias(req);
            req.getRequestDispatcher("/WEB-INF/jsp/producto_form.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void cargarCategorias(HttpServletRequest req) throws Exception {
        List<Categoria> categorias = new CategoriaDao().listar();
        req.setAttribute("categorias", categorias);
    }
}
