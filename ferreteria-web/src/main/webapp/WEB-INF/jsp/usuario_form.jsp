<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@ include file="/WEB-INF/jsp/includes/head.jspf" %>
  <title>Nuevo usuario</title>
</head>
<body>
<div class="container">
  <div class="top">
    <h2>Nuevo usuario</h2>
    <a class="button btn btn-secondary" href="${pageContext.request.contextPath}/admin/usuarios">Volver</a>
  </div>

  <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>

  <form method="post" action="${pageContext.request.contextPath}/admin/usuarios/guardar">
    <div class="row">
      <div>
        <label>Usuario</label>
        <input type="text" name="username" required />
      </div>
      <div>
        <label>Nombre completo</label>
        <input type="text" name="nombreCompleto" required />
      </div>
    </div>

    <div class="row">
      <div>
        <label>Contraseña</label>
        <input type="password" name="password" required />
      </div>
      <div>
        <label>Rol</label>
        <select name="rolId" required>
          <option value="" disabled selected>Seleccione...</option>
          <c:forEach items="${roles}" var="r">
            <option value="${r.id}">${r.nombre}</option>
          </c:forEach>
        </select>
      </div>
    </div>

    <label><input type="checkbox" name="activo" checked /> Activo</label>

    <div class="actions">
      <button class="button btn btn-primary" type="submit">Guardar</button>
      <a class="button btn btn-secondary" href="${pageContext.request.contextPath}/admin/usuarios">Cancelar</a>
    </div>
  </form>
</div>
</body>
</html>
