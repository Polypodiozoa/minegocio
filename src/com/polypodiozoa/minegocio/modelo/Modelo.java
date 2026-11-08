package com.polypodiozoa.minegocio.modelo;

/** Modelos simples de datos. Todos los valores monetarios están en pesos colombianos enteros. */
public final class Modelo {

    private Modelo() {}

    public static class Producto {
        public long id;
        public String nombre = "";
        public String categoria = "";
        public String codigo = "";
        public String unidad = "unidad";
        public String foto = "";
        public long precio;
        public long costo;
        public int stock;
        public int stockMinimo;
        /** Tarifa propia de IVA. -1 = usar la tarifa general de la venta. */
        public double impuestoPct = -1;
        /** ¿Se controla vencimiento? Las categorías de comida y droguería lo activan. */
        public boolean vence;
        /** Días de aviso antes del vencimiento. -1 = usar el valor de Ajustes (30). */
        public int diasAviso = -1;
        public boolean activo = true;
        public long creado;

        /** ¿Este producto tiene tarifa propia, distinta de la general? */
        public boolean tieneImpuestoPropio() {
            return impuestoPct >= 0;
        }
    }

    public static class Cliente {
        public long id;
        public String nombre = "";
        public String telefono = "";
        public String correo = "";
        public String direccion = "";
        public String notas = "";
        public String foto = "";
        public long creado;
        public long saldo;
        public long cupo;
    }

    public static class Proveedor {
        public long id;
        public String nombre = "";
        public String contacto = "";
        public String telefono = "";
        public String correo = "";
        public String direccion = "";
        public String notas = "";
        public String foto = "";
        public long creado;
    }

    public static class Usuario {
        public long id;
        public String nombre = "";
        public String rol = "empleado";
        public String menu = "";
        /** Módulos permitidos, separados por coma. Vacío = todos. */
        public String permisos = "";
        public boolean activo = true;
        public long creado;

        public boolean esAdministrador() {
            return "admin".equalsIgnoreCase(rol);
        }

        public String rolVisible() {
            return esAdministrador() ? "Administrador" : "Empleado";
        }
    }

    public static class Venta {
        public long id;
        public long fecha;
        public long clienteId;
        public long subtotal;
        public long descuento;
        public double impuestoPct;
        public long impuesto;
        public long total;
        public long costo;
        public long fechaLimite;
        public long abonado;
        public long saldo;
        public String formaPago = "Efectivo";
        public String estado = "pagada";
        public String notas = "";
        public String clienteNombre = "";
        public long usuarioId;
        public String usuarioNombre = "";
        public boolean anulada;
        /** false = queda por entregar; el stock se descuenta según Ajustes. */
        public boolean entregada = true;
        /** true = se registró con una fecha anterior a hoy. */
        public boolean retroactiva;
        public int numItems;
    }

    public static class Compra {
        public long id;
        public long fecha;
        public long proveedorId;
        public long subtotal;
        public double impuestoPct;
        public long impuesto;
        public long total;
        public String formaPago = "Efectivo";
        public String notas = "";
        public String proveedorNombre = "";
        public long usuarioId;
        public String usuarioNombre = "";
        public boolean anulada;
        /** false = mercancía pendiente de recibir. */
        public boolean recibida = true;
        public long fechaEntrega;
        public int numItems;
    }

    public static class Item {
        public long id;
        public long ventaId;
        public long compraId;
        public long productoId;
        public String descripcion = "";
        public int cantidad = 1;
        public long precioUnitario;
        public long costoUnitario;
        public long subtotal;
        /** Costo real de la línea (suma de los lotes consumidos). */
        public long costoTotal;
        /** Tarifa aplicada a la línea. -1 = tarifa general de la venta. */
        public double impuestoPct = -1;
        /** Impuesto de la línea, ya calculado. */
        public long impuesto;
        /** Vencimiento pedido para esta línea de compra (0 = no vence). */
        public long vencimiento;
        /** Unidades que no alcanzaron a cubrirse con lotes (venta sin existencia). */
        public int faltante;
    }

    public static class Lote {
        public long id;
        public long productoId;
        public long compraId;
        public long costoUnitario;
        public int cantidadInicial;
        public int restante;
        public long vencimiento;
        public boolean recibido = true;
        public long creado;
    }

