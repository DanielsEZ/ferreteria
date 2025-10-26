<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String username = (String) session.getAttribute("username");
  if (username == null) {
    response.sendRedirect(request.getContextPath() + "/auth/login");
    return;
  }
  String nombre = (String) session.getAttribute("nombre");
  String rol = (String) session.getAttribute("rol");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Inicio - Ferretería</title>
  <%@ include file="/WEB-INF/jsp/includes/head.jspf" %>
</head>
<body>
  <div class="container">
  <div class="top">
    <h2>Bienvenido, <%= (nombre != null ? nombre : username) %> (<span class="badge"><%= rol %></span>)</h2>
    <a class="button btn btn-primary" href="<%= request.getContextPath() %>/auth/logout">Cerrar sesión</a>
  </div>
  <div class="cards">
    <div class="card">
      <h3>Productos</h3>
      <p>Gestión de catálogo (ADMIN).</p>
      <a class="button btn btn-primary" href="<%= request.getContextPath() %>/admin/productos">Ir a Productos</a>
    </div>
    <div class="card">
      <h3>Usuarios</h3>
      <p>Gestionar cuentas y roles (ADMIN).</p>
      <a class="button btn btn-primary" href="<%= request.getContextPath() %>/admin/usuarios">Ir a Usuarios</a>
    </div>
    <div class="card">
      <h3>Ventas</h3>
      <p>Crear pedidos (VENTAS o ADMIN).</p>
      <a class="button btn btn-primary" href="<%= request.getContextPath() %>/ventas/pedidos">Ir a Ventas</a>
    </div>
    <div class="card">
      <h3>Caja</h3>
      <p>Cobrar ventas pendientes (CAJA o ADMIN).</p>
      <a class="button btn btn-primary" href="<%= request.getContextPath() %>/caja/pendientes">Ir a Caja</a>
    </div>
    <div class="card">
      <h3>Bodega</h3>
      <p>Registrar compras (BODEGA o ADMIN).</p>
      <a class="button btn btn-primary" href="<%= request.getContextPath() %>/bodega/compras">Ir a Bodega</a>
    </div>
    <div class="card">
      <h3>Reportes</h3>
      <p>Consultas y exportes (ADMIN).</p>
      <a class="button btn btn-primary" href="<%= request.getContextPath() %>/reportes/compras">Reporte de Compras</a>
      <div style="margin-top:.5rem"></div>
      <a class="button btn btn-primary" href="<%= request.getContextPath() %>/reportes/ventas">Reporte de Ventas</a>
    </div>
  </div>
  </div>
</body>
</html>
