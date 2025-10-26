package com.ferreteria.web;

import com.ferreteria.dao.RolDao;
import com.ferreteria.dao.UsuarioDao;
import com.ferreteria.model.Rol;
import com.ferreteria.model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.List;

public class UsuarioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path) || "/lista".equals(path)) {
            listar(req, resp);
            return;
        }
        if ("/nuevo".equals(path)) {
            cargarRoles(req);
            req.getRequestDispatcher("/WEB-INF/jsp/usuario_form.jsp").forward(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if ("/guardar".equals(path)) {
            String username = req.getParameter("username");
            String nombre = req.getParameter("nombreCompleto");
            String password = req.getParameter("password");
            String rolIdStr = req.getParameter("rolId");
            String activoStr = req.getParameter("activo");
            try {
                if (username == null || username.isBlank() || nombre == null || nombre.isBlank() || password == null || password.isBlank() || rolIdStr == null || rolIdStr.isBlank()) {
                    req.setAttribute("error", "Todos los campos son obligatorios");
                    cargarRoles(req);
                    req.getRequestDispatcher("/WEB-INF/jsp/usuario_form.jsp").forward(req, resp);
                    return;
                }
                String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
                int rolId = Integer.parseInt(rolIdStr);
                boolean activo = "on".equalsIgnoreCase(activoStr) || "1".equals(activoStr);
                new UsuarioDao().insertar(username.trim(), hash, nombre.trim(), rolId, activo);
                resp.sendRedirect(req.getContextPath() + "/admin/usuarios?msg=Usuario%20creado");
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
            List<Usuario> usuarios = new UsuarioDao().listar(q);
            req.setAttribute("usuarios", usuarios);
            req.setAttribute("q", q);
            req.getRequestDispatcher("/WEB-INF/jsp/usuarios.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void cargarRoles(HttpServletRequest req) throws ServletException {
        try {
            List<Rol> roles = new RolDao().listar();
            req.setAttribute("roles", roles);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
