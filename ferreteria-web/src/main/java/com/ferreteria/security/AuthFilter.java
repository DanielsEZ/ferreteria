package com.ferreteria.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.substring(ctx.length());

        // Rutas públicas
        boolean isLogin = path.equals("/auth/login") || path.equals("/login.jsp");
        boolean isStatic = path.startsWith("/assets/") || path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".ico");

        if (isLogin || isStatic) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        boolean logged = (session != null && session.getAttribute("username") != null);
        if (!logged) {
            resp.sendRedirect(ctx + "/auth/login");
            return;
        }

        // Autorización por rol para /admin/*
        if (path.startsWith("/admin/")) {
            String rol = (String) session.getAttribute("rol");
            // Excepción: /admin/proveedores/* permitido para ADMIN o BODEGA
            if (path.startsWith("/admin/proveedores/")) {
                if (rol == null || !("ADMIN".equalsIgnoreCase(rol) || "BODEGA".equalsIgnoreCase(rol))) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso restringido a ADMIN o BODEGA");
                    return;
                }
            } else {
                if (rol == null || !"ADMIN".equalsIgnoreCase(rol)) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso restringido a ADMIN");
                    return;
                }
            }
        }

        // Autorización Bodega: /bodega/* requiere BODEGA o ADMIN
        if (path.startsWith("/bodega/")) {
            String rol = (String) session.getAttribute("rol");
            if (rol == null || !("BODEGA".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso restringido a BODEGA o ADMIN");
                return;
            }
        }

        // Autorización Ventas: /ventas/* permite ADMIN, VENTAS y USER básico
        if (path.startsWith("/ventas/")) {
            String rol = (String) session.getAttribute("rol");
            if (rol == null || !("VENTAS".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol) || "USER".equalsIgnoreCase(rol))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso restringido a VENTAS, USER o ADMIN");
                return;
            }
        }

        // Autorización Reportes: /reportes/* permite ADMIN y VENTAS
        if (path.startsWith("/reportes/")) {
            String rol = (String) session.getAttribute("rol");
            if (rol == null || !("VENTAS".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso restringido a REPORTES para VENTAS o ADMIN");
                return;
            }
        }

        // Autorización Caja: /caja/* requiere CAJA o ADMIN
        if (path.startsWith("/caja/")) {
            String rol = (String) session.getAttribute("rol");
            if (rol == null || !("CAJA".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso restringido a CAJA o ADMIN");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
