<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Pedidos pendientes (Ventas)</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
  <div class="container">
    <div class="top">
      <h2>Pedidos pendientes</h2>
      <div class="search">
        <a class="button" href="${pageContext.request.contextPath}/ventas/pedidos/nuevo">Nuevo pedido</a>
        <a class="button" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
      </div>
    </div>

  <table>
    <thead>
      <tr>
        <th>ID</th>
        <th>Fecha</th>
        <th>Cliente</th>
        <th>Total</th>
        <th>Acciones</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${pendientes}" var="v">
        <tr>
          <td>${v.id}</td>
          <td>${v.fecha}</td>
          <td>${v.clienteNombre}</td>
          <td>${v.total}</td>
          <td>
            <a class="button" href="${pageContext.request.contextPath}/ventas/pedidos/anular?id=${v.id}" onclick="return confirm('¿Anular pedido?')">Anular</a>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty pendientes}">
        <tr><td colspan="5">No hay pedidos pendientes</td></tr>
      </c:if>
    </tbody>
  </table>
  </div>
</body>
</html>
