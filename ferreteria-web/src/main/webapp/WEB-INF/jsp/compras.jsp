<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Compras (Bodega)</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
  <div class="container">
    <div class="top">
      <h2>Compras recientes</h2>
      <div>
        <a class="button" href="${pageContext.request.contextPath}/bodega/compras/nuevo">Nueva compra</a>
        <a class="button" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
      </div>
    </div>

  <table>
    <thead>
      <tr>
        <th>ID</th>
        <th>Fecha</th>
        <th>Proveedor</th>
        <th>Número factura</th>
        <th>Total</th>
        <th>Acciones</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${compras}" var="c">
        <tr>
          <td>${c.id}</td>
          <td>${c.fecha}</td>
          <td>${c.proveedorNombre}</td>
          <td>${c.numeroFactura}</td>
          <td>${c.total}</td>
          <td>
            <c:choose>
              <c:when test="${c.confirmada}">
                <span class="badge">Confirmada</span>
              </c:when>
              <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/bodega/compras/confirmar" style="display:inline">
                  <input type="hidden" name="compraId" value="${c.id}" />
                  <button class="button" type="submit" onclick="return confirm('¿Confirmar esta compra y generar entradas a inventario?')">Confirmar</button>
                </form>
              </c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty compras}">
        <tr><td colspan="6">No hay compras registradas</td></tr>
      </c:if>
    </tbody>
  </table>
  </div>
</body>
</html>
