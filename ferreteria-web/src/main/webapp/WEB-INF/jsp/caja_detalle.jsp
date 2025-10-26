<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Cobrar venta</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body>
  <div class="container">
  <h2>Cobrar venta #${venta.id}</h2>
  <div>
    <strong>Cliente:</strong> ${venta.clienteNombre} &nbsp; 
    <strong>Fecha:</strong> ${venta.fecha} &nbsp; 
    <strong>Total:</strong> ${venta.total}
  </div>

  <table>
    <thead>
      <tr>
        <th>Producto</th>
        <th>Cantidad</th>
        <th>Precio</th>
        <th>Descuento</th>
        <th>Subtotal</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${venta.detalles}" var="d">
        <tr>
          <td>${d.productoNombre}</td>
          <td>${d.cantidad}</td>
          <td>${d.precioUnitario}</td>
          <td>${d.descuento}</td>
          <td>${d.cantidad * d.precioUnitario - d.descuento}</td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <h3>Registrar pago</h3>
  <form method="post" action="${pageContext.request.contextPath}/caja/venta/pago">
    <input type="hidden" name="ventaId" value="${venta.id}" />
    <div class="row">
      <div>
        <label>Método de pago</label>
        <select name="metodoPagoId" required>
          <c:forEach items="${metodos}" var="m">
            <option value="${m.id}">${m.nombre}</option>
          </c:forEach>
        </select>
      </div>
      <div>
        <label>Monto</label>
        <input type="number" step="0.01" min="0.01" name="monto" required />
      </div>
    </div>
    <label>Referencia (opcional)</label>
    <input type="text" name="referencia" />

    <div class="actions">
      <button class="button" type="submit">Registrar pago</button>
      <a class="button" href="${pageContext.request.contextPath}/caja/pendientes">Volver</a>
    </div>
  </form>
  </div>
</body>
</html>
