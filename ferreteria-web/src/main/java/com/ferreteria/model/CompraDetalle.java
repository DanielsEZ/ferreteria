package com.ferreteria.model;

public class CompraDetalle {
    private int id;
    private int compraId;
    private int productoId;
    private String productoNombre;
    private double cantidad;
    private double costoUnitario;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCompraId() { return compraId; }
    public void setCompraId(int compraId) { this.compraId = compraId; }
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(double costoUnitario) { this.costoUnitario = costoUnitario; }
}
