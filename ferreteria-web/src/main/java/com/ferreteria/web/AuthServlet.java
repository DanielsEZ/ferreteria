package com.ferreteria.web;

import com.ferreteria.dao.UsuarioDao;
import com.ferreteria.model.Usuario;
import com.ferreteria.security.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/login".equals(path)) {
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }
        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if ("/login".equals(path)) {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            try {
                UsuarioDao dao = new UsuarioDao();
                Usuario u = dao.findByUsername(username);
                if (u == null || !u.isActivo() || !PasswordUtil.matches(password, u.getPasswordHash())) {
                    req.setAttribute("error", "Usuario o contraseña inválidos");
                    req.getRequestDispatcher("/login.jsp").forward(req, resp);
                    return;
                }
                HttpSession session = req.getSession(true);
                session.setAttribute("usuarioId", u.getId());
                session.setAttribute("username", u.getUsername());
                session.setAttribute("nombre", u.getNombreCompleto());
                session.setAttribute("rol", u.getRol());
                resp.sendRedirect(req.getContextPath() + "/index.jsp");
                return;
            } catch (Exception e) {
                throw new ServletException("Error autenticando: " + e.getMessage(), e);
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
