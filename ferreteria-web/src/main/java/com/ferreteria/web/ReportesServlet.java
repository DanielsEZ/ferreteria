package com.ferreteria.web;

import com.ferreteria.dao.CompraDao;
import com.ferreteria.dao.ProveedorDao;
import com.ferreteria.dao.VentaDao;
import com.ferreteria.model.Compra;
import com.ferreteria.model.Venta;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ReportesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (path.equals("/compras")) {
            comprasHtml(req, resp);
            return;
        }
        if (path.equals("/compras/csv")) {
            comprasCsv(req, resp);
            return;
        }
        if (path.equals("/ventas")) {
            ventasHtml(req, resp);
            return;
        }
        if (path.equals("/ventas/csv")) {
            ventasCsv(req, resp);
            return;
        }
        if (path.equals("/ventas/pdf")) {
            ventasPdf(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private static class Rango {
        Timestamp desde;
        Timestamp hasta;
        String desdeStr;
        String hastaStr;
    }

    private Rango resolverRango(HttpServletRequest req) {
        String periodo = req.getParameter("periodo");
        LocalDate hoy = LocalDate.now();
        LocalDateTime desdeDT;
        LocalDateTime hastaDT;
        if ("anio".equals(periodo)) {
            desdeDT = LocalDate.of(hoy.getYear(), 1, 1).atStartOfDay();
            hastaDT = LocalDate.of(hoy.getYear(), 12, 31).atTime(LocalTime.MAX);
        } else if ("mes".equals(periodo)) {
            LocalDate first = hoy.withDayOfMonth(1);
            LocalDate last = first.plusMonths(1).minusDays(1);
            desdeDT = first.atStartOfDay();
            hastaDT = last.atTime(LocalTime.MAX);
        } else if ("custom".equals(periodo)) {
            String desde = req.getParameter("desde");
            String hasta = req.getParameter("hasta");
            LocalDate d = (desde != null && !desde.isBlank()) ? LocalDate.parse(desde) : hoy;
            LocalDate h = (hasta != null && !hasta.isBlank()) ? LocalDate.parse(hasta) : hoy;
            desdeDT = d.atStartOfDay();
            hastaDT = h.atTime(LocalTime.MAX);
        } else { // hoy
            desdeDT = hoy.atStartOfDay();
            hastaDT = hoy.atTime(LocalTime.MAX);
        }
        Rango r = new Rango();
        r.desde = Timestamp.valueOf(desdeDT);
        r.hasta = Timestamp.valueOf(hastaDT);
        r.desdeStr = desdeDT.toLocalDate().toString();
        r.hastaStr = hastaDT.toLocalDate().toString();
        return r;
    }

    private void comprasHtml(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Rango r = resolverRango(req);
            String proveedorIdStr = req.getParameter("proveedorId");
            Integer proveedorId = (proveedorIdStr != null && !proveedorIdStr.isBlank()) ? Integer.parseInt(proveedorIdStr) : null;

            List<Compra> compras = new CompraDao().listarPorRango(r.desde, r.hasta, proveedorId);
            req.setAttribute("compras", compras);
            req.setAttribute("proveedores", new ProveedorDao().listar());
            req.setAttribute("desde", r.desdeStr);
            req.setAttribute("hasta", r.hastaStr);
            req.getRequestDispatcher("/WEB-INF/jsp/reportes_compras.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void comprasCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            Rango r = resolverRango(req);
            String proveedorIdStr = req.getParameter("proveedorId");
            Integer proveedorId = (proveedorIdStr != null && !proveedorIdStr.isBlank()) ? Integer.parseInt(proveedorIdStr) : null;
            List<Compra> compras = new CompraDao().listarPorRango(r.desde, r.hasta, proveedorId);

            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=compras.csv");
            try (PrintWriter w = resp.getWriter()) {
                w.println("Fecha,Proveedor,NumeroFactura,Total");
                for (Compra c : compras) {
                    String fecha = String.valueOf(c.getFecha());
                    String prov = c.getProveedorNombre().replace(",", " ");
                    String factura = c.getNumeroFactura() == null ? "" : c.getNumeroFactura().replace(",", " ");
                    String total = String.valueOf(c.getTotal());
                    w.printf("%s,%s,%s,%s%n", fecha, prov, factura, total);
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void ventasHtml(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Rango r = resolverRango(req);
            List<Venta> ventas = new VentaDao().listarPorRango(r.desde, r.hasta);
            req.setAttribute("ventas", ventas);
            req.setAttribute("desde", r.desdeStr);
            req.setAttribute("hasta", r.hastaStr);
            req.getRequestDispatcher("/WEB-INF/jsp/reportes_ventas.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void ventasCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            Rango r = resolverRango(req);
            List<Venta> ventas = new VentaDao().listarPorRango(r.desde, r.hasta);
            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=ventas.csv");
            try (PrintWriter w = resp.getWriter()) {
                w.println("Fecha,Cliente,Estado,Total");
                for (Venta v : ventas) {
                    String fecha = String.valueOf(v.getFecha());
                    String cliente = (v.getClienteNombre() == null ? "" : v.getClienteNombre()).replace(",", " ");
                    String estado = v.getEstado();
                    String total = String.valueOf(v.getTotal());
                    w.printf("%s,%s,%s,%s%n", fecha, cliente, estado, total);
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void ventasPdf(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Rango r = resolverRango(req);
            List<Venta> ventas = new VentaDao().listarPorRango(r.desde, r.hasta);

            // Cargar y compilar el JRXML
            String jrxmlPath = req.getServletContext().getRealPath("/WEB-INF/jasper/reportes_ventas.jrxml");
            net.sf.jasperreports.engine.JasperReport report = net.sf.jasperreports.engine.JasperCompileManager.compileReport(jrxmlPath);

            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("DESDE", r.desdeStr);
            params.put("HASTA", r.hastaStr);

            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource ds = new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(ventas);
            net.sf.jasperreports.engine.JasperPrint jp = net.sf.jasperreports.engine.JasperFillManager.fillReport(report, params, ds);

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=ventas.pdf");
            net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream(jp, resp.getOutputStream());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
