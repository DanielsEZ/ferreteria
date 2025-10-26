<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@ include file="/WEB-INF/jsp/includes/head.jspf" %>
  <title>Usuarios</title>
</head>
<body>
<div class="container">
  <div class="top">
    <h2>Usuarios</h2>
    <div class="search">
      <form method="get" action="${pageContext.request.contextPath}/admin/usuarios" class="d-flex" style="gap:.5rem">
        <input type="text" name="q" placeholder="Buscar por usuario o nombre" value="${q}" />
        <button class="button btn btn-secondary" type="submit">Buscar</button>
      </form>
      <a class="button btn btn-primary" href="${pageContext.request.contextPath}/admin/usuarios/nuevo">Nuevo</a>
      <a class="button btn btn-secondary" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
    </div>
  </div>

  <c:if test="${param.msg ne null}"><div class="alert alert-ok">${param.msg}</div></c:if>

  <table class="table table-striped table-hover">
    <thead>
      <tr>
        <th>ID</th>
        <th>Usuario</th>
        <th>Nombre</th>
        <th>Rol</th>
        <th>Estado</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${usuarios}" var="u">
        <tr>
          <td>${u.id}</td>
          <td>${u.username}</td>
          <td>${u.nombreCompleto}</td>
          <td>${u.rol}</td>
          <td>
            <c:choose>
              <c:when test="${u.activo}">Activo</c:when>
              <c:otherwise><span class="muted">Inactivo</span></c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty usuarios}">
        <tr><td colspan="5">No hay usuarios</td></tr>
      </c:if>
    </tbody>
  </table>
</div>
</body>
</html>
