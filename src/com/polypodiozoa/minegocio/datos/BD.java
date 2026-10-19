package com.polypodiozoa.minegocio.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Base de datos local SQLite. Todo queda en el dispositivo: la app no pide
 * permiso de internet ni envía información a ningún servidor.
 */
public class BD extends SQLiteOpenHelper {

    public static final String ARCHIVO = "minegocio.db";
    private static final int VERSION = 2;
    private static BD instancia;

    public static synchronized BD get(Context contexto) {
        if (instancia == null) {
            instancia = new BD(contexto.getApplicationContext());
        }
        return instancia;
    }

    public static synchronized void reiniciar() {
        if (instancia != null) {
            instancia.close();
            instancia = null;
        }
    }

    public static File archivo(Context contexto) {
        return contexto.getDatabasePath(ARCHIVO);
    }

    private BD(Context contexto) {
        super(contexto, ARCHIVO, null, VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ajustes (clave TEXT PRIMARY KEY, valor TEXT NOT NULL)");
        db.execSQL("CREATE TABLE productos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "categoria TEXT," +
                "codigo TEXT," +
                "unidad TEXT DEFAULT 'unidad'," +
                "precio INTEGER NOT NULL DEFAULT 0," +
                "costo INTEGER NOT NULL DEFAULT 0," +
                "stock INTEGER NOT NULL DEFAULT 0," +
                "stock_minimo INTEGER NOT NULL DEFAULT 0," +
                "foto TEXT," +
                "activo INTEGER NOT NULL DEFAULT 1," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE clientes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "telefono TEXT," +
                "correo TEXT," +
                "direccion TEXT," +
                "notas TEXT," +
                "foto TEXT," +
                "cupo INTEGER NOT NULL DEFAULT 0," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE proveedores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "contacto TEXT," +
                "telefono TEXT," +
                "correo TEXT," +
                "direccion TEXT," +
                "notas TEXT," +
                "foto TEXT," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE ventas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fecha INTEGER NOT NULL," +
                "cliente_id INTEGER REFERENCES clientes(id)," +
                "subtotal INTEGER NOT NULL DEFAULT 0," +
                "descuento INTEGER NOT NULL DEFAULT 0," +
                "impuesto_pct REAL NOT NULL DEFAULT 0," +
                "impuesto INTEGER NOT NULL DEFAULT 0," +
                "total INTEGER NOT NULL DEFAULT 0," +
                "costo INTEGER NOT NULL DEFAULT 0," +
                "fecha_limite INTEGER NOT NULL DEFAULT 0," +
                "forma_pago TEXT," +
                "estado TEXT NOT NULL DEFAULT 'pagada'," +
                "notas TEXT," +
                "anulada INTEGER NOT NULL DEFAULT 0," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE venta_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "venta_id INTEGER NOT NULL REFERENCES ventas(id) ON DELETE CASCADE," +
                "producto_id INTEGER REFERENCES productos(id)," +
                "descripcion TEXT NOT NULL," +
                "cantidad INTEGER NOT NULL DEFAULT 1," +
                "precio_unitario INTEGER NOT NULL DEFAULT 0," +
                "costo_unitario INTEGER NOT NULL DEFAULT 0," +
                "subtotal INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE compras (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fecha INTEGER NOT NULL," +
                "proveedor_id INTEGER REFERENCES proveedores(id)," +
                "subtotal INTEGER NOT NULL DEFAULT 0," +
                "impuesto_pct REAL NOT NULL DEFAULT 0," +
                "impuesto INTEGER NOT NULL DEFAULT 0," +
                "total INTEGER NOT NULL DEFAULT 0," +
                "forma_pago TEXT," +
                "notas TEXT," +
                "anulada INTEGER NOT NULL DEFAULT 0," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE compra_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "compra_id INTEGER NOT NULL REFERENCES compras(id) ON DELETE CASCADE," +
                "producto_id INTEGER REFERENCES productos(id)," +
                "descripcion TEXT NOT NULL," +
                "cantidad INTEGER NOT NULL DEFAULT 1," +
                "costo_unitario INTEGER NOT NULL DEFAULT 0," +
                "subtotal INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE pagos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cliente_id INTEGER NOT NULL REFERENCES clientes(id)," +
                "fecha INTEGER NOT NULL," +
                "monto INTEGER NOT NULL," +
                "forma_pago TEXT," +
                "notas TEXT," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE pago_aplicaciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "pago_id INTEGER NOT NULL REFERENCES pagos(id) ON DELETE CASCADE," +
                "venta_id INTEGER NOT NULL REFERENCES ventas(id)," +
                "monto INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE gastos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fecha INTEGER NOT NULL," +
                "categoria TEXT," +
                "descripcion TEXT NOT NULL," +
                "monto INTEGER NOT NULL DEFAULT 0," +
                "forma_pago TEXT," +
                "notas TEXT," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE adjuntos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "entidad TEXT NOT NULL," +
                "entidad_id INTEGER NOT NULL," +
                "ruta TEXT NOT NULL," +
                "nombre TEXT," +
                "mime TEXT," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_ventas_fecha ON ventas(fecha)");
        db.execSQL("CREATE INDEX idx_compras_fecha ON compras(fecha)");
        db.execSQL("CREATE INDEX idx_items_venta ON venta_items(venta_id)");
        db.execSQL("CREATE INDEX idx_items_compra ON compra_items(compra_id)");
        db.execSQL("CREATE INDEX idx_adjuntos ON adjuntos(entidad, entidad_id)");
        db.execSQL("CREATE INDEX idx_pagos_cliente ON pagos(cliente_id)");
        db.execSQL("CREATE INDEX idx_gastos_fecha ON gastos(fecha)");
        db.execSQL("CREATE INDEX idx_aplicaciones_pago ON pago_aplicaciones(pago_id)");
        db.execSQL("CREATE INDEX idx_aplicaciones_venta ON pago_aplicaciones(venta_id)");

        ContentValues v = new ContentValues();
        v.put("clave", "impuesto_pct");
        v.put("valor", "19");
        db.insert("ajustes", null, v);
        v = new ContentValues();
        v.put("clave", "impuesto_activado");
        v.put("valor", "1");
        db.insert("ajustes", null, v);
        v = new ContentValues();
        v.put("clave", "permitir_stock_negativo");
        v.put("valor", "0");
        db.insert("ajustes", null, v);
        v = new ContentValues();
        v.put("clave", "ultima_copia");
        v.put("valor", "0");
        db.insert("ajustes", null, v);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int anterior, int nueva) {
        if (anterior < 2) {
            db.execSQL("ALTER TABLE clientes ADD COLUMN cupo INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE ventas ADD COLUMN fecha_limite INTEGER NOT NULL DEFAULT 0");
            db.execSQL("CREATE TABLE pago_aplicaciones (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "pago_id INTEGER NOT NULL REFERENCES pagos(id) ON DELETE CASCADE," +
                    "venta_id INTEGER NOT NULL REFERENCES ventas(id)," +
                    "monto INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE INDEX idx_aplicaciones_pago ON pago_aplicaciones(pago_id)");
            db.execSQL("CREATE INDEX idx_aplicaciones_venta ON pago_aplicaciones(venta_id)");
            db.execSQL("UPDATE ventas SET fecha_limite = fecha + 2592000000 " +
                    "WHERE estado='credito' AND fecha_limite=0");
            migrarAplicaciones(db);
        }
    }

    /** Reparte los abonos antiguos entre las ventas fiadas, del vencimiento más próximo al más lejano. */
    private void migrarAplicaciones(SQLiteDatabase db) {
        try (Cursor c = db.rawQuery("SELECT id, cliente_id, monto FROM pagos ORDER BY fecha, id", null)) {
            while (c.moveToNext()) {
                aplicarPago(db, c.getLong(0), c.getLong(1), c.getLong(2), 0);
            }
        } catch (Exception ignored) {
            // Si una base antigua trae datos raros, la app sigue funcionando; el saldo
            // general del cliente se calcula con pagos, no con aplicaciones.
        }
    }

    // ---------------------------------------------------------------- ajustes

    public String ajuste(String clave, String defecto) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT valor FROM ajustes WHERE clave=?", new String[]{clave})) {
            if (c.moveToFirst()) {
                String s = c.getString(0);
                return s == null ? defecto : s;
            }
        } catch (Exception e) {
            return defecto;
        }
        return defecto;
    }

