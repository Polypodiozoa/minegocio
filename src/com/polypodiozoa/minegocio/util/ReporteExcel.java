package com.polypodiozoa.minegocio.util;

import android.content.Context;
import android.net.Uri;

import com.polypodiozoa.minegocio.datos.BD;
import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Excel.Celda;
import com.polypodiozoa.minegocio.util.Excel.Hoja;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reportes de la app en Excel: resumen, ventas, productos, deudas y gastos. */
public final class ReporteExcel {

    private ReporteExcel() {}

    public static void generar(Context ctx, Uri destino, long desde, long hasta,
                               String periodo) throws IOException {
        BD bd = BD.get(ctx);
        Modelo.Resumen r = bd.resumen(desde, hasta);

        Excel excel = new Excel();
        excel.agregar(hojaResumen(bd, r, periodo, desde, hasta));
        excel.agregar(hojaVentas(bd, desde, hasta));
        excel.agregar(hojaProductos(bd, desde, hasta));
        excel.agregar(hojaDeudas(bd));
        excel.agregar(hojaGastos(bd, desde, hasta));
        escribir(ctx, destino, excel);
    }

    public static void generarDeudas(Context ctx, Uri destino) throws IOException {
        BD bd = BD.get(ctx);
        Excel excel = new Excel();
        excel.agregar(hojaDeudas(bd));
        escribir(ctx, destino, excel);
    }

    private static void escribir(Context ctx, Uri destino, Excel excel) throws IOException {
        OutputStream salida = ctx.getContentResolver().openOutputStream(destino, "w");
        if (salida == null) throw new IOException("No se pudo abrir el destino.");
        excel.escribir(salida);
    }

    private static Hoja hojaResumen(BD bd, Modelo.Resumen r, String periodo,
                                    long desde, long hasta) {
        Hoja h = new Hoja("Resumen").ancho(38, 20, 18, 16, 16);
        h.fila(Celda.texto(bd.nombreNegocio(), Excel.TITULO));
        h.fila("Reporte de ventas");
        h.fila("Período", periodo);
        h.fila("Desde", Fechas.fecha(desde));
        h.fila("Hasta", Fechas.fecha(hasta));
        h.fila("Generado", Fechas.fechaHora(Fechas.ahora()));
        h.filaVacia();
        h.titulo("Indicador", "Valor");
        h.fila("Ventas realizadas", r.ventas);
        h.fila("Ingreso", Celda.moneda(r.ventasTotal));
        h.fila("Costo de lo vendido", Celda.moneda(r.costoVentas));
        h.fila("Utilidad bruta", Celda.moneda(r.utilidadBruta));
        long base = Math.max(0, r.ventasTotal - r.impuestoVentas);
        h.fila("Margen bruto", Celda.porcentaje(base > 0 ? r.utilidadBruta * 1.0 / base : 0));
        h.fila("Impuestos cobrados", Celda.moneda(r.impuestoVentas));
        h.fila("Compras", Celda.moneda(r.comprasTotal));
        h.fila("Impuestos pagados en compras", Celda.moneda(r.impuestoCompras));
        h.fila("Gastos", Celda.moneda(r.gastosTotal));
        h.fila("Utilidad neta", Celda.moneda(r.utilidadNeta));
        h.filaVacia();
        h.titulo("Cartera actual (todas las fechas)");
        h.fila("Por cobrar", Celda.moneda(r.porCobrar));
        h.fila("Deuda vencida", Celda.moneda(r.deudaVencida));
        h.fila("Por vencer en 7 días", Celda.moneda(r.deudaPorVencer));
        h.fila("Deudas abiertas", r.deudasAbiertas);
        h.fila("Deudas vencidas", r.deudasVencidas);

        h.filaVacia();
        h.titulo("Formas de pago", "Ventas", "Total");
        for (Modelo.TotalFormaPago f : bd.formasPago(desde, hasta)) {
            h.fila(f.formaPago, f.cantidad, Celda.moneda(f.total));
        }

        h.filaVacia();
        h.titulo("Productos más vendidos", "Cantidad", "Ingresos", "Costo", "Utilidad");
        for (Modelo.TopProducto t : bd.topProductos(desde, hasta, 10)) {
            h.fila(t.descripcion, t.cantidad, Celda.moneda(t.total),
                    Celda.moneda(t.costo), Celda.moneda(t.utilidad));
        }
        return h;
    }

