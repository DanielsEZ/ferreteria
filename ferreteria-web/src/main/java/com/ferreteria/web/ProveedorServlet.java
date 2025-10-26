package com.ferreteria.web;

import com.ferreteria.dao.ProveedorDao;
import com.ferreteria.model.Proveedor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class ProveedorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path) || "/lista".equals(path)) {
            listar(req, resp);
            return;
        }
        if ("/nuevo".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/jsp/proveedor_form.jsp").forward(req, resp);
            return;
        }
        if (path.startsWith("/editar")) {
            String idStr = req.getParameter("id");
            try {
                int id = Integer.parseInt(idStr);
                Proveedor p = new ProveedorDao().obtener(id);
                if (p == null) { resp.sendError(404, "Proveedor no encontrado"); return; }
                req.setAttribute("proveedor", p);
                req.getRequestDispatcher("/WEB-INF/jsp/proveedor_form.jsp").forward(req, resp);
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        if (path.startsWith("/eliminar")) {
            String idStr = req.getParameter("id");
            try {
                int id = Integer.parseInt(idStr);
                new ProveedorDao().eliminar(id);
                resp.sendRedirect(req.getContextPath() + "/admin/proveedores?msg=Proveedor%20eliminado");
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
        if ("/guardar".equals(path)) {
            String idStr = req.getParameter("id");
            String nombre = req.getParameter("nombre");
            try {
                if (nombre == null || nombre.trim().isEmpty()) {
                    req.setAttribute("error", "El nombre es obligatorio");
                    if (idStr != null && !idStr.isBlank()) {
                        Proveedor p = new Proveedor();
                        p.setId(Integer.parseInt(idStr));
                        p.setNombre(nombre);
                        req.setAttribute("proveedor", p);
                    }
                    req.getRequestDispatcher("/WEB-INF/jsp/proveedor_form.jsp").forward(req, resp);
                    return;
                }
                ProveedorDao dao = new ProveedorDao();
                if (idStr != null && !idStr.isBlank()) {
                    Proveedor p = new Proveedor();
                    p.setId(Integer.parseInt(idStr));
                    p.setNombre(nombre.trim());
                    dao.actualizar(p);
                } else {
                    Proveedor p = new Proveedor();
                    p.setNombre(nombre.trim());
                    dao.insertar(p);
                }
                resp.sendRedirect(req.getContextPath() + "/admin/proveedores?msg=Guardado");
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String q = req.getParameter("q");
            List<Proveedor> proveedores = (q == null || q.isBlank())
                    ? new ProveedorDao().listar()
                    : new ProveedorDao().listar(q);
            req.setAttribute("proveedores", proveedores);
            req.setAttribute("q", q);
            req.getRequestDispatcher("/WEB-INF/jsp/proveedores.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