    public long ajusteNumero(String clave, long defecto) {
        try {
            return Long.parseLong(ajuste(clave, String.valueOf(defecto)));
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    public double ajusteDecimal(String clave, double defecto) {
        try {
            return Double.parseDouble(ajuste(clave, String.valueOf(defecto)));
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    public boolean ajusteSi(String clave, boolean defecto) {
        return "1".equals(ajuste(clave, defecto ? "1" : "0"));
    }

    public void guardarAjuste(String clave, String valor) {
        ContentValues v = new ContentValues();
        v.put("clave", clave);
        v.put("valor", valor == null ? "" : valor);
        getWritableDatabase().insertWithOnConflict("ajustes", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void guardarAjuste(String clave, long valor) {
        guardarAjuste(clave, String.valueOf(valor));
    }

    public void guardarAjuste(String clave, boolean valor) {
        guardarAjuste(clave, valor ? "1" : "0");
    }

    public String nombreNegocio() {
        String n = ajuste("negocio_nombre", "").trim();
        return n.isEmpty() ? "Mi negocio" : n;
    }

    // --------------------------------------------------------------- productos

    private static String n(String s) {
        return s == null ? "" : s;
    }

    private Modelo.Producto leerProducto(Cursor c) {
        Modelo.Producto p = new Modelo.Producto();
        p.id = c.getLong(c.getColumnIndexOrThrow("id"));
        p.nombre = n(c.getString(c.getColumnIndexOrThrow("nombre")));
        p.categoria = n(c.getString(c.getColumnIndexOrThrow("categoria")));
        p.codigo = n(c.getString(c.getColumnIndexOrThrow("codigo")));
        p.unidad = n(c.getString(c.getColumnIndexOrThrow("unidad")));
        p.precio = c.getLong(c.getColumnIndexOrThrow("precio"));
        p.costo = c.getLong(c.getColumnIndexOrThrow("costo"));
        p.stock = c.getInt(c.getColumnIndexOrThrow("stock"));
        p.stockMinimo = c.getInt(c.getColumnIndexOrThrow("stock_minimo"));
        p.foto = n(c.getString(c.getColumnIndexOrThrow("foto")));
        p.activo = c.getInt(c.getColumnIndexOrThrow("activo")) != 0;
        p.creado = c.getLong(c.getColumnIndexOrThrow("creado"));
        return p;
    }

    public List<Modelo.Producto> productos(String filtro, boolean incluirArchivados) {
        List<Modelo.Producto> lista = new ArrayList<>();
        String f = filtro == null ? "" : filtro.trim().replace("%", " ").replace("_", " ");
        String sql = "SELECT * FROM productos WHERE " + (incluirArchivados ? "1=1" : "activo=1");
        List<String> args = new ArrayList<>();
        if (!f.isEmpty()) {
            sql += " AND (nombre LIKE ? OR categoria LIKE ? OR codigo LIKE ?)";
            String patron = "%" + f + "%";
            args.add(patron);
            args.add(patron);
            args.add(patron);
        }
        sql += " ORDER BY nombre COLLATE NOCASE";
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) lista.add(leerProducto(c));
        }
        return lista;
    }

    public Modelo.Producto producto(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM productos WHERE id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return leerProducto(c);
        }
        return null;
    }

    public long guardarProducto(Modelo.Producto p) {
        ContentValues v = new ContentValues();
        v.put("nombre", p.nombre == null ? "" : p.nombre.trim());
        v.put("categoria", p.categoria == null ? "" : p.categoria.trim());
        v.put("codigo", p.codigo == null ? "" : p.codigo.trim());
        v.put("unidad", p.unidad == null || p.unidad.trim().isEmpty() ? "unidad" : p.unidad.trim());
        v.put("precio", Math.max(0, p.precio));
        v.put("costo", Math.max(0, p.costo));
        v.put("stock", p.stock);
        v.put("stock_minimo", Math.max(0, p.stockMinimo));
        v.put("foto", p.foto == null ? "" : p.foto);
        v.put("activo", p.activo ? 1 : 0);
        if (p.creado == 0) p.creado = Fechas.ahora();
        v.put("creado", p.creado);
        SQLiteDatabase db = getWritableDatabase();
        if (p.id > 0) {
            db.update("productos", v, "id=?", new String[]{String.valueOf(p.id)});
            return p.id;
        }
        p.id = db.insert("productos", null, v);
        return p.id;
    }

    public int referenciasProducto(long id) {
        return (int) (contar("SELECT COUNT(*) FROM venta_items WHERE producto_id=?", String.valueOf(id))
                + contar("SELECT COUNT(*) FROM compra_items WHERE producto_id=?", String.valueOf(id)));
    }

    /** Devuelve true si borró; false si tiene movimientos y no se debe borrar. */
    public boolean eliminarProducto(long id) {
        if (referenciasProducto(id) > 0) return false;
        SQLiteDatabase db = getWritableDatabase();
        db.delete("adjuntos", "entidad=? AND entidad_id=?", new String[]{"producto", String.valueOf(id)});
        db.delete("productos", "id=?", new String[]{String.valueOf(id)});
        return true;
    }

    public void ajustarStock(long id, int nuevoStock) {
        ContentValues v = new ContentValues();
        v.put("stock", nuevoStock);
        getWritableDatabase().update("productos", v, "id=?", new String[]{String.valueOf(id)});
    }

    public List<Modelo.Producto> productosStockBajo(int limite) {
        List<Modelo.Producto> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM productos WHERE activo=1 AND stock<=stock_minimo ORDER BY stock ASC, nombre LIMIT ?",
                new String[]{String.valueOf(limite)})) {
            while (c.moveToNext()) lista.add(leerProducto(c));
        }
        return lista;
    }

    public int totalProductos() {
        return (int) contar("SELECT COUNT(*) FROM productos WHERE activo=1");
    }

    // ---------------------------------------------------------------- clientes

    private Modelo.Cliente leerCliente(Cursor c) {
        Modelo.Cliente cl = new Modelo.Cliente();
        cl.id = c.getLong(c.getColumnIndexOrThrow("id"));
        cl.nombre = n(c.getString(c.getColumnIndexOrThrow("nombre")));
        cl.telefono = n(c.getString(c.getColumnIndexOrThrow("telefono")));
        cl.correo = n(c.getString(c.getColumnIndexOrThrow("correo")));
        cl.direccion = n(c.getString(c.getColumnIndexOrThrow("direccion")));
        cl.notas = n(c.getString(c.getColumnIndexOrThrow("notas")));
        cl.foto = n(c.getString(c.getColumnIndexOrThrow("foto")));
        cl.creado = c.getLong(c.getColumnIndexOrThrow("creado"));
        int idx = c.getColumnIndex("saldo");
        cl.saldo = idx >= 0 ? c.getLong(idx) : 0;
        idx = c.getColumnIndex("cupo");
        cl.cupo = idx >= 0 ? c.getLong(idx) : 0;
        return cl;
    }

    private static final String SQL_CLIENTE_SALDO =
            "SELECT c.*, " +
            "(SELECT COALESCE(SUM(v.total),0) FROM ventas v WHERE v.cliente_id=c.id AND v.anulada=0 AND v.estado='credito') " +
            "- (SELECT COALESCE(SUM(p.monto),0) FROM pagos p WHERE p.cliente_id=c.id) AS saldo " +
            "FROM clientes c ";

    public List<Modelo.Cliente> clientes(String filtro) {
        List<Modelo.Cliente> lista = new ArrayList<>();
        String f = filtro == null ? "" : filtro.trim().replace("%", " ").replace("_", " ");
        String sql = SQL_CLIENTE_SALDO;
        List<String> args = new ArrayList<>();
        if (!f.isEmpty()) {
            sql += "WHERE c.nombre LIKE ? OR c.telefono LIKE ? OR c.correo LIKE ? ";
            String patron = "%" + f + "%";
            args.add(patron);
            args.add(patron);
            args.add(patron);
        }
        sql += "ORDER BY c.nombre COLLATE NOCASE";
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) lista.add(leerCliente(c));
        }
        return lista;
    }

    public Modelo.Cliente cliente(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(SQL_CLIENTE_SALDO + "WHERE c.id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return leerCliente(c);
        }
        return null;
    }

    public long guardarCliente(Modelo.Cliente cl) {
        ContentValues v = new ContentValues();
        v.put("nombre", cl.nombre == null ? "" : cl.nombre.trim());
        v.put("telefono", cl.telefono == null ? "" : cl.telefono.trim());
        v.put("correo", cl.correo == null ? "" : cl.correo.trim());
        v.put("direccion", cl.direccion == null ? "" : cl.direccion.trim());
        v.put("notas", cl.notas == null ? "" : cl.notas.trim());
        v.put("foto", cl.foto == null ? "" : cl.foto);
        v.put("cupo", Math.max(0, cl.cupo));
        if (cl.creado == 0) cl.creado = Fechas.ahora();
        v.put("creado", cl.creado);
        SQLiteDatabase db = getWritableDatabase();
        if (cl.id > 0) {
            db.update("clientes", v, "id=?", new String[]{String.valueOf(cl.id)});
            return cl.id;
        }
        cl.id = db.insert("clientes", null, v);
        return cl.id;
    }

    public int referenciasCliente(long id) {
        return (int) (contar("SELECT COUNT(*) FROM ventas WHERE cliente_id=?", String.valueOf(id))
                + contar("SELECT COUNT(*) FROM pagos WHERE cliente_id=?", String.valueOf(id)));
    }

    public boolean eliminarCliente(long id) {
        if (referenciasCliente(id) > 0) return false;
        SQLiteDatabase db = getWritableDatabase();
        db.delete("adjuntos", "entidad=? AND entidad_id=?", new String[]{"cliente", String.valueOf(id)});
        db.delete("clientes", "id=?", new String[]{String.valueOf(id)});
        return true;
    }

    public long saldoCliente(long id) {
        return suma("SELECT " +
                "(SELECT COALESCE(SUM(total),0) FROM ventas WHERE cliente_id=? AND anulada=0 AND estado='credito')" +
                " - (SELECT COALESCE(SUM(monto),0) FROM pagos WHERE cliente_id=?)",
                String.valueOf(id), String.valueOf(id));
    }

    /** Total que deben los clientes: la suma del saldo de cada venta fiada.
     *
     *  Antes era «ventas fiadas menos todos los abonos». Si un abono quedaba sin
     *  aplicar (por ejemplo, al anular una venta) o el cliente pagaba de más, la
     *  cifra de Inicio no coincidía con la lista de Deudas. Ahora es exactamente
     *  la suma que se ve allí, y nunca queda negativa: el sobrante de un cliente
     *  es su saldo a favor, no una deuda menos.
     */
    public long porCobrar() {
        return suma("SELECT COALESCE(SUM(MAX(0, v.total - COALESCE(" +
                "(SELECT SUM(a.monto) FROM pago_aplicaciones a WHERE a.venta_id=v.id),0))),0) " +
                "FROM ventas v WHERE v.estado='credito' AND v.anulada=0");
    }

    public int totalClientes() {
        return (int) contar("SELECT COUNT(*) FROM clientes");
    }

    // ------------------------------------------------------------- proveedores

    private Modelo.Proveedor leerProveedor(Cursor c) {
        Modelo.Proveedor p = new Modelo.Proveedor();
        p.id = c.getLong(c.getColumnIndexOrThrow("id"));
        p.nombre = n(c.getString(c.getColumnIndexOrThrow("nombre")));
        p.contacto = n(c.getString(c.getColumnIndexOrThrow("contacto")));
        p.telefono = n(c.getString(c.getColumnIndexOrThrow("telefono")));
        p.correo = n(c.getString(c.getColumnIndexOrThrow("correo")));
        p.direccion = n(c.getString(c.getColumnIndexOrThrow("direccion")));
        p.notas = n(c.getString(c.getColumnIndexOrThrow("notas")));
        p.foto = n(c.getString(c.getColumnIndexOrThrow("foto")));
        p.creado = c.getLong(c.getColumnIndexOrThrow("creado"));
        return p;
    }

    public List<Modelo.Proveedor> proveedores(String filtro) {
        List<Modelo.Proveedor> lista = new ArrayList<>();
        String f = filtro == null ? "" : filtro.trim().replace("%", " ").replace("_", " ");
        String sql = "SELECT * FROM proveedores ";
        List<String> args = new ArrayList<>();
        if (!f.isEmpty()) {
            sql += "WHERE nombre LIKE ? OR contacto LIKE ? OR telefono LIKE ? ";
            String patron = "%" + f + "%";
            args.add(patron);
            args.add(patron);
            args.add(patron);
        }
        sql += "ORDER BY nombre COLLATE NOCASE";
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) lista.add(leerProveedor(c));
        }
        return lista;
    }

    public Modelo.Proveedor proveedor(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM proveedores WHERE id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return leerProveedor(c);
        }
        return null;
    }

    public long guardarProveedor(Modelo.Proveedor p) {
        ContentValues v = new ContentValues();
        v.put("nombre", p.nombre == null ? "" : p.nombre.trim());
        v.put("contacto", p.contacto == null ? "" : p.contacto.trim());
        v.put("telefono", p.telefono == null ? "" : p.telefono.trim());
        v.put("correo", p.correo == null ? "" : p.correo.trim());
        v.put("direccion", p.direccion == null ? "" : p.direccion.trim());
        v.put("notas", p.notas == null ? "" : p.notas.trim());
        v.put("foto", p.foto == null ? "" : p.foto);
        if (p.creado == 0) p.creado = Fechas.ahora();
        v.put("creado", p.creado);
        SQLiteDatabase db = getWritableDatabase();
        if (p.id > 0) {
            db.update("proveedores", v, "id=?", new String[]{String.valueOf(p.id)});
            return p.id;
        }
        p.id = db.insert("proveedores", null, v);
        return p.id;
    }

    public int referenciasProveedor(long id) {
        return (int) contar("SELECT COUNT(*) FROM compras WHERE proveedor_id=?", String.valueOf(id));
    }

    public boolean eliminarProveedor(long id) {
        if (referenciasProveedor(id) > 0) return false;
        SQLiteDatabase db = getWritableDatabase();
        db.delete("adjuntos", "entidad=? AND entidad_id=?", new String[]{"proveedor", String.valueOf(id)});
        db.delete("proveedores", "id=?", new String[]{String.valueOf(id)});
        return true;
    }

    public int totalProveedores() {
        return (int) contar("SELECT COUNT(*) FROM proveedores");
    }

    // ------------------------------------------------------------------ ventas

    private Modelo.Venta leerVenta(Cursor c) {
        Modelo.Venta v = new Modelo.Venta();
        v.id = c.getLong(c.getColumnIndexOrThrow("id"));
        v.fecha = c.getLong(c.getColumnIndexOrThrow("fecha"));
        v.clienteId = c.getLong(c.getColumnIndexOrThrow("cliente_id"));
        v.subtotal = c.getLong(c.getColumnIndexOrThrow("subtotal"));
        v.descuento = c.getLong(c.getColumnIndexOrThrow("descuento"));
        v.impuestoPct = c.getDouble(c.getColumnIndexOrThrow("impuesto_pct"));
        v.impuesto = c.getLong(c.getColumnIndexOrThrow("impuesto"));
        v.total = c.getLong(c.getColumnIndexOrThrow("total"));
        v.costo = c.getLong(c.getColumnIndexOrThrow("costo"));
        int idxLimite = c.getColumnIndex("fecha_limite");
        v.fechaLimite = idxLimite >= 0 ? c.getLong(idxLimite) : 0;
        int idxAbonado = c.getColumnIndex("abonado");
        v.abonado = idxAbonado >= 0 ? c.getLong(idxAbonado) : 0;
        v.formaPago = n(c.getString(c.getColumnIndexOrThrow("forma_pago")));
        v.estado = n(c.getString(c.getColumnIndexOrThrow("estado")));
        v.saldo = "credito".equals(v.estado) ? Math.max(0, v.total - v.abonado) : 0;
        v.notas = n(c.getString(c.getColumnIndexOrThrow("notas")));
        v.anulada = c.getInt(c.getColumnIndexOrThrow("anulada")) != 0;
        int idx = c.getColumnIndex("cliente_nombre");
        v.clienteNombre = idx >= 0 ? n(c.getString(idx)) : "";
        idx = c.getColumnIndex("num_items");
        v.numItems = idx >= 0 ? c.getInt(idx) : 0;
        return v;
    }

    private static final String SQL_VENTA =
            "SELECT v.*, c.nombre AS cliente_nombre, " +
            "(SELECT COUNT(*) FROM venta_items vi WHERE vi.venta_id=v.id) AS num_items, " +
            "COALESCE((SELECT SUM(a.monto) FROM pago_aplicaciones a WHERE a.venta_id=v.id),0) AS abonado " +
            "FROM ventas v LEFT JOIN clientes c ON c.id=v.cliente_id ";

    public List<Modelo.Venta> ventas(String filtro) {
        List<Modelo.Venta> lista = new ArrayList<>();
        String f = filtro == null ? "" : filtro.trim().replace("%", " ").replace("_", " ");
        String sql = SQL_VENTA;
        List<String> args = new ArrayList<>();
        if (!f.isEmpty()) {
            sql += "WHERE c.nombre LIKE ? OR v.notas LIKE ? OR CAST(v.id AS TEXT) LIKE ? ";
            String patron = "%" + f + "%";
            args.add(patron);
            args.add(patron);
            args.add(patron);
        }
        sql += "ORDER BY v.fecha DESC, v.id DESC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) lista.add(leerVenta(c));
        }
        return lista;
    }

    public List<Modelo.Venta> ultimasVentas(int limite) {
        List<Modelo.Venta> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                SQL_VENTA + "ORDER BY v.fecha DESC, v.id DESC LIMIT ?",
                new String[]{String.valueOf(limite)})) {
            while (c.moveToNext()) lista.add(leerVenta(c));
        }
        return lista;
    }

    public Modelo.Venta venta(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(SQL_VENTA + "WHERE v.id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return leerVenta(c);
        }
        return null;
    }

    public List<Modelo.Venta> ventasCliente(long clienteId, int limite) {
        List<Modelo.Venta> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                SQL_VENTA + "WHERE v.cliente_id=? ORDER BY v.fecha DESC, v.id DESC LIMIT ?",
                new String[]{String.valueOf(clienteId), String.valueOf(limite)})) {
            while (c.moveToNext()) lista.add(leerVenta(c));
        }
        return lista;
    }

    public List<Modelo.Item> itemsVenta(long ventaId) {
        List<Modelo.Item> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM venta_items WHERE venta_id=? ORDER BY id",
                new String[]{String.valueOf(ventaId)})) {
            while (c.moveToNext()) {
                Modelo.Item i = new Modelo.Item();
                i.id = c.getLong(c.getColumnIndexOrThrow("id"));
                i.ventaId = c.getLong(c.getColumnIndexOrThrow("venta_id"));
                i.compraId = 0;
                i.productoId = c.getLong(c.getColumnIndexOrThrow("producto_id"));
                i.descripcion = n(c.getString(c.getColumnIndexOrThrow("descripcion")));
                i.cantidad = c.getInt(c.getColumnIndexOrThrow("cantidad"));
                i.precioUnitario = c.getLong(c.getColumnIndexOrThrow("precio_unitario"));
                i.costoUnitario = c.getLong(c.getColumnIndexOrThrow("costo_unitario"));
                i.subtotal = c.getLong(c.getColumnIndexOrThrow("subtotal"));
                lista.add(i);
            }
        }
        return lista;
    }

    public long guardarVenta(Modelo.Venta v, List<Modelo.Item> items, List<Modelo.Adjunto> adjuntos) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (v.fecha == 0) v.fecha = Fechas.ahora();
            ContentValues cv = new ContentValues();
            cv.put("fecha", v.fecha);
            if (v.clienteId > 0) cv.put("cliente_id", v.clienteId);
            else cv.putNull("cliente_id");
            cv.put("subtotal", v.subtotal);
            cv.put("descuento", v.descuento);
            cv.put("impuesto_pct", v.impuestoPct);
            cv.put("impuesto", v.impuesto);
            cv.put("total", v.total);
            cv.put("costo", v.costo);
            cv.put("fecha_limite", v.fechaLimite);
            cv.put("forma_pago", v.formaPago);
            cv.put("estado", v.estado);
            cv.put("notas", v.notas);
            cv.put("anulada", 0);
            cv.put("creado", Fechas.ahora());
            v.id = db.insert("ventas", null, cv);
            for (Modelo.Item i : items) {
                ContentValues ci = new ContentValues();
                ci.put("venta_id", v.id);
                if (i.productoId > 0) ci.put("producto_id", i.productoId);
                else ci.putNull("producto_id");
                ci.put("descripcion", i.descripcion);
                ci.put("cantidad", i.cantidad);
                ci.put("precio_unitario", i.precioUnitario);
                ci.put("costo_unitario", i.costoUnitario);
                ci.put("subtotal", i.subtotal);
                db.insert("venta_items", null, ci);
                if (i.productoId > 0) {
                    db.execSQL("UPDATE productos SET stock=stock-? WHERE id=?",
                            new Object[]{i.cantidad, i.productoId});
                }
            }
            insertarAdjuntos(db, "venta", v.id, adjuntos);
            if ("credito".equals(v.estado) && v.clienteId > 0) {
                /* Si el cliente tiene plata a favor de una venta anulada o de un
                   abono mayor que su deuda, esta venta la consume. */
                recalcularAplicaciones(db, v.clienteId, 0, 0);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return v.id;
    }

    public void anularVenta(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Modelo.Venta v = venta(id);
            if (v == null || v.anulada) {
                db.setTransactionSuccessful();
                return;
            }
            for (Modelo.Item i : itemsVenta(id)) {
                if (i.productoId > 0) {
                    db.execSQL("UPDATE productos SET stock=stock+? WHERE id=?",
                            new Object[]{i.cantidad, i.productoId});
                }
            }
            ContentValues cv = new ContentValues();
            cv.put("anulada", 1);
            db.update("ventas", cv, "id=?", new String[]{String.valueOf(id)});
            /* Los abonos que estaban en esta venta no se pierden: vuelven al
               reparto del cliente y, si no hay otra deuda, quedan a favor. */
            recalcularAplicaciones(db, v.clienteId, 0, 0);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ----------------------------------------------------------------- compras

    private Modelo.Compra leerCompra(Cursor c) {
        Modelo.Compra p = new Modelo.Compra();
        p.id = c.getLong(c.getColumnIndexOrThrow("id"));
        p.fecha = c.getLong(c.getColumnIndexOrThrow("fecha"));
        p.proveedorId = c.getLong(c.getColumnIndexOrThrow("proveedor_id"));
        p.subtotal = c.getLong(c.getColumnIndexOrThrow("subtotal"));
        p.impuestoPct = c.getDouble(c.getColumnIndexOrThrow("impuesto_pct"));
        p.impuesto = c.getLong(c.getColumnIndexOrThrow("impuesto"));
        p.total = c.getLong(c.getColumnIndexOrThrow("total"));
        p.formaPago = n(c.getString(c.getColumnIndexOrThrow("forma_pago")));
        p.notas = n(c.getString(c.getColumnIndexOrThrow("notas")));
        p.anulada = c.getInt(c.getColumnIndexOrThrow("anulada")) != 0;
        int idx = c.getColumnIndex("proveedor_nombre");
        p.proveedorNombre = idx >= 0 ? n(c.getString(idx)) : "";
        idx = c.getColumnIndex("num_items");
        p.numItems = idx >= 0 ? c.getInt(idx) : 0;
        return p;
    }

    private static final String SQL_COMPRA =
            "SELECT c.*, p.nombre AS proveedor_nombre, " +
            "(SELECT COUNT(*) FROM compra_items ci WHERE ci.compra_id=c.id) AS num_items " +
            "FROM compras c LEFT JOIN proveedores p ON p.id=c.proveedor_id ";

    public List<Modelo.Compra> compras(String filtro) {
        List<Modelo.Compra> lista = new ArrayList<>();
        String f = filtro == null ? "" : filtro.trim().replace("%", " ").replace("_", " ");
        String sql = SQL_COMPRA;
        List<String> args = new ArrayList<>();
        if (!f.isEmpty()) {
            sql += "WHERE p.nombre LIKE ? OR c.notas LIKE ? OR CAST(c.id AS TEXT) LIKE ? ";
            String patron = "%" + f + "%";
            args.add(patron);
            args.add(patron);
            args.add(patron);
        }
        sql += "ORDER BY c.fecha DESC, c.id DESC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) lista.add(leerCompra(c));
        }
        return lista;
    }

    public Modelo.Compra compra(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(SQL_COMPRA + "WHERE c.id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return leerCompra(c);
        }
        return null;
    }

    public List<Modelo.Compra> comprasProveedor(long proveedorId, int limite) {
        List<Modelo.Compra> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                SQL_COMPRA + "WHERE c.proveedor_id=? ORDER BY c.fecha DESC, c.id DESC LIMIT ?",
                new String[]{String.valueOf(proveedorId), String.valueOf(limite)})) {
            while (c.moveToNext()) lista.add(leerCompra(c));
        }
        return lista;
    }

    public List<Modelo.Item> itemsCompra(long compraId) {
        List<Modelo.Item> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM compra_items WHERE compra_id=? ORDER BY id",
                new String[]{String.valueOf(compraId)})) {
            while (c.moveToNext()) {
                Modelo.Item i = new Modelo.Item();
                i.id = c.getLong(c.getColumnIndexOrThrow("id"));
                i.compraId = c.getLong(c.getColumnIndexOrThrow("compra_id"));
                i.productoId = c.getLong(c.getColumnIndexOrThrow("producto_id"));
                i.descripcion = n(c.getString(c.getColumnIndexOrThrow("descripcion")));
                i.cantidad = c.getInt(c.getColumnIndexOrThrow("cantidad"));
                i.costoUnitario = c.getLong(c.getColumnIndexOrThrow("costo_unitario"));
                i.precioUnitario = i.costoUnitario;
                i.subtotal = c.getLong(c.getColumnIndexOrThrow("subtotal"));
                lista.add(i);
            }
        }
        return lista;
    }

    public long guardarCompra(Modelo.Compra compra, List<Modelo.Item> items, List<Modelo.Adjunto> adjuntos) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (compra.fecha == 0) compra.fecha = Fechas.ahora();
            ContentValues cv = new ContentValues();
            cv.put("fecha", compra.fecha);
            if (compra.proveedorId > 0) cv.put("proveedor_id", compra.proveedorId);
            else cv.putNull("proveedor_id");
            cv.put("subtotal", compra.subtotal);
            cv.put("impuesto_pct", compra.impuestoPct);
            cv.put("impuesto", compra.impuesto);
            cv.put("total", compra.total);
            cv.put("forma_pago", compra.formaPago);
            cv.put("notas", compra.notas);
            cv.put("anulada", 0);
            cv.put("creado", Fechas.ahora());
            compra.id = db.insert("compras", null, cv);
            for (Modelo.Item i : items) {
                ContentValues ci = new ContentValues();
                ci.put("compra_id", compra.id);
                if (i.productoId > 0) ci.put("producto_id", i.productoId);
                else ci.putNull("producto_id");
                ci.put("descripcion", i.descripcion);
                ci.put("cantidad", i.cantidad);
                ci.put("costo_unitario", i.costoUnitario);
                ci.put("subtotal", i.subtotal);
                db.insert("compra_items", null, ci);
                if (i.productoId > 0) {
                    db.execSQL("UPDATE productos SET stock=stock+? WHERE id=?",
                            new Object[]{i.cantidad, i.productoId});
                    if (i.costoUnitario > 0) {
                        db.execSQL("UPDATE productos SET costo=? WHERE id=?",
                                new Object[]{i.costoUnitario, i.productoId});
                    }
                }
            }
            insertarAdjuntos(db, "compra", compra.id, adjuntos);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return compra.id;
    }

    /** Último costo de compra vigente del producto, sin contar la compra indicada. */
    private long costoUltimaCompra(SQLiteDatabase db, long productoId, long excluir) {
        try (Cursor c = db.rawQuery(
                "SELECT ci.costo_unitario FROM compra_items ci JOIN compras c ON c.id=ci.compra_id " +
                        "WHERE ci.producto_id=? AND c.anulada=0 AND c.id<>? AND ci.costo_unitario>0 " +
                        "ORDER BY c.fecha DESC, ci.id DESC LIMIT 1",
                new String[]{String.valueOf(productoId), String.valueOf(excluir)})) {
            if (c.moveToFirst()) return c.getLong(0);
        }
        return 0;
    }

    public void anularCompra(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Modelo.Compra compra = compra(id);
            if (compra == null || compra.anulada) {
                db.setTransactionSuccessful();
                return;
            }
            for (Modelo.Item i : itemsCompra(id)) {
                if (i.productoId > 0) {
                    db.execSQL("UPDATE productos SET stock=stock-? WHERE id=?",
                            new Object[]{i.cantidad, i.productoId});
                    /* El costo del producto lo puso esta compra: al anularla vuelve
                       al de la última compra que siga vigente. */
                    long costo = costoUltimaCompra(db, i.productoId, id);
                    if (costo > 0) {
                        db.execSQL("UPDATE productos SET costo=? WHERE id=?",
                                new Object[]{costo, i.productoId});
                    }
                }
            }
            ContentValues cv = new ContentValues();
            cv.put("anulada", 1);
            db.update("compras", cv, "id=?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ------------------------------------------------------------------- pagos

    public long registrarPago(Modelo.Pago p, List<Modelo.Adjunto> adjuntos) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (p.fecha == 0) p.fecha = Fechas.ahora();
            ContentValues cv = new ContentValues();
            cv.put("cliente_id", p.clienteId);
            cv.put("fecha", p.fecha);
            cv.put("monto", p.monto);
            cv.put("forma_pago", p.formaPago);
            cv.put("notas", p.notas);
            cv.put("creado", Fechas.ahora());
            p.id = db.insert("pagos", null, cv);
            insertarAdjuntos(db, "pago", p.id, adjuntos);
            /* Se reparte todo lo que el cliente ha abonado (incluido este pago),
               respetando la venta que eligió el usuario si la eligió. */
            recalcularAplicaciones(db, p.clienteId, p.id, p.ventaId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return p.id;
    }

    public List<Modelo.Pago> pagosCliente(long clienteId) {
        List<Modelo.Pago> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT p.*, c.nombre AS cliente_nombre FROM pagos p LEFT JOIN clientes c ON c.id=p.cliente_id " +
                        "WHERE p.cliente_id=? ORDER BY p.fecha DESC, p.id DESC",
                new String[]{String.valueOf(clienteId)})) {
            while (c.moveToNext()) {
                Modelo.Pago p = new Modelo.Pago();
                p.id = c.getLong(c.getColumnIndexOrThrow("id"));
                p.clienteId = c.getLong(c.getColumnIndexOrThrow("cliente_id"));
                p.fecha = c.getLong(c.getColumnIndexOrThrow("fecha"));
                p.monto = c.getLong(c.getColumnIndexOrThrow("monto"));
                p.formaPago = n(c.getString(c.getColumnIndexOrThrow("forma_pago")));
                p.notas = n(c.getString(c.getColumnIndexOrThrow("notas")));
                p.clienteNombre = n(c.getString(c.getColumnIndexOrThrow("cliente_nombre")));
                lista.add(p);
            }
        }
        return lista;
    }

    public void eliminarPago(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long clienteId = 0;
            try (Cursor c = db.rawQuery("SELECT cliente_id FROM pagos WHERE id=?",
                    new String[]{String.valueOf(id)})) {
                if (c.moveToFirst()) clienteId = c.getLong(0);
            }
            db.delete("pago_aplicaciones", "pago_id=?", new String[]{String.valueOf(id)});
            db.delete("pagos", "id=?", new String[]{String.valueOf(id)});
            db.delete("adjuntos", "entidad=? AND entidad_id=?",
                    new String[]{"pago", String.valueOf(id)});
            /* Lo que pagaban los otros abonos se reparte otra vez. */
            recalcularAplicaciones(db, clienteId, 0, 0);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Reparte un abono entre las ventas fiadas pendientes, empezando por la indicada o la más urgente. */
    private void aplicarPago(SQLiteDatabase db, long pagoId, long clienteId, long monto, long ventaPreferida) {
        if (monto <= 0 || clienteId <= 0) return;
        List<Long> orden = new ArrayList<>();
        if (ventaPreferida > 0) orden.add(ventaPreferida);
        try (Cursor c = db.rawQuery(
                "SELECT v.id FROM ventas v WHERE v.cliente_id=? AND v.estado='credito' AND v.anulada=0 " +
                        "AND v.total > COALESCE((SELECT SUM(a.monto) FROM pago_aplicaciones a WHERE a.venta_id=v.id),0) " +
                        "ORDER BY CASE WHEN v.fecha_limite=0 THEN 1 ELSE 0 END, v.fecha_limite, v.fecha, v.id",
                new String[]{String.valueOf(clienteId)})) {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                if (!orden.contains(id)) orden.add(id);
            }
        }
        long resto = monto;
        for (long ventaId : orden) {
            if (resto <= 0) break;
            long saldo = saldoVentaDb(db, ventaId);
            if (saldo <= 0) continue;
            long aplicar = Math.min(resto, saldo);
            ContentValues cv = new ContentValues();
            cv.put("pago_id", pagoId);
            cv.put("venta_id", ventaId);
            cv.put("monto", aplicar);
            db.insert("pago_aplicaciones", null, cv);
            resto -= aplicar;
        }
    }

    private long saldoVentaDb(SQLiteDatabase db, long ventaId) {
        try (Cursor c = db.rawQuery(
                "SELECT v.total - COALESCE((SELECT SUM(a.monto) FROM pago_aplicaciones a WHERE a.venta_id=v.id),0) " +
                        "FROM ventas v WHERE v.id=? AND v.anulada=0 AND v.estado='credito'",
                new String[]{String.valueOf(ventaId)})) {
            if (c.moveToFirst()) return Math.max(0, c.getLong(0));
        }
        return 0;
    }

    /** Vuelve a repartir TODOS los abonos del cliente entre sus deudas abiertas.
     *
     *  Un abono es plata que ya entró y no pertenece a una venta concreta: las
     *  aplicaciones son solo cómo se reparte esa plata entre las deudas. Cuando
     *  una venta se anula, cuando se registra otra venta fiada o cuando se borra
     *  un abono, el reparto viejo deja de servir; aquí se borra y se hace de
     *  nuevo, del vencimiento más próximo al más lejano. Lo que sobre queda como
     *  saldo a favor del cliente y se aplicará a su próxima venta fiada.
     *
     *  Sin esto, Inicio («por cobrar» con el total de abonos) y Deudas (saldo por
     *  venta con lo aplicado) podían dar cifras distintas para el mismo cliente.
     */
    private void recalcularAplicaciones(SQLiteDatabase db, long clienteId,
                                        long pagoPreferido, long ventaPreferida) {
        if (clienteId <= 0) return;
        db.delete("pago_aplicaciones",
                "pago_id IN (SELECT id FROM pagos WHERE cliente_id=?)",
                new String[]{String.valueOf(clienteId)});
        List<long[]> pagos = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT id, monto FROM pagos WHERE cliente_id=? ORDER BY fecha, id",
                new String[]{String.valueOf(clienteId)})) {
            while (c.moveToNext()) pagos.add(new long[]{c.getLong(0), c.getLong(1)});
        }
        for (long[] p : pagos) {
            aplicarPago(db, p[0], clienteId, p[1], p[0] == pagoPreferido ? ventaPreferida : 0);
        }
    }

    public long saldoVenta(long ventaId) {
        return saldoVentaDb(getReadableDatabase(), ventaId);
    }

    public List<Modelo.Deuda> deudas() {
        return deudas(0);
    }

    public List<Modelo.Deuda> deudasCliente(long clienteId) {
        return deudas(clienteId);
    }

    private static final String SQL_DEUDA =
            "SELECT v.id, v.cliente_id, v.fecha, v.fecha_limite, v.total, v.forma_pago, v.notas, " +
                    "c.nombre AS cliente_nombre, c.telefono AS cliente_telefono, " +
                    "COALESCE((SELECT SUM(a.monto) FROM pago_aplicaciones a WHERE a.venta_id=v.id),0) AS abonado " +
                    "FROM ventas v LEFT JOIN clientes c ON c.id=v.cliente_id " +
                    "WHERE v.estado='credito' AND v.anulada=0 ";

    private List<Modelo.Deuda> deudas(long clienteId) {
        List<Modelo.Deuda> lista = new ArrayList<>();
        String sql = SQL_DEUDA;
        List<String> args = new ArrayList<>();
        if (clienteId > 0) {
            sql += "AND v.cliente_id=? ";
            args.add(String.valueOf(clienteId));
        }
        sql += "ORDER BY CASE WHEN v.fecha_limite=0 THEN 1 ELSE 0 END, v.fecha_limite, v.fecha, v.id";
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) {
                Modelo.Deuda d = new Modelo.Deuda();
                d.ventaId = c.getLong(0);
                d.clienteId = c.getLong(1);
                d.fecha = c.getLong(2);
                d.fechaLimite = c.getLong(3);
                d.total = c.getLong(4);
                d.formaPago = n(c.getString(5));
                d.notas = n(c.getString(6));
                d.clienteNombre = n(c.getString(7));
                d.clienteTelefono = n(c.getString(8));
                d.abonado = c.getLong(9);
                d.saldo = Math.max(0, d.total - d.abonado);
                if (d.saldo > 0) lista.add(d);
            }
        }
        return lista;
    }

    public Modelo.Deuda deudaVenta(long ventaId) {
        Modelo.Venta v = venta(ventaId);
        if (v == null || v.anulada || !"credito".equals(v.estado)) return null;
        long abonado = suma("SELECT COALESCE(SUM(monto),0) FROM pago_aplicaciones WHERE venta_id=?",
                String.valueOf(ventaId));
        Modelo.Deuda d = new Modelo.Deuda();
        d.ventaId = v.id;
        d.clienteId = v.clienteId;
        d.fecha = v.fecha;
        d.fechaLimite = v.fechaLimite;
        d.total = v.total;
        d.abonado = abonado;
        d.saldo = Math.max(0, v.total - abonado);
        d.clienteNombre = v.clienteNombre;
        Modelo.Cliente cliente = cliente(v.clienteId);
        d.clienteTelefono = cliente == null ? "" : cliente.telefono;
        d.formaPago = v.formaPago;
        d.notas = v.notas;
        return d;
    }

    public long totalCompradoCliente(long clienteId) {
        return suma("SELECT COALESCE(SUM(total),0) FROM ventas WHERE cliente_id=? AND anulada=0",
                String.valueOf(clienteId));
    }

    public long totalAbonadoCliente(long clienteId) {
        return suma("SELECT COALESCE(SUM(monto),0) FROM pagos WHERE cliente_id=?",
                String.valueOf(clienteId));
    }

    // ------------------------------------------------------------------ gastos

    private Modelo.Gasto leerGasto(Cursor c) {
        Modelo.Gasto g = new Modelo.Gasto();
        g.id = c.getLong(c.getColumnIndexOrThrow("id"));
        g.fecha = c.getLong(c.getColumnIndexOrThrow("fecha"));
        g.categoria = n(c.getString(c.getColumnIndexOrThrow("categoria")));
        g.descripcion = n(c.getString(c.getColumnIndexOrThrow("descripcion")));
        g.monto = c.getLong(c.getColumnIndexOrThrow("monto"));
        g.formaPago = n(c.getString(c.getColumnIndexOrThrow("forma_pago")));
        g.notas = n(c.getString(c.getColumnIndexOrThrow("notas")));
        return g;
    }

    public List<Modelo.Gasto> gastos(String filtro) {
        List<Modelo.Gasto> lista = new ArrayList<>();
        String f = filtro == null ? "" : filtro.trim().replace("%", " ").replace("_", " ");
        String sql = "SELECT * FROM gastos ";
        List<String> args = new ArrayList<>();
        if (!f.isEmpty()) {
            sql += "WHERE descripcion LIKE ? OR categoria LIKE ? ";
            String patron = "%" + f + "%";
            args.add(patron);
            args.add(patron);
        }
        sql += "ORDER BY fecha DESC, id DESC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) lista.add(leerGasto(c));
        }
        return lista;
    }

    public Modelo.Gasto gasto(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM gastos WHERE id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return leerGasto(c);
        }
        return null;
    }

    public long guardarGasto(Modelo.Gasto g) {
        ContentValues v = new ContentValues();
        if (g.fecha == 0) g.fecha = Fechas.ahora();
        v.put("fecha", g.fecha);
        v.put("categoria", g.categoria == null ? "" : g.categoria.trim());
        v.put("descripcion", g.descripcion == null ? "" : g.descripcion.trim());
        v.put("monto", Math.max(0, g.monto));
        v.put("forma_pago", g.formaPago);
        v.put("notas", g.notas == null ? "" : g.notas);
        v.put("creado", Fechas.ahora());
        SQLiteDatabase db = getWritableDatabase();
        if (g.id > 0) {
            db.update("gastos", v, "id=?", new String[]{String.valueOf(g.id)});
            return g.id;
        }
        g.id = db.insert("gastos", null, v);
        return g.id;
    }

    public void eliminarGasto(long id) {
        getWritableDatabase().delete("gastos", "id=?", new String[]{String.valueOf(id)});
        getWritableDatabase().delete("adjuntos", "entidad=? AND entidad_id=?",
                new String[]{"gasto", String.valueOf(id)});
    }

    // --------------------------------------------------------------- adjuntos

    public void insertarAdjuntos(SQLiteDatabase db, String entidad, long entidadId,
                                 List<Modelo.Adjunto> adjuntos) {
        if (adjuntos == null) return;
        for (Modelo.Adjunto a : adjuntos) {
            ContentValues v = new ContentValues();
            v.put("entidad", entidad);
            v.put("entidad_id", entidadId);
            v.put("ruta", a.ruta);
            v.put("nombre", a.nombre);
            v.put("mime", a.mime);
            v.put("creado", Fechas.ahora());
            db.insert("adjuntos", null, v);
        }
    }

    public void agregarAdjuntos(String entidad, long entidadId, List<Modelo.Adjunto> lista) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            insertarAdjuntos(db, entidad, entidadId, lista);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public long agregarAdjunto(Modelo.Adjunto a) {
        ContentValues v = new ContentValues();
        v.put("entidad", a.entidad);
        v.put("entidad_id", a.entidadId);
        v.put("ruta", a.ruta);
        v.put("nombre", a.nombre);
        v.put("mime", a.mime);
        v.put("creado", Fechas.ahora());
        return getWritableDatabase().insert("adjuntos", null, v);
    }

    public List<Modelo.Adjunto> adjuntos(String entidad, long entidadId) {
        List<Modelo.Adjunto> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM adjuntos WHERE entidad=? AND entidad_id=? ORDER BY id",
                new String[]{entidad, String.valueOf(entidadId)})) {
            while (c.moveToNext()) {
                Modelo.Adjunto a = new Modelo.Adjunto();
                a.id = c.getLong(c.getColumnIndexOrThrow("id"));
                a.entidad = n(c.getString(c.getColumnIndexOrThrow("entidad")));
                a.entidadId = c.getLong(c.getColumnIndexOrThrow("entidad_id"));
                a.ruta = n(c.getString(c.getColumnIndexOrThrow("ruta")));
                a.nombre = n(c.getString(c.getColumnIndexOrThrow("nombre")));
                a.mime = n(c.getString(c.getColumnIndexOrThrow("mime")));
                a.creado = c.getLong(c.getColumnIndexOrThrow("creado"));
                lista.add(a);
            }
        }
        return lista;
    }

    public Modelo.Adjunto adjunto(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM adjuntos WHERE id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) {
                Modelo.Adjunto a = new Modelo.Adjunto();
                a.id = c.getLong(c.getColumnIndexOrThrow("id"));
                a.entidad = n(c.getString(c.getColumnIndexOrThrow("entidad")));
                a.entidadId = c.getLong(c.getColumnIndexOrThrow("entidad_id"));
                a.ruta = n(c.getString(c.getColumnIndexOrThrow("ruta")));
                a.nombre = n(c.getString(c.getColumnIndexOrThrow("nombre")));
                a.mime = n(c.getString(c.getColumnIndexOrThrow("mime")));
                a.creado = c.getLong(c.getColumnIndexOrThrow("creado"));
                return a;
            }
        }
        return null;
    }

    public void eliminarAdjunto(long id) {
        getWritableDatabase().delete("adjuntos", "id=?", new String[]{String.valueOf(id)});
    }

    // --------------------------------------------------------------- informes

    public long suma(String sql, String... args) {
        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            if (c.moveToFirst()) return c.getLong(0);
        }
        return 0;
    }

    public long contar(String sql, String... args) {
        return suma(sql, args);
    }

    public Modelo.Resumen resumen(long desde, long hasta) {
        Modelo.Resumen r = new Modelo.Resumen();
        String d = String.valueOf(desde);
        String h = String.valueOf(hasta);
        r.ventas = (int) contar("SELECT COUNT(*) FROM ventas WHERE anulada=0 AND fecha>=? AND fecha<?", d, h);
        r.ventasTotal = suma("SELECT COALESCE(SUM(total),0) FROM ventas WHERE anulada=0 AND fecha>=? AND fecha<?", d, h);
        r.ventasSubtotal = suma("SELECT COALESCE(SUM(subtotal),0) FROM ventas WHERE anulada=0 AND fecha>=? AND fecha<?", d, h);
        r.descuentos = suma("SELECT COALESCE(SUM(descuento),0) FROM ventas WHERE anulada=0 AND fecha>=? AND fecha<?", d, h);
        r.impuestoVentas = suma("SELECT COALESCE(SUM(impuesto),0) FROM ventas WHERE anulada=0 AND fecha>=? AND fecha<?", d, h);
        r.costoVentas = suma("SELECT COALESCE(SUM(costo),0) FROM ventas WHERE anulada=0 AND fecha>=? AND fecha<?", d, h);
        r.comprasTotal = suma("SELECT COALESCE(SUM(total),0) FROM compras WHERE anulada=0 AND fecha>=? AND fecha<?", d, h);
        r.impuestoCompras = suma("SELECT COALESCE(SUM(impuesto),0) FROM compras WHERE anulada=0 AND fecha>=? AND fecha<?", d, h);
        r.gastosTotal = suma("SELECT COALESCE(SUM(monto),0) FROM gastos WHERE fecha>=? AND fecha<?", d, h);
        r.utilidadBruta = r.ventasTotal - r.impuestoVentas - r.costoVentas;
        r.utilidadNeta = r.utilidadBruta - r.gastosTotal;
        r.porCobrar = porCobrar();
        long hoy = Fechas.inicioDia(Fechas.ahora());
        long enSiete = hoy + 8L * 24L * 60L * 60L * 1000L;
        for (Modelo.Deuda deuda : deudas()) {
            r.deudasAbiertas++;
            if (deuda.fechaLimite > 0 && deuda.fechaLimite < hoy) {
                r.deudasVencidas++;
                r.deudaVencida += deuda.saldo;
            } else if (deuda.fechaLimite > 0 && deuda.fechaLimite < enSiete) {
                r.deudaPorVencer += deuda.saldo;
            }
        }
        return r;
    }

    public List<Modelo.TopProducto> topProductos(long desde, long hasta, int limite) {
        List<Modelo.TopProducto> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT vi.descripcion, SUM(vi.cantidad) AS cantidad, SUM(vi.subtotal) AS total, " +
                        "SUM(vi.costo_unitario * vi.cantidad) AS costo " +
                        "FROM venta_items vi JOIN ventas v ON v.id=vi.venta_id " +
                        "WHERE v.anulada=0 AND v.fecha>=? AND v.fecha<? " +
                        "GROUP BY vi.descripcion ORDER BY total DESC LIMIT ?",
                new String[]{String.valueOf(desde), String.valueOf(hasta), String.valueOf(limite)})) {
            while (c.moveToNext()) {
                Modelo.TopProducto t = new Modelo.TopProducto();
                t.descripcion = n(c.getString(0));
                t.cantidad = c.getLong(1);
                t.total = c.getLong(2);
                t.costo = c.getLong(3);
                t.utilidad = t.total - t.costo;
                lista.add(t);
            }
        }
        return lista;
    }

    public List<Modelo.ProductoVendido> productosVendidos(long desde, long hasta) {
        List<Modelo.ProductoVendido> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT vi.producto_id, SUM(vi.cantidad), SUM(vi.subtotal), " +
                        "SUM(vi.costo_unitario * vi.cantidad) " +
                        "FROM venta_items vi JOIN ventas v ON v.id=vi.venta_id " +
                        "WHERE v.anulada=0 AND v.fecha>=? AND v.fecha<? AND vi.producto_id IS NOT NULL " +
                        "GROUP BY vi.producto_id",
                new String[]{String.valueOf(desde), String.valueOf(hasta)})) {
            while (c.moveToNext()) {
                Modelo.ProductoVendido p = new Modelo.ProductoVendido();
                p.productoId = c.getLong(0);
                p.cantidad = c.getLong(1);
                p.total = c.getLong(2);
                p.costo = c.getLong(3);
                lista.add(p);
            }
        }
        return lista;
    }

    public List<Modelo.TotalFormaPago> formasPago(long desde, long hasta) {
        List<Modelo.TotalFormaPago> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(NULLIF(forma_pago,''),'Sin dato') AS fp, COUNT(*), SUM(total) " +
                        "FROM ventas WHERE anulada=0 AND fecha>=? AND fecha<? " +
                        "GROUP BY fp ORDER BY 3 DESC",
                new String[]{String.valueOf(desde), String.valueOf(hasta)})) {
            while (c.moveToNext()) {
                Modelo.TotalFormaPago t = new Modelo.TotalFormaPago();
                t.formaPago = n(c.getString(0));
                t.cantidad = c.getInt(1);
                t.total = c.getLong(2);
                lista.add(t);
            }
        }
        return lista;
    }

    // ------------------------------------------------------------ utilidades

    public void borrarTodo() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("adjuntos", null, null);
            db.delete("venta_items", null, null);
            db.delete("compra_items", null, null);
            db.delete("pagos", null, null);
            db.delete("ventas", null, null);
            db.delete("compras", null, null);
            db.delete("gastos", null, null);
            db.delete("productos", null, null);
            db.delete("clientes", null, null);
            db.delete("proveedores", null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        /* VACUUM no puede ir dentro de la transacción: deja el archivo sin las
           páginas de los datos borrados. */
        try {
            db.execSQL("VACUUM");
        } catch (Exception ignored) {
        }
    }

    /** Datos de demostración para conocer la app. No se cargan solos. */
    public void cargarEjemplo() {
        long ahora = Fechas.ahora();
        Modelo.Producto aretes = producto("Aretes de oro 18k", "Joyería", "ARE-001", 180000, 110000, 4, 1);
        Modelo.Producto pulsera = producto("Pulsera tejida", "Joyería", "PUL-002", 45000, 22000, 8, 2);
        Modelo.Producto lapices = producto("Caja de lápices x12", "Papelería", "LAP-012", 12500, 7000, 15, 5);
        Modelo.Producto cuaderno = producto("Cuaderno universitario", "Papelería", "CUA-100", 8500, 4800, 20, 6);
        Modelo.Producto cafe = producto("Café molido 500 g", "Alimentos", "CAF-500", 22000, 14000, 6, 3);
        Modelo.Producto bolsa = producto("Bolsa ecológica", "Accesorios", "BOL-001", 6000, 2500, 30, 10);

        Modelo.Cliente c1 = cliente("María Fernanda Gómez", "310 555 2211", "maria@correo.com",
                "Calle 45 #12-30, Bogotá", "Prefiere pago con Nequi.");
        c1.cupo = 200000;
        guardarCliente(c1);
        Modelo.Cliente c2 = cliente("Carlos Andrés Pérez", "320 444 8899", "",
                "Carrera 8 #20-15, Soacha", "Cliente frecuente. Paga por transferencia.");
        c2.cupo = 300000;
        guardarCliente(c2);
        Modelo.Cliente c3 = cliente("Luisa Ramírez", "315 777 1234", "",
                "Diagonal 12 #5-40, Chía", "Cliente nueva.");

        Modelo.Proveedor pr1 = proveedor("Distribuidora El Progreso", "Jorge Ruiz", "601 555 0101",
                "ventas@elprogreso.co", "Bogotá", "Entrega los martes.");
        Modelo.Proveedor pr2 = proveedor("Papelería Central", "Ana Torres", "601 555 0202",
                "", "Bogotá", "");

        Modelo.Venta v1 = new Modelo.Venta();
        v1.fecha = ahora - 3L * 60L * 60L * 1000L;
        v1.clienteId = c1.id;
        v1.formaPago = "Nequi / Daviplata";
        v1.estado = "pagada";
        v1.impuestoPct = 19;
        List<Modelo.Item> i1 = new ArrayList<>();
        i1.add(item(aretes, 1, 180000));
        i1.add(item(bolsa, 1, 6000));
        calcularVenta(v1, i1);
        guardarVenta(v1, i1, null);

        Modelo.Venta v2 = new Modelo.Venta();
        v2.fecha = ahora - 26L * 60L * 60L * 1000L;
        v2.clienteId = c2.id;
        v2.formaPago = "Crédito";
        v2.estado = "credito";
        v2.fechaLimite = ahora - 3L * 24L * 60L * 60L * 1000L;
        v2.impuestoPct = 19;
        List<Modelo.Item> i2 = new ArrayList<>();
        i2.add(item(pulsera, 2, 45000));
        i2.add(item(lapices, 2, 12500));
        calcularVenta(v2, i2);
        guardarVenta(v2, i2, null);

        Modelo.Venta v3 = new Modelo.Venta();
        v3.fecha = ahora - 2L * 24L * 60L * 60L * 1000L;
        v3.clienteId = 0;
        v3.formaPago = "Efectivo";
        v3.estado = "pagada";
        v3.impuestoPct = 0;
        List<Modelo.Item> i3 = new ArrayList<>();
        i3.add(item(cuaderno, 3, 8500));
        i3.add(item(cafe, 1, 22000));
        calcularVenta(v3, i3);
        guardarVenta(v3, i3, null);

        Modelo.Pago pago = new Modelo.Pago();
        pago.clienteId = c2.id;
        pago.fecha = ahora - 20L * 60L * 60L * 1000L;
        pago.monto = 100000;
        pago.formaPago = "Transferencia";
        pago.notas = "Abono a la venta a crédito.";
        registrarPago(pago, null);

        Modelo.Compra compra = new Modelo.Compra();
        compra.fecha = ahora - 4L * 24L * 60L * 60L * 1000L;
        compra.proveedorId = pr2.id;
        compra.formaPago = "Transferencia";
        compra.impuestoPct = 19;
        List<Modelo.Item> ic = new ArrayList<>();
        ic.add(itemCompra(lapices, 12, 6800));
        ic.add(itemCompra(cuaderno, 10, 4600));
        calcularCompra(compra, ic);
        guardarCompra(compra, ic, null);

        Modelo.Gasto g = new Modelo.Gasto();
        g.fecha = ahora - 5L * 24L * 60L * 60L * 1000L;
        g.categoria = "Arriendo";
        g.descripcion = "Arriendo del local";
        g.monto = 350000;
        g.formaPago = "Transferencia";
        guardarGasto(g);
    }

    private Modelo.Producto producto(String nombre, String categoria, String codigo,
                                     long precio, long costo, int stock, int minimo) {
        Modelo.Producto p = new Modelo.Producto();
        p.nombre = nombre;
        p.categoria = categoria;
        p.codigo = codigo;
        p.precio = precio;
        p.costo = costo;
        p.stock = stock;
        p.stockMinimo = minimo;
        guardarProducto(p);
        return p;
    }

    private Modelo.Cliente cliente(String nombre, String telefono, String correo,
                                   String direccion, String notas) {
        Modelo.Cliente c = new Modelo.Cliente();
        c.nombre = nombre;
        c.telefono = telefono;
        c.correo = correo;
        c.direccion = direccion;
        c.notas = notas;
        guardarCliente(c);
        return c;
    }

    private Modelo.Proveedor proveedor(String nombre, String contacto, String telefono,
                                       String correo, String direccion, String notas) {
        Modelo.Proveedor p = new Modelo.Proveedor();
        p.nombre = nombre;
        p.contacto = contacto;
        p.telefono = telefono;
        p.correo = correo;
        p.direccion = direccion;
        p.notas = notas;
        guardarProveedor(p);
        return p;
    }

    private Modelo.Item item(Modelo.Producto p, int cantidad, long precio) {
        Modelo.Item i = new Modelo.Item();
        i.productoId = p.id;
        i.descripcion = p.nombre;
        i.cantidad = cantidad;
        i.precioUnitario = precio;
        i.costoUnitario = p.costo;
        i.subtotal = precio * cantidad;
        return i;
    }

    private Modelo.Item itemCompra(Modelo.Producto p, int cantidad, long costo) {
        Modelo.Item i = new Modelo.Item();
        i.productoId = p.id;
        i.descripcion = p.nombre;
        i.cantidad = cantidad;
        i.costoUnitario = costo;
        i.subtotal = costo * cantidad;
        return i;
    }

    public static void calcularVenta(Modelo.Venta v, List<Modelo.Item> items) {
        long subtotal = 0;
        long costo = 0;
        for (Modelo.Item i : items) {
            subtotal += i.precioUnitario * i.cantidad;
            costo += i.costoUnitario * i.cantidad;
            i.subtotal = i.precioUnitario * i.cantidad;
        }
        v.subtotal = subtotal;
        v.costo = costo;
        long base = Math.max(0, subtotal - v.descuento);
        v.impuesto = Math.round(base * v.impuestoPct / 100.0);
        v.total = base + v.impuesto;
    }

    public static void calcularCompra(Modelo.Compra c, List<Modelo.Item> items) {
        long subtotal = 0;
        for (Modelo.Item i : items) {
            subtotal += i.costoUnitario * i.cantidad;
            i.subtotal = i.costoUnitario * i.cantidad;
        }
        c.subtotal = subtotal;
        c.impuesto = Math.round(subtotal * c.impuestoPct / 100.0);
        c.total = subtotal + c.impuesto;
    }
}