    private static Hoja hojaVentas(BD bd, long desde, long hasta) {
        Hoja h = new Hoja("Ventas").ancho(8, 18, 26, 18, 11, 14, 14, 12, 13, 14, 14, 14, 10, 14, 14, 46);
        h.titulo("Venta", "Fecha", "Cliente", "Pago", "Estado", "Vence",
                "Subtotal", "Descuento", "Impuesto", "Total", "Costo", "Ganancia",
                "Margen", "Abonado", "Saldo", "Productos");
        List<Modelo.Venta> ventas = bd.ventas("");
        for (Modelo.Venta v : ventas) {
            if (v.fecha < desde || v.fecha >= hasta) continue;
            StringBuilder productos = new StringBuilder();
            for (Modelo.Item i : bd.itemsVenta(v.id)) {
                if (productos.length() > 0) productos.append(" | ");
                productos.append(i.cantidad).append(" x ").append(i.descripcion);
            }
            long ganancia = v.total - v.impuesto - v.costo;
            long base = Math.max(0, v.subtotal - v.descuento);
            double margen = base > 0 ? ganancia * 1.0 / base : 0;
            Modelo.Deuda deuda = "credito".equals(v.estado) ? bd.deudaVenta(v.id) : null;
            h.fila(v.id,
                    Fechas.formato("dd/MM/yyyy HH:mm", v.fecha),
                    v.clienteNombre.isEmpty() ? "Consumidor final" : v.clienteNombre,
                    v.formaPago,
                    v.anulada ? "Anulada" : "credito".equals(v.estado) ? "Crédito" : "Pagada",
                    v.fechaLimite > 0 ? Fechas.fecha(v.fechaLimite) : "",
                    Celda.moneda(v.subtotal), Celda.moneda(v.descuento),
                    Celda.moneda(v.impuesto), Celda.moneda(v.total), Celda.moneda(v.costo),
                    Celda.moneda(ganancia), Celda.porcentaje(margen),
                    Celda.moneda(deuda == null ? 0 : deuda.abonado),
                    Celda.moneda(deuda == null ? 0 : deuda.saldo),
                    productos.toString());
        }
        return h;
    }

    private static Hoja hojaProductos(BD bd, long desde, long hasta) {
        Hoja h = new Hoja("Productos").ancho(30, 18, 11, 10, 12, 16, 14, 16, 12, 13, 15, 14, 15);
        h.titulo("Producto", "Categoría", "Existencia", "Mínimo", "Estado",
                "Costo unitario", "Precio", "Ganancia unitaria", "Margen",
                "Vendidos período", "Ingresos período", "Costo período", "Utilidad período");
        Map<Long, Modelo.ProductoVendido> vendidos = new HashMap<>();
        for (Modelo.ProductoVendido p : bd.productosVendidos(desde, hasta)) {
            vendidos.put(p.productoId, p);
        }
        for (Modelo.Producto p : bd.productos("", false)) {
            Modelo.ProductoVendido v = vendidos.get(p.id);
            long gananciaUnidad = p.precio - p.costo;
            double margen = p.precio > 0 ? gananciaUnidad * 1.0 / p.precio : 0;
            String estado = p.stock <= 0 ? "Agotado" : p.stock <= p.stockMinimo ? "Bajo" : "Disponible";
            h.fila(p.nombre, p.categoria, p.stock, p.stockMinimo, estado,
                    Celda.moneda(p.costo), Celda.moneda(p.precio),
                    Celda.moneda(gananciaUnidad), Celda.porcentaje(margen),
                    v == null ? 0 : v.cantidad,
                    Celda.moneda(v == null ? 0 : v.total),
                    Celda.moneda(v == null ? 0 : v.costo),
                    Celda.moneda(v == null ? 0 : v.total - v.costo));
        }
        return h;
    }

    private static Hoja hojaDeudas(BD bd) {
        Hoja h = new Hoja("Deudas").ancho(8, 28, 16, 14, 14, 10, 14, 14, 14, 16, 12);
        h.titulo("Venta", "Cliente", "Teléfono", "Fecha venta", "Vence", "Días",
                "Total", "Abonado", "Saldo", "Estado", "Forma de pago");
        long total = 0;
        long abonado = 0;
        long saldo = 0;
        for (Modelo.Deuda d : bd.deudas()) {
            total += d.total;
            abonado += d.abonado;
            saldo += d.saldo;
            int dias = Fechas.diasHasta(d.fechaLimite);
            String estado = d.fechaLimite <= 0 ? "Sin fecha"
                    : dias < 0 ? "Vencida" : dias <= 7 ? "Por vencer" : "Vigente";
            h.fila(d.ventaId, d.clienteNombre, d.clienteTelefono,
                    Fechas.fecha(d.fecha),
                    d.fechaLimite > 0 ? Fechas.fecha(d.fechaLimite) : "",
                    d.fechaLimite > 0 ? dias : "",
                    Celda.moneda(d.total), Celda.moneda(d.abonado), Celda.moneda(d.saldo),
                    estado, d.formaPago);
        }
        h.filaVacia();
        h.fila(Celda.texto("Totales", Excel.TITULO), Celda.texto("", Excel.TITULO),
                Celda.texto("", Excel.TITULO), Celda.texto("", Excel.TITULO),
                Celda.texto("", Excel.TITULO), Celda.texto("", Excel.TITULO),
                Celda.moneda(total), Celda.moneda(abonado), Celda.moneda(saldo),
                Celda.texto("", Excel.TITULO), Celda.texto("", Excel.TITULO));
        return h;
    }

    private static Hoja hojaGastos(BD bd, long desde, long hasta) {
        Hoja h = new Hoja("Gastos").ancho(18, 20, 34, 16, 18, 36);
        h.titulo("Fecha", "Categoría", "Descripción", "Monto", "Pago", "Notas");
        long total = 0;
        for (Modelo.Gasto g : bd.gastos("")) {
            if (g.fecha < desde || g.fecha >= hasta) continue;
            total += g.monto;
            h.fila(Fechas.fecha(g.fecha), g.categoria, g.descripcion,
                    Celda.moneda(g.monto), g.formaPago, g.notas);
        }
        h.filaVacia();
        h.fila(Celda.texto("Total gastos", Excel.TITULO), Celda.texto("", Excel.TITULO),
                Celda.texto("", Excel.TITULO), Celda.moneda(total),
                Celda.texto("", Excel.TITULO), Celda.texto("", Excel.TITULO));
        return h;
    }
}
