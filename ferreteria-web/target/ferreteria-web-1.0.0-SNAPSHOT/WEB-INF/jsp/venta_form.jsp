<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Nuevo pedido</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
  <style>
    .stock-cell { text-align: right; min-width: 80px; }
    .error { color:#b30000; }
    .invalid { outline: 2px solid #b30000; }
  </style>
  <script>
    function productoYaSeleccionado(productoId) {
      // Verificar si el producto ya está seleccionado en algún otro select
      var selects = document.querySelectorAll('select[name="productoId"]');
      for (var i = 0; i < selects.length; i++) {
        if (selects[i].value === productoId) {
          return true; // El producto ya está seleccionado
        }
      }
      return false; // El producto no está seleccionado
    }
    
    function actualizarOpcionesDisponibles() {
      var selects = document.querySelectorAll('select[name="productoId"]');
      var selectedValues = [];
      
      // Recolectar todos los valores seleccionados
      selects.forEach(function(select) {
        if (select.value) {
          selectedValues.push(select.value);
        }
      });
      
      // Actualizar cada select
      selects.forEach(function(select) {
        var currentValue = select.value;
        
        // Actualizar cada opción
        Array.from(select.options).forEach(function(option) {
          if (option.value) { // No afectar la opción por defecto
            // Habilitar/deshabilitar según si ya está seleccionado en otro lugar
            option.disabled = selectedValues.includes(option.value) && option.value !== currentValue;
          }
        });
      });
    }
    
    function addRow() {
      var tbody = document.getElementById('items');
      var tr = document.createElement('tr');
      
      // Obtener el primer select de productos para copiar sus opciones
      var firstSelect = document.querySelector('select[name="productoId"]');
      var optionsHtml = '';
      
      if (firstSelect) {
        // Clonar las opciones del primer select
        var options = firstSelect.querySelectorAll('option');
        options.forEach(function(option) {
          if (option.value) { // No copiar la opción por defecto vacía
            // Solo incluir opciones que no estén ya seleccionadas
            if (!productoYaSeleccionado(option.value)) {
              optionsHtml += option.outerHTML;
            }
          }
        });
      }
      
      // Si no hay opciones disponibles, mostrar mensaje y no agregar fila
      if (optionsHtml === '') {
        alert('No hay más productos disponibles para agregar.');
        return;
      }
      
      tr.innerHTML =
        '<td>'+
          '<select name="productoId" required onchange="updateStock(this); actualizarOpcionesDisponibles();">'+
            '<option value="" disabled selected>Seleccione...</option>'+
            optionsHtml +
          '</select>'+
        '</td>'+
        '<td class="stock-cell"></td>'+
        '<td><input type="number" step="0.001" min="0.001" name="cantidad" required value="1" oninput="capCantidad(this)" /></td>'+
        '<td><input type="number" step="0.01" min="0" name="precioUnitario" required value="0.00" /></td>'+
        '<td><input type="number" step="0.01" min="0" name="descuento" value="0.00" /></td>'+
        '<td><button type="button" onclick="this.closest(\'tr\').remove(); actualizarOpcionesDisponibles();">Quitar</button></td>';
      
      tbody.appendChild(tr);
      actualizarOpcionesDisponibles();
    }

    function updateStock(selectEl){
      if (!selectEl) return;
      
      // Verificar si el producto ya está seleccionado en otro renglón
      if (selectEl.value && productoYaSeleccionado(selectEl.value)) {
        // Buscar si hay otro select con el mismo valor
        var selects = document.querySelectorAll('select[name="productoId"]');
        for (var i = 0; i < selects.length; i++) {
          if (selects[i] !== selectEl && selects[i].value === selectEl.value) {
            alert('Este producto ya ha sido seleccionado en otro renglón.');
            selectEl.value = ''; // Restablecer la selección
            var stockCell = selectEl.closest('tr').querySelector('.stock-cell');
            if (stockCell) stockCell.textContent = '';
            return;
          }
        }
      }
      
      var tr = selectEl.closest('tr');
      var stockCell = tr ? tr.querySelector('.stock-cell') : null;
      var opt = selectEl.options[selectEl.selectedIndex];
      var stock = (opt && opt.dataset && opt.dataset.stock) ? opt.dataset.stock : '';
      var precioVenta = (opt && opt.dataset && opt.dataset.precio) ? opt.dataset.precio : '0.00';
      
      if (stockCell) stockCell.textContent = stock;
      
      // Establecer el precio unitario automáticamente
      var precioInput = tr.querySelector('input[name="precioUnitario"]');
      if (precioInput) {
        precioInput.value = parseFloat(precioVenta).toFixed(2);
      }
      
      // setear max en cantidad
      var qty = tr.querySelector('input[name="cantidad"]');
      if (qty) {
        var max = parseFloat(stock || '0');
        if (!isNaN(max)) {
          qty.max = max;
          capCantidad(qty);
        }
      }
      
      actualizarOpcionesDisponibles();
    }

    function capCantidad(input){
      var tr = input.closest('tr');
      var selectEl = tr.querySelector('select[name="productoId"]');
      var opt = selectEl ? selectEl.options[selectEl.selectedIndex] : null;
      var stock = opt && opt.dataset ? parseFloat(opt.dataset.stock || '0') : 0;
      var val = parseFloat(input.value || '0');
      input.classList.remove('invalid');
      if (!isNaN(stock) && !isNaN(val) && val > stock) {
        // marcar inválido visualmente
        input.classList.add('invalid');
      }
    }

    function validarSubmit(e){
      var rows = document.querySelectorAll('#items tr');
      var errores = [];
      rows.forEach(function(tr, idx){
        var sel = tr.querySelector('select[name="productoId"]');
        var qty = tr.querySelector('input[name="cantidad"]');
        if (!sel || !qty) return;
        var opt = sel.options[sel.selectedIndex];
        var stock = opt && opt.dataset ? parseFloat(opt.dataset.stock || '0') : 0;
        var val = parseFloat(qty.value || '0');
        if (!isNaN(stock) && !isNaN(val) && val > stock) {
          errores.push('- Línea ' + (idx+1) + ': solicitado ' + val + ', disponible ' + stock);
          qty.classList.add('invalid');
        }
      });
      if (errores.length > 0) {
        alert('No se puede guardar el pedido. Stock insuficiente en:\n' + errores.join('\n'));
        e.preventDefault();
        return false;
      }
      return true;
    }

    function toggleClienteSelector(input){
      var sel = document.querySelector('select[name="clienteId"]');
      if (!sel) return;
      var hasFreeName = input && input.value && input.value.trim().length > 0;
      sel.disabled = hasFreeName;
      if (hasFreeName) sel.value = '';
    }
  </script>
</head>
<body>
  <div class="container">
  <h2>Nuevo pedido</h2>
  <c:if test="${not empty error}"><div class="alert alert-error"><pre style="white-space:pre-wrap">${error}</pre></div></c:if>
  <c:if test="${empty productos}">
    <div class="alert alert-error">No hay productos activos para seleccionar. Ve a Productos y agrega o activa algunos.</div>
  </c:if>
  <form method="post" action="${pageContext.request.contextPath}/ventas/pedidos/guardar" onsubmit="return validarSubmit(event)">
    <div class="row">
      <div>
        <label>Nombre del cliente (rápido)</label>
        <input type="text" name="clienteNombreLibre" placeholder="Ej: Juan Pérez" oninput="toggleClienteSelector(this)" />
        <div class="muted">Si escribes aquí, se creará el cliente automáticamente y se usará este nombre.</div>
      </div>
      <div>
        <label>Cliente</label>
        <select name="clienteId">
          <option value="">Consumidor final</option>
          <c:forEach items="${clientes}" var="c">
            <option value="${c.id}">${c.nombre}</option>
          </c:forEach>
        </select>
      </div>
      <div>
        <label>Observaciones</label>
        <input type="text" name="observaciones" />
      </div>
    </div>

    <h3>Items</h3>
    <table id="items">
      <thead>
        <tr>
          <th>Producto</th>
          <th>Stock</th>
          <th>Cantidad</th>
          <th>Precio unitario</th>
          <th>Descuentos</th>
          <th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>
            <select name="productoId" required onchange="updateStock(this)">
              <option value="" disabled selected>Seleccione...</option>
              <c:forEach items="${productos}" var="p">
                <option value="${p.id}" data-stock="${p.stockActual}" data-precio="${p.precioVenta}">${p.nombre} (${p.sku}) - Q${p.precioVenta}</option>
              </c:forEach>
            </select>
          </td>
          <td class="stock-cell"></td>
          <td><input type="number" step="0.001" min="0.001" name="cantidad" required value="1" /></td>
          <td><input type="number" step="0.01" min="0" name="precioUnitario" required value="0.00" /></td>
          <td><input type="number" step="0.01" min="0" name="descuento" value="0.00" /></td>
          <td><button type="button" onclick="this.closest('tr').remove()">Quitar</button></td>
        </tr>
      </tbody>
    </table>
    <button class="button" type="button" id="btnAdd" onclick="addRow()">Agregar item</button>

    <div class="actions">
      <button class="button" type="submit">Guardar</button>
      <a class="button" href="${pageContext.request.contextPath}/ventas/pedidos">Cancelar</a>
    </div>
  </form>
