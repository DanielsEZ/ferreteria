<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Factura</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
  <style>
    .totals { margin-top:1rem; }
    .totals div { display:flex; justify-content:space-between; }
  </style>
</head>
<body>
  <div class="container">
    <h2>Factura de venta #${venta.id}</h2>
    <div>
      <strong>Cliente:</strong> ${venta.clienteNombre} &nbsp; 
      <strong>Fecha:</strong> ${venta.fecha}
    </div>

    <table>
      <thead>
        <tr>
          <th>Producto</th>
          <th>Cant.</th>
          <th>Precio</th>
          <th>Desc.</th>
          <th>Subtotal</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${venta.detalles}" var="d">
          <tr>
            <td>${d.productoNombre}</td>
            <td class="num">${d.cantidad}</td>
            <td class="num">${d.precioUnitario}</td>
            <td class="num">${d.descuento}</td>
            <td class="num">${d.cantidad * d.precioUnitario - d.descuento}</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>

    <div class="totals">
      <div><span>Total</span><strong>${venta.total}</strong></div>
      <div><span>Método</span><span>${metodoNombre}</span></div>
      <div><span>Monto recibido</span><span>${montoRecibido}</span></div>
      <div><span>Vuelto</span><strong>${cambio}</strong></div>
    </div>

    <div class="actions" style="margin-top:1rem">
      <a class="button" href="${pageContext.request.contextPath}/caja/pendientes">Volver</a>
      <button class="button" onclick="window.print()">Imprimir</button>
    </div>
  </div>
</body>
</html>
