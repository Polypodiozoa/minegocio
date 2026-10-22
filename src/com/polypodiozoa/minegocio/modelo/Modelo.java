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
        public boolean activo = true;
        public long creado;
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
    }

    public static class TotalFormaPago {
        public String formaPago = "";
        public int cantidad;
        public long total;
    }
}
