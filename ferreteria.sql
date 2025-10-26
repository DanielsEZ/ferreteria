-- Crear base
CREATE DATABASE IF NOT EXISTS ferreteria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ferreteria;

-- Roles
CREATE TABLE roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Usuarios
CREATE TABLE usuarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nombre_completo VARCHAR(120) NOT NULL,
  rol_id INT NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- Clientes
CREATE TABLE clientes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  documento VARCHAR(30) NULL,
  telefono VARCHAR(30) NULL,
  direccion VARCHAR(200) NULL,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Proveedores
CREATE TABLE proveedores (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  nit VARCHAR(30) NULL,
  telefono VARCHAR(30) NULL,
  direccion VARCHAR(200) NULL,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categorías
CREATE TABLE categorias (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL UNIQUE
);

-- Productos
CREATE TABLE productos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(50) NOT NULL UNIQUE,
  nombre VARCHAR(150) NOT NULL,
  categoria_id INT NOT NULL,
  precio_compra DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  precio_venta DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);
CREATE INDEX idx_prod_nombre ON productos (nombre);

-- Métodos de pago
CREATE TABLE metodos_pago (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Compras (Bodega)
CREATE TABLE compras (
  id INT AUTO_INCREMENT PRIMARY KEY,
  proveedor_id INT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  numero_factura VARCHAR(50) NULL,
  total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  observaciones VARCHAR(255) NULL,
  usuario_id INT NULL, -- quién registró la compra
  FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE compras_detalle (
  id INT AUTO_INCREMENT PRIMARY KEY,
  compra_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad DECIMAL(14,3) NOT NULL,
  costo_unitario DECIMAL(12,2) NOT NULL,
  subtotal DECIMAL(14,2) AS (cantidad * costo_unitario) STORED,
  FOREIGN KEY (compra_id) REFERENCES compras(id),
  FOREIGN KEY (producto_id) REFERENCES productos(id)
);
CREATE INDEX idx_compra_det_prod ON compras_detalle (producto_id);

-- Ventas (Pedidos/Facturas)
-- estado: 'PENDIENTE' (pedido), 'FACTURADA', 'PAGADA', 'ANULADA'
CREATE TABLE ventas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cliente_id INT NULL,
  vendedor_id INT NULL, -- usuario de ventas
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado ENUM('PENDIENTE','FACTURADA','PAGADA','ANULADA') NOT NULL DEFAULT 'PENDIENTE',
  total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  observaciones VARCHAR(255) NULL,
  FOREIGN KEY (cliente_id) REFERENCES clientes(id),
  FOREIGN KEY (vendedor_id) REFERENCES usuarios(id)
);

CREATE TABLE ventas_detalle (
  id INT AUTO_INCREMENT PRIMARY KEY,
  venta_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad DECIMAL(14,3) NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  descuento DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  subtotal DECIMAL(14,2) AS (GREATEST(cantidad * precio_unitario - descuento, 0)) STORED,
  FOREIGN KEY (venta_id) REFERENCES ventas(id),
  FOREIGN KEY (producto_id) REFERENCES productos(id)
);
CREATE INDEX idx_venta_det_prod ON ventas_detalle (producto_id);

-- Pagos
CREATE TABLE pagos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  venta_id INT NOT NULL,
  metodo_pago_id INT NOT NULL,
  monto DECIMAL(14,2) NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  referencia VARCHAR(100) NULL, -- voucher, cheque, etc.
  FOREIGN KEY (venta_id) REFERENCES ventas(id),
  FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago(id)
);

-- Movimientos de inventario (Kardex)
-- tipo: IN (entrada), OUT (salida)
-- referencia_tipo: COMPRA, VENTA, AJUSTE
CREATE TABLE inventario_mov (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  producto_id INT NOT NULL,
  tipo ENUM('IN','OUT') NOT NULL,
  referencia_tipo ENUM('COMPRA','VENTA','AJUSTE') NOT NULL,
  referencia_id INT NOT NULL,
  cantidad DECIMAL(14,3) NOT NULL,
  valor_unitario DECIMAL(12,2) NULL, -- costo o precio de referencia
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_id INT NULL,
  FOREIGN KEY (producto_id) REFERENCES productos(id),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
CREATE INDEX idx_inv_mov_prod ON inventario_mov (producto_id);

-- Vista de existencias actuales
CREATE OR REPLACE VIEW vw_existencias AS
SELECT
  p.id AS producto_id,
  p.sku,
  p.nombre,
  p.categoria_id,
  COALESCE(SUM(CASE WHEN im.tipo='IN' THEN im.cantidad ELSE 0 END),0) AS entradas,
  COALESCE(SUM(CASE WHEN im.tipo='OUT' THEN im.cantidad ELSE 0 END),0) AS salidas,
  COALESCE(SUM(CASE WHEN im.tipo='IN' THEN im.cantidad ELSE -im.cantidad END),0) AS stock_actual
FROM productos p
LEFT JOIN inventario_mov im ON im.producto_id = p.id
GROUP BY p.id, p.sku, p.nombre, p.categoria_id;

-- Datos semilla
INSERT IGNORE INTO roles (nombre) VALUES ('ADMIN'), ('BODEGA'), ('VENTAS'), ('CAJA');

INSERT IGNORE INTO metodos_pago (nombre) VALUES ('EFECTIVO'), ('TARJETA'), ('CHEQUE'), ('TRANSFERENCIA');

INSERT IGNORE INTO categorias (nombre) VALUES ('Herramientas'), ('Electricidad'), ('Plomería');

-- Usuario admin por defecto (password_hash: configura más tarde)
INSERT IGNORE INTO usuarios (username, password_hash, nombre_completo, rol_id, activo)
SELECT 'admin', '$2a$10$hash_de_ejemplo_reemplazar', 'Administrador', r.id, 1
FROM roles r WHERE r.nombre='ADMIN' LIMIT 1;