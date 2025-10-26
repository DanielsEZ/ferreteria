<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Reporte de ventas</title>
  <style>
    body{font-family:Arial,sans-serif;margin:1.5rem}
    .row{display:grid;grid-template-columns:1fr 1fr 1fr;gap:1rem}
    label{display:block;margin-top:.5rem}
    input, select{width:100%;padding:.4rem;border:1px solid #ccc;border-radius:6px}
    .actions{margin-top:1rem;display:flex;gap:.5rem}
    a.button, button.button{display:inline-block;padding:.4rem .8rem;background:#0b5ed7;color:#fff;text-decoration:none;border-radius:6px;border:none}
    table{border-collapse:collapse;width:100%;margin-top:1rem}
    th,td{border:1px solid #ddd;padding:.5rem}
    th{background:#f2f2f2}
    .num{text-align:right;white-space:nowrap}
  </style>
  <script>
    function onPeriodoChange(){
      const v = document.querySelector('input[name=periodo]:checked').value;
      const desde = document.getElementById('desde');
      const hasta = document.getElementById('hasta');
      const disabled = (v !== 'custom');
      desde.disabled = disabled; hasta.disabled = disabled;
    }
    document.addEventListener('DOMContentLoaded', onPeriodoChange);
  </script>
</head>
<body>
  <h2>Reporte de ventas</h2>
  <form method="get" action="${pageContext.request.contextPath}/reportes/ventas">
    <div class="row">
      <div>
        <label>Periodo</label>
        <div>
          <label><input type="radio" name="periodo" value="hoy" ${param.periodo == 'hoy' || empty param.periodo ? 'checked' : ''} onclick="onPeriodoChange()"/> Hoy</label>
          <label><input type="radio" name="periodo" value="mes" ${param.periodo == 'mes' ? 'checked' : ''} onclick="onPeriodoChange()"/> Mes actual</label>
          <label><input type="radio" name="periodo" value="anio" ${param.periodo == 'anio' ? 'checked' : ''} onclick="onPeriodoChange()"/> Año actual</label>
          <label><input type="radio" name="periodo" value="custom" ${param.periodo == 'custom' ? 'checked' : ''} onclick="onPeriodoChange()"/> Personalizado</label>
        </div>
      </div>
      <div>
        <label>Desde</label>
        <input type="date" id="desde" name="desde" value="${desde}" />
        <label>Hasta</label>
        <input type="date" id="hasta" name="hasta" value="${hasta}" />
      </div>
    </div>
    <div class="actions">
      <button class="button" type="submit">Ver</button>
      <a class="button" href="${pageContext.request.contextPath}/reportes/ventas/csv?${pageContext.request.queryString}">CSV</a>
      <a class="button" href="${pageContext.request.contextPath}/reportes/ventas/pdf?${pageContext.request.queryString}">PDF</a>
      <a class="button" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
    </div>
  </form>

  <table>
    <thead>
      <tr>
        <th>Fecha</th>
        <th>Cliente</th>
        <th>Estado</th>
        <th class="num">Total</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${ventas}" var="v">
        <tr>
          <td>${v.fecha}</td>
          <td>${v.clienteNombre}</td>
          <td>${v.estado}</td>
          <td class="num">${v.total}</td>
        </tr>
      </c:forEach>
      <c:if test="${empty ventas}">
        <tr><td colspan="4">Sin resultados para el período seleccionado</td></tr>
      </c:if>
    </tbody>
  </table>
</body>
</html>
