<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@ include file="/WEB-INF/jsp/includes/head.jspf" %>
  <title>Proveedores</title>
</head>
<body>
<div class="container">
  <div class="top">
    <h2>Proveedores</h2>
    <div class="search">
      <form method="get" action="${pageContext.request.contextPath}/admin/proveedores" class="d-flex" style="gap:.5rem">
        <input type="text" name="q" placeholder="Buscar por nombre" value="${q}" />
        <button class="button btn btn-secondary" type="submit">Buscar</button>
      </form>
      <a class="button btn btn-primary" href="${pageContext.request.contextPath}/admin/proveedores/nuevo">Nuevo</a>
      <a class="button btn btn-secondary" href="${pageContext.request.contextPath}/bodega/compras">Volver a Bodega</a>
      <a class="button btn btn-secondary" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
    </div>
  </div>

  <c:if test="${param.msg ne null}">
    <div class="alert alert-ok">${param.msg}</div>
  </c:if>

  <table class="table table-striped table-hover">
    <thead>
      <tr>
        <th>ID</th>
        <th>Nombre</th>
        <th class="text-end">Acciones</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${proveedores}" var="p">
        <tr>
          <td>${p.id}</td>
          <td>${p.nombre}</td>
          <td class="text-end">
            <a class="button btn btn-sm btn-primary" href="${pageContext.request.contextPath}/admin/proveedores/editar?id=${p.id}">Editar</a>
            <a class="button btn btn-sm btn-danger" href="${pageContext.request.contextPath}/admin/proveedores/eliminar?id=${p.id}" onclick="return confirm('¿Eliminar proveedor?')">Eliminar</a>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty proveedores}">
        <tr><td colspan="3">No hay proveedores registrados.</td></tr>
      </c:if>
    </tbody>
  </table>
</div>
</body>
</html>
