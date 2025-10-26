<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Ajuste de stock</title>
  <style>
    body{font-family:Arial,sans-serif;margin:1.5rem}
    form{max-width:560px}
    label{display:block;margin-top:.6rem}
    input, select{width:100%;padding:.5rem;border:1px solid #ccc;border-radius:6px}
    .row{display:grid;grid-template-columns:1fr 1fr;gap:1rem}
    .actions{margin-top:1rem;display:flex;gap:.5rem}
    .button{display:inline-block;padding:.5rem .9rem;background:#0b5ed7;color:#fff;text-decoration:none;border-radius:6px}
    .muted{color:#777}
    .error{color:#b00020;margin:.5rem 0}
    .num{text-align:right}
  </style>
</head>
<body>
  <h2>Ajuste de stock</h2>
  <c:if test="${not empty error}"><div class="error">${error}</div></c:if>
  <div class="muted">
    <div><strong>Producto:</strong> ${producto.nombre} (${producto.sku})</div>
    <div><strong>Stock actual:</strong> <span class="num">${producto.stockActual}</span></div>
  </div>

  <form method="post" action="${pageContext.request.contextPath}/admin/productos/ajustar">
    <input type="hidden" name="productoId" value="${producto.id}" />

    <label>Tipo de ajuste</label>
    <select name="tipo" required>
      <option value="IN">Entrada (IN)</option>
      <option value="OUT">Salida (OUT)</option>
    </select>

    <div class="row">
      <div>
        <label>Cantidad</label>
        <input type="number" step="0.001" min="0.001" name="cantidad" required />
      </div>
      <div>
        <label>Valor unitario (opcional)</label>
        <input type="number" step="0.01" min="0" name="valorUnitario" placeholder="0.00" />
      </div>
    </div>

    <div class="actions">
      <button class="button" type="submit">Guardar ajuste</button>
      <a class="button" href="${pageContext.request.contextPath}/admin/productos">Cancelar</a>
    </div>
  </form>
</body>
</html>
