<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Nueva compra</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
  <style>
    .producto-seleccionado { display: none; }
    .stock-info { font-size: 0.8em; color: #666; margin-top: 2px; }
    .error-message { color: #d32f2f; font-size: 0.85em; margin-top: 4px; display: none; }
  </style>
  <script>
    // Objeto para mantener el stock de productos
    const productosStock = {};
    
    // Función para verificar si un producto ya está seleccionado
    function productoYaSeleccionado(productoId) {
      const selects = document.querySelectorAll('select[name="productoId"]');
      for (let i = 0; i < selects.length; i++) {
        if (selects[i].value === productoId) {
          return true;
        }
      }
      return false;
    }
    
    // Inicializar el stock de productos
    document.addEventListener('DOMContentLoaded', function() {
      // Ocultar opciones ya seleccionadas
      actualizarOpcionesDisponibles();
      
      // Agregar evento para actualizar las opciones cuando se elimina un producto
      const itemsContainer = document.getElementById('items');
      if (itemsContainer) {
        itemsContainer.addEventListener('click', function(e) {
          if (e.target.matches('button') || e.target.closest('button')) {
            setTimeout(actualizarOpcionesDisponibles, 0);
          }
        });
      }
    });
    
    // Función para actualizar las opciones disponibles en los selects
    function actualizarOpcionesDisponibles() {
      console.log('Actualizando opciones disponibles...');
      const selects = document.querySelectorAll('select[name="productoId"]');
      if (selects.length === 0) return;
      
      // Obtener el select original con todas las opciones
      const selectOriginal = document.querySelector('select[name="productoId"]:not(.clonado)');
      if (!selectOriginal) return;
      
      // Obtener todos los valores seleccionados
      const selectedValues = [];
      selects.forEach(select => {
        if (select.value) {
          selectedValues.push(select.value);
        }
      });
      
      // Actualizar cada select
      selects.forEach(select => {
        const currentValue = select.value;
        const isOriginal = (select === selectOriginal);
        
        // Si es el select original, solo necesitamos asegurarnos de que tenga todas las opciones
        if (isOriginal) {
          // Verificar que tenga todas las opciones
          if (select.options.length !== selectOriginal.options.length) {
            // Si no tiene todas las opciones, restaurarlas
            select.innerHTML = selectOriginal.innerHTML;
          }
          return;
        }
        
        // Para selects clonados
        const selectedOption = select.options[select.selectedIndex];
        
        // Limpiar las opciones actuales, excepto la primera
        while (select.options.length > 1) {
          select.remove(1);
        }
        
        // Agregar opción por defecto
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = 'Seleccione...';
        defaultOption.disabled = true;
        defaultOption.selected = !currentValue;
        
        // Agregar opciones disponibles
        Array.from(selectOriginal.options).forEach(opt => {
          if (!opt.value) return; // Saltar opción por defecto
          
          // Solo agregar si no está seleccionado en otro select o es el valor actual de este select
          if (opt.value === currentValue || !selectedValues.includes(opt.value)) {
            const newOption = opt.cloneNode(true);
            select.add(newOption);
          }
        });
        
        // Restaurar la opción seleccionada si existía y sigue disponible
        if (selectedOption && selectedOption.value) {
          // Verificar si la opción aún está disponible
          const optionExists = Array.from(select.options).some(opt => opt.value === selectedOption.value);
          if (optionExists) {
            select.value = selectedOption.value;
          } else {
            select.selectedIndex = 0; // Seleccionar la opción por defecto
          }
        }
      });
      
      console.log('Opciones actualizadas');
    }
    
    // Función para obtener las opciones de productos disponibles
    function getOpcionesProductosDisponibles() {
      // Obtener el primer select que tiene todas las opciones
      const primerSelect = document.querySelector('select[name="productoId"]');
      if (!primerSelect) return '';
      
      // Obtener todos los valores ya seleccionados
      const selectedProducts = [];
      document.querySelectorAll('select[name="productoId"]').forEach(select => {
        if (select.value) {
          selectedProducts.push(select.value);
        }
      });
      
      // Construir las opciones disponibles
      let optionsHtml = '';
      primerSelect.querySelectorAll('option').forEach(option => {
        if (option.value && !selectedProducts.includes(option.value)) {
          optionsHtml += option.outerHTML;
        }
      });
      
      return optionsHtml;
    }
    
    // Función para agregar una nueva fila
    function addRow() {
      console.log('Función addRow() llamada');
      const tbody = document.getElementById('items');
      if (!tbody) {
        console.error('No se encontró el elemento con ID "items"');
        return;
      }
      
      // Obtener la primera fila para clonar
      const primeraFila = tbody.querySelector('tr');
      if (!primeraFila) {
        console.error('No se encontró la primera fila para clonar');
        return;
      }
      
      // Clonar la primera fila
      const nuevaFila = primeraFila.cloneNode(true);
      
      // Limpiar valores de la nueva fila
      const select = nuevaFila.querySelector('select[name="productoId"]');
      const inputCantidad = nuevaFila.querySelector('input[name="cantidad"]');
      const inputPrecio = nuevaFila.querySelector('input[name="costoUnitario"]');
      
      // Resetear valores
      if (select) {
        select.selectedIndex = 0;
        select.className = 'clonado';
      }
      
      if (inputCantidad) {
        inputCantidad.value = '1';
      }
      
      if (inputPrecio) {
        inputPrecio.value = '0.00';
      }
      
      // Configurar el botón de eliminar
      const btnEliminar = nuevaFila.querySelector('button');
      if (btnEliminar) {
        btnEliminar.onclick = function() {
          nuevaFila.remove();
          actualizarOpcionesDisponibles();
        };
      }
      
      // Agregar la nueva fila a la tabla
      tbody.appendChild(nuevaFila);
      
      // Actualizar opciones disponibles
      actualizarOpcionesDisponibles();
      
      console.log('Nueva fila agregada correctamente');
    }
    
    // Función para actualizar el precio basado en el producto seleccionado
    function actualizarPrecio(select) {
      const tr = select.closest('tr');
      const precioInput = tr.querySelector('input[name="costoUnitario"]');
      const stockInfo = tr.querySelector('.stock-info');
      
      if (select.value) {
        // Aquí podrías hacer una llamada AJAX para obtener el precio actual del producto
        // Por ahora, dejamos que el usuario lo ingrese manualmente
        if (precioInput) {
          precioInput.focus();
        }
        
        // Mostrar información de stock si está disponible
        if (stockInfo && select.selectedOptions[0].dataset.stock !== undefined) {
          stockInfo.textContent = `Stock actual: ${select.selectedOptions[0].dataset.stock}`;
        }
      } else {
        if (stockInfo) stockInfo.textContent = '';
      }
    }
    
    // Función para validar la cantidad contra el stock disponible
    function validarCantidad(input) {
      const tr = input.closest('tr');
      const select = tr.querySelector('select[name="productoId"]');
      const errorDiv = tr.querySelector('.error-message');
      
      if (!select.value) return;
      
      const stockDisponible = parseFloat(select.selectedOptions[0].dataset.stock || '0');
      const cantidad = parseFloat(input.value || '0');
      
      if (isNaN(cantidad) || cantidad <= 0) {
        showError(errorDiv, 'La cantidad debe ser mayor a cero');
        return;
      }
      
      if (stockDisponible > 0 && cantidad > stockDisponible) {
        showError(errorDiv, `La cantidad no puede ser mayor al stock disponible (${stockDisponible})`);
      } else {
        hideError(errorDiv);
      }
    }
    
    function showError(element, message) {
      if (!element) return;
      element.textContent = message;
      element.style.display = 'block';
      element.closest('tr').classList.add('error');
    }
    
    function hideError(element) {
      if (!element) return;
      element.textContent = '';
      element.style.display = 'none';
      element.closest('tr').classList.remove('error');
    }
  </script>
</head>
<body>
  <div class="container">
  <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 1rem;">
    <h2>Nueva compra</h2>
    <div>
      <a href="${pageContext.request.contextPath}/index.jsp" class="button btn btn-secondary">Inicio</a>
      <a href="${pageContext.request.contextPath}/logout" class="button btn btn-primary">Cerrar sesión</a>
    </div>
  </div>

  <c:if test="${empty proveedores}">
    <div class="alert alert-error">No hay proveedores registrados. Por favor, cree al menos un proveedor antes de registrar compras.</div>
  </c:if>
  <c:if test="${empty productos}">
    <div class="alert alert-error">
      No hay productos activos. Active/cree productos para poder agregarlos a la compra.
      <div style="margin-top:.4rem; display:flex; gap:.5rem; flex-wrap:wrap">
        <a class="button btn btn-secondary" target="_blank" href="${pageContext.request.contextPath}/admin/productos/nuevo">Crear producto</a>
        <a class="button btn btn-secondary" target="_blank" href="${pageContext.request.contextPath}/admin/productos">Administrar productos</a>
      </div>
    </div>
  </c:if>

  <form method="post" action="${pageContext.request.contextPath}/bodega/compras/guardar">
    <div class="row">
      <div>
        <label>Proveedor</label>
        <select name="proveedorId" required>
          <option value="" disabled selected>Seleccione...</option>
          <c:forEach items="${proveedores}" var="p">
            <option value="${p.id}">${p.nombre}</option>
          </c:forEach>
        </select>
        <div style="margin-top:.4rem; display:flex; gap:.5rem; flex-wrap:wrap">
          <a class="button btn btn-secondary" target="_blank" href="${pageContext.request.contextPath}/admin/proveedores/nuevo">Crear proveedor</a>
          <a class="button btn btn-secondary" target="_blank" href="${pageContext.request.contextPath}/admin/proveedores">Administrar proveedores</a>
        </div>
      </div>
      <div>
        <label>Número de factura</label>
        <input type="text" name="numeroFactura" />
      </div>
    </div>

    <label>Observaciones</label>
    <input type="text" name="observaciones" />

    <div class="top" style="margin-top:1rem">
      <h3 style="margin:0">Items</h3>
      <div class="search">
        <a class="button btn btn-secondary" target="_blank" href="${pageContext.request.contextPath}/admin/productos/nuevo">Crear producto</a>
        <a class="button btn btn-secondary" target="_blank" href="${pageContext.request.contextPath}/admin/productos">Administrar productos</a>
        <span class="badge">Productos disponibles: <c:out value='${fn:length(productos)}'/></span>
      </div>
    </div>
    <table>
      <thead>
        <tr>
          <th>Producto</th>
          <th>Cantidad</th>
          <th>Costo unitario</th>
          <th></th>
        </tr>
      </thead>
      <tbody id="items">
        <c:if test="${not empty productos}">
          <tr>
            <td>
              <select name="productoId" required>
                <option value="" disabled selected>Seleccione...</option>
                <c:forEach items="${productos}" var="pr">
                  <option value="${pr.id}" data-stock="${pr.stockActual}">${pr.nombre} (${pr.sku}) - Stock: ${pr.stockActual}</option>
                </c:forEach>
              </select>
            </td>
            <td><input type="number" step="0.001" min="0.001" name="cantidad" required value="1" /></td>
            <td><input type="number" step="0.01" min="0" name="costoUnitario" required value="0.00" /></td>
            <td><button type="button" onclick="this.closest('tr').remove()">Quitar</button></td>
          </tr>
        </c:if>
      </tbody>
    </table>
    <button class="button" type="button" onclick="addRow()" <c:if test='${empty productos}'>disabled</c:if>>Agregar item</button>

    <div class="actions">
      <button class="button" type="submit" <c:if test='${empty proveedores || empty productos}'>disabled</c:if>>Guardar</button>
      <a class="button" href="${pageContext.request.contextPath}/bodega/compras">Cancelar</a>
    </div>
  </form>

  <!-- Flag para JS sin usar EL directo en el script -->
  <!-- Usamos un atributo data para pasar el valor de JSP a JavaScript -->
  <div id="metaFlags" data-has-productos="${not empty productos}"></div>
  <script>
    // Al cargar, si hay productos disponibles, agrega una fila por defecto
    document.addEventListener('DOMContentLoaded', function(){
      // Obtenemos el valor del atributo data
      var metaFlags = document.getElementById('metaFlags');
      var hasProductos = metaFlags ? metaFlags.dataset.hasProductos === 'true' : false;
      var tbody = document.getElementById('items');
      if (hasProductos && tbody && tbody.children.length === 0) { 
        addRow(); 
      }
      
      // Agregar validación al enviar el formulario
      const form = document.querySelector('form');
      if (form) {
        form.addEventListener('submit', function(e) {
          const selects = document.querySelectorAll('select[name="productoId"]');
          const selectedProducts = new Set();
          let isValid = true;
          
          // Verificar productos duplicados
          selects.forEach(select => {
            if (select.value) {
              if (selectedProducts.has(select.value)) {
                isValid = false;
                showError(
                  select.closest('tr').querySelector('.error-message'),
                  'Este producto ya está en la lista'
                );
              } else {
                selectedProducts.add(select.value);
              }
            }
          });
          
          if (!isValid) {
            e.preventDefault();
            alert('Por favor, corrija los errores en el formulario antes de continuar.');
          }
        });
      }
    });
  </script>
  </div>
</body>
</html>
