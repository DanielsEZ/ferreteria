<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
  com.ferreteria.model.Producto p = (com.ferreteria.model.Producto) request.getAttribute("producto");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title><c:out value="${producto != null ? 'Editar' : 'Nuevo'}" /> Producto</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
  <div class="container">
  <h2><c:out value="${producto != null ? 'Editar' : 'Nuevo'}" /> Producto</h2>
  <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
  <form method="post" action="${pageContext.request.contextPath}/admin/productos/guardar" style="max-width:640px">
    <input type="hidden" name="id" value="${producto != null ? producto.id : ''}" />

    <label>SKU</label>
    <input type="text" name="sku" required value="${producto != null ? producto.sku : ''}" />

    <label>Nombre</label>
    <input type="text" name="nombre" required value="${producto != null ? producto.nombre : ''}" />

    <div class="row">
      <div>
        <label>Categoría</label>
        <select name="categoriaId" required>
          <option value="" disabled ${producto == null ? 'selected' : ''}>Seleccione...</option>
          <c:forEach items="${categorias}" var="c">
            <option value="${c.id}" ${producto != null && producto.categoriaId == c.id ? 'selected' : ''}>${c.nombre}</option>
          </c:forEach>
        </select>
      </div>
      <div>
        <label>Activo</label>
        <input type="checkbox" name="activo" ${producto == null || producto.activo ? 'checked' : ''} />
      </div>
    </div>

    <div class="row">
      <div>
        <label>Precio compra</label>
        <input type="number" step="0.01" min="0" name="precioCompra" required value="${producto != null ? producto.precioCompra : '0.00'}" />
      </div>
      <div>
        <label>Precio venta</label>
        <input type="number" step="0.01" min="0" name="precioVenta" required value="${producto != null ? producto.precioVenta : '0.00'}" />
      </div>
    </div>

    <c:if test="${producto == null}">
      <label>Stock inicial (opcional)</label>
      <input type="number" step="0.001" min="0" name="stockInicial" placeholder="0.000" />
    </c:if>

    <div class="actions">
      <button class="button" type="submit">Guardar</button>
      <a class="button" href="${pageContext.request.contextPath}/admin/productos">Cancelar</a>
    </div>
  </form>
  </div>
</body>
</html>