    public static class PrecioCompra {
        public long compraId;
        public long fecha;
        public int cantidad;
        public long costoUnitario;
        public String proveedor = "";
        /** Variación porcentual frente a la compra anterior del mismo producto. */
        public int variacion;
        public long vencimiento;
    }

    public static class Adjunto {
        public long id;
        public String entidad = "";
        public long entidadId;
        public String ruta = "";
        public String nombre = "";
        public String mime = "";
        public long creado;
    }

    public static class Pago {
        public long id;
        public long clienteId;
        public long fecha;
        public long monto;
        public String formaPago = "Efectivo";
        public String notas = "";
        public String clienteNombre = "";
        public long usuarioId;
        public String usuarioNombre = "";
        public long ventaId;
    }

    public static class Gasto {
        public long id;
        public long fecha;
        public String categoria = "";
        public String descripcion = "";
        public long monto;
        public String formaPago = "Efectivo";
        public String notas = "";
        public long usuarioId;
        public String usuarioNombre = "";
    }

    public static class Deuda {
        public long ventaId;
        public long clienteId;
        public long fecha;
        public long fechaLimite;
        public long total;
        public long abonado;
        public long saldo;
        public String clienteNombre = "";
        public String clienteTelefono = "";
        public String formaPago = "";
        public String notas = "";
    }

    public static class Caja {
        public long id;
        public long usuarioId;
        public String usuarioNombre = "";
        public long apertura;
        public long cierre;
        public long montoInicial;
        public long montoContado;
        public long diferencia;
        public String notas = "";
        public String estado = "abierta";
    }

    public static class ResumenCaja {
        public int ventasCantidad;
        public long ventasTotal;
        public long ventasEfectivo;
        public long pagosTotal;
        public long pagosEfectivo;
        public long comprasTotal;
        public long comprasEfectivo;
        public long gastosTotal;
        public long gastosEfectivo;
        public long efectivoEsperado;
    }

    public static class Resumen {
        public int ventas;
        public long ventasTotal;
        public long ventasSubtotal;
        public long descuentos;
        public long impuestoVentas;
        public long costoVentas;
        public long comprasTotal;
        public long impuestoCompras;
        public long gastosTotal;
        public long utilidadBruta;
        public long utilidadNeta;
        public long porCobrar;
        public long deudaVencida;
        public long deudaPorVencer;
        public int deudasAbiertas;
        public int deudasVencidas;
        public long ventasCredito;
        public int productosAgotados;
        public int productosActivos;
    }

    public static class TopProducto {
        public String descripcion = "";
        public long cantidad;
        public long total;
        public long costo;
        public long utilidad;
    }

    public static class ProductoVendido {
        public long productoId;
        public long cantidad;
        public long total;
        public long costo;
        /** Número de ventas distintas en las que apareció. */
        public long transacciones;
    }

    public static class TotalFormaPago {
        public String formaPago = "";
        public int cantidad;
        public long total;
    }

    public static class Cotizacion {
        public long id;
        public String numero = "";
        public long fecha;
        public long clienteId;
        public String clienteNombre = "";
        public long subtotal;
        public long descuento;
        public double impuestoPct;
        public long impuesto;
        public long total;
        public long validaHasta;
        public String notas = "";
        public String estado = "abierta";
        public long usuarioId;
        public String usuarioNombre = "";
        public int numItems;
    }

    public static class Agenda {
        public long id;
        /** cliente | proveedor */
        public String tipo = "cliente";
        public long entidadId;
        public String nombre = "";
        public long fecha;
        public String titulo = "";
        public String notas = "";
        public int avisoDias;
        /** "", semanal, mensual, anual */
        public String recurrente = "";
        public boolean hecho;
    }

    public static class Kpi {
        public long ventasHoy;
        public long ventasPromedio7;
        public long ticketPromedio;
        public int margenBruto;
        public long utilidadNetaMes;
        public int diasInventario;
        public int sinMovimiento30;
        public long cartera;
        public int carteraVencidaPct;
        public int diasCobro;
        public int fiadoSobreVentas;
        public int quiebresPct;
        public int crecimientoMes;
    }

    public static class PuntoSerie {
        public long desde;
        public String etiqueta = "";
        public long valor;
        public long valor2;
    }
}