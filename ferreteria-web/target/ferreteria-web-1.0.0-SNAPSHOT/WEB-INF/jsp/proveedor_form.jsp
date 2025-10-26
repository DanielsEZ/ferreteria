<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@ include file="/WEB-INF/jsp/includes/head.jspf" %>
  <title><c:choose><c:when test="${not empty proveedor}">Editar proveedor</c:when><c:otherwise>Nuevo proveedor</c:otherwise></c:choose></title>
</head>
<body>
<div class="container">
  <div class="top">
    <h2>
      <c:choose>
        <c:when test="${not empty proveedor}">Editar proveedor</c:when>
        <c:otherwise>Nuevo proveedor</c:otherwise>
      </c:choose>
    </h2>
    <a class="button btn btn-secondary" href="${pageContext.request.contextPath}/admin/proveedores">Volver</a>
  </div>

  <c:if test="${not empty error}">
    <div class="alert alert-error">${error}</div>
  </c:if>

  <form method="post" action="${pageContext.request.contextPath}/admin/proveedores/guardar">
    <c:if test="${not empty proveedor}">
      <input type="hidden" name="id" value="${proveedor.id}" />
    </c:if>
    <div class="row">
      <div>
        <label>Nombre</label>
        <input type="text" name="nombre" value="${proveedor.nombre}" required />
      </div>
    </div>
    <div class="actions">
      <button class="button btn btn-primary" type="submit">Guardar</button>
      <a class="button btn btn-secondary" href="${pageContext.request.contextPath}/admin/proveedores">Cancelar</a>
    </div>
  </form>
</div>
</body>
</html>
