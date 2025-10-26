<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Productos</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
  <div class="container">
    <div class="top">
      <h2>Productos</h2>
      <div>
        <form class="search" method="get" action="">
          <input type="text" name="q" placeholder="Buscar por nombre o SKU" value="${q}" />
          <button class="button" type="submit">Buscar</button>
          <a class="button" href="${pageContext.request.contextPath}/admin/productos/nuevo">Nuevo</a>
          <a class="button" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
        </form>
      </div>

    <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
    <c:if test="${not empty param.msg}"><div class="alert alert-ok">${param.msg}</div></c:if>

    <table>
    <thead>
      <tr>
        <th>ID</th>
        <th>SKU</th>
        <th>Nombre</th>
        <th>Categoría</th>
        <th>Precio Compra</th>
        <th>Precio Venta</th>
        <th>Stock</th>
        <th>Estado</th>
        <th>Acciones</th>
      </tr>
    </thead>
  <tbody>
    <c:forEach items="${lista}" var="p">
      <tr>
        <td>${p.id}</td>
        <td>${p.sku}</td>
        <td>${p.nombre}</td>
        <td>${p.categoriaNombre}</td>
        <td class="num">${p.precioCompra}</td>
        <td class="num">${p.precioVenta}</td>
        <td class="num">${p.stockActual}</td>
        <td>
          <c:choose>
            <c:when test="${p.activo}">Activo</c:when>
            <c:otherwise><span class="muted">Inactivo</span></c:otherwise>
          </c:choose>
        </td>
        <td>
          <a class="button" href="${pageContext.request.contextPath}/admin/productos/editar?id=${p.id}">Editar</a>
          <a class="button" href="${pageContext.request.contextPath}/admin/productos/ajustar?id=${p.id}">Ajustar</a>
          <c:choose>
            <c:when test="${p.activo}">
              <a class="button" href="${pageContext.request.contextPath}/admin/productos/desactivar?id=${p.id}" onclick="return confirm('¿Desactivar producto?')">Desactivar</a>
            </c:when>
            <c:otherwise>
              <a class="button" href="${pageContext.request.contextPath}/admin/productos/activar?id=${p.id}" onclick="return confirm('¿Activar producto?')">Activar</a>
            </c:otherwise>
          </c:choose>
          <a class="button" href="${pageContext.request.contextPath}/admin/productos/eliminar?id=${p.id}" onclick="return confirm('¿Eliminar producto de forma permanente? Esta acción no se puede deshacer.')">Eliminar</a>
        </td>
      </tr>
    </c:forEach>
    <c:if test="${empty lista}">
      <tr><td colspan="9" class="muted">Sin resultados</td></tr>
    </c:if>
  </tbody>
  </table>
  </div>
</body>
</html>
