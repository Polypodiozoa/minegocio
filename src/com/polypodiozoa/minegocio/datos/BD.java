package com.polypodiozoa.minegocio.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;

import com.polypodiozoa.minegocio.modelo.Modelo;
import com.polypodiozoa.minegocio.util.Fechas;
import com.polypodiozoa.minegocio.util.Formatos;
import com.polypodiozoa.minegocio.util.Impuestos;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Base de datos local SQLite. Todo queda en el dispositivo: la app no pide
 * permiso de internet ni envía información a ningún servidor.
 */
public class BD extends SQLiteOpenHelper {

    public static final String ARCHIVO = "minegocio.db";
    private static final int VERSION = 5;
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

    /** Se enciende si SQLite detecta que la base está dañada. */
    private static volatile boolean danada;

    private BD(Context contexto) {
        super(contexto, ARCHIVO, null, VERSION, new DatabaseErrorHandler() {
            @Override
            public void onCorruption(SQLiteDatabase dbObj) {
                /* El manejador por defecto BORRA la base. Aquí solo se anota: la
                   app la aparta y avisa, para que el negocio pueda restaurar una
                   copia en vez de descubrir que perdió todo. */
                danada = true;
                try {
                    dbObj.close();
                } catch (Exception ignored) {
                }
            }
        });
    }

    /** ¿La base se puede abrir y está íntegra? Nunca borra nada. */
    public boolean sana() {
        danada = false;
        try {
            SQLiteDatabase db = getWritableDatabase();
            try (Cursor c = db.rawQuery("PRAGMA quick_check", null)) {
                if (c.moveToFirst() && !"ok".equalsIgnoreCase(c.getString(0))) return false;
            }
            return !danada;
        } catch (SQLiteDatabaseCorruptException e) {
            return false;
        } catch (Throwable e) {
            /* Otro problema (bloqueo, disco lleno): no se toca la base. */
            return !danada;
        }
    }

    /** Aparta una base dañada en vez de borrarla, para poder rescatarla después. */
    public static void apartarDanada(Context contexto) {
        reiniciar();
        File base = archivo(contexto);
        File destino = new File(contexto.getFilesDir(),
                "base-danada-" + Fechas.formato("yyyyMMdd-HHmm", Fechas.ahora()) + ".db");
        if (base.exists()) {
            //noinspection ResultOfMethodCallIgnored
            base.renameTo(destino);
        }
        for (String sufijo : new String[]{"-wal", "-shm", "-journal"}) {
            //noinspection ResultOfMethodCallIgnored
            new File(base.getPath() + sufijo).delete();
        }
        reiniciar();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ajustes (clave TEXT PRIMARY KEY, valor TEXT NOT NULL)");
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "rol TEXT NOT NULL DEFAULT 'empleado'," +
                "menu TEXT," +
                "permisos TEXT NOT NULL DEFAULT ''," +
                "activo INTEGER NOT NULL DEFAULT 1," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE cajas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario_id INTEGER NOT NULL DEFAULT 0," +
                "apertura INTEGER NOT NULL," +
                "cierre INTEGER NOT NULL DEFAULT 0," +
                "monto_inicial INTEGER NOT NULL DEFAULT 0," +
                "monto_contado INTEGER NOT NULL DEFAULT 0," +
                "diferencia INTEGER NOT NULL DEFAULT 0," +
                "notas TEXT," +
                "estado TEXT NOT NULL DEFAULT 'abierta'," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_cajas_estado ON cajas(estado, id)");
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
                "impuesto_pct REAL NOT NULL DEFAULT -1," +
                "vence INTEGER NOT NULL DEFAULT 0," +
                "dias_aviso INTEGER NOT NULL DEFAULT -1," +
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
                "usuario_id INTEGER NOT NULL DEFAULT 0," +
                "entrega INTEGER NOT NULL DEFAULT 1," +
                "retroactiva INTEGER NOT NULL DEFAULT 0," +
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
                "costo_total INTEGER NOT NULL DEFAULT 0," +
                "impuesto_pct REAL NOT NULL DEFAULT -1," +
                "impuesto INTEGER NOT NULL DEFAULT 0," +
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
                "usuario_id INTEGER NOT NULL DEFAULT 0," +
                "entrega INTEGER NOT NULL DEFAULT 1," +
                "fecha_entrega INTEGER NOT NULL DEFAULT 0," +
                "anulada INTEGER NOT NULL DEFAULT 0," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE compra_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "compra_id INTEGER NOT NULL REFERENCES compras(id) ON DELETE CASCADE," +
                "producto_id INTEGER REFERENCES productos(id)," +
                "descripcion TEXT NOT NULL," +
                "cantidad INTEGER NOT NULL DEFAULT 1," +
                "costo_unitario INTEGER NOT NULL DEFAULT 0," +
                "vencimiento INTEGER NOT NULL DEFAULT 0," +
                "subtotal INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE pagos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cliente_id INTEGER NOT NULL REFERENCES clientes(id)," +
                "fecha INTEGER NOT NULL," +
                "monto INTEGER NOT NULL," +
                "forma_pago TEXT," +
                "notas TEXT," +
                "usuario_id INTEGER NOT NULL DEFAULT 0," +
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
                "usuario_id INTEGER NOT NULL DEFAULT 0," +
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

        crearTablasNuevas(db);

        long usuario = db.insert("usuarios", null, valoresUsuario("Administrador", "admin", ""));
        ContentValues v = new ContentValues();
        v.put("clave", "usuario_actual_id");
        v.put("valor", String.valueOf(usuario));
        db.insert("ajustes", null, v);

        ajustesPorDefecto(db);
    }

    /** Ajustes con los que nace una base nueva. «Borrar todo» vuelve exactamente a estos. */
    private static void ajustesPorDefecto(SQLiteDatabase db) {
        ponerAjuste(db, "impuesto_pct", "19");
        ponerAjuste(db, "impuesto_activado", "1");
        ponerAjuste(db, "permitir_stock_negativo", "0");
        ponerAjuste(db, "ultima_copia", "0");
        /* Comportamiento de entrega (decide el negocio en Ajustes):
           una compra pendiente NO suma stock; una venta por entregar SÍ lo descuenta. */
        ponerAjuste(db, "entrega_compra_suma_stock", "0");
        ponerAjuste(db, "entrega_venta_descuenta_stock", "1");
        ponerAjuste(db, "mostrar_saldo_favor", "0");
        ponerAjuste(db, "kpi_empleados", "0");
        ponerAjuste(db, "retro_dias", "1");
        ponerAjuste(db, "aviso_vencimiento_dias", "30");
        ponerAjuste(db, "categorias_vencen", "Alimentos,Bebidas,Droguería");
    }

    /** Tablas y índices que llegaron con el esquema 5. También las usa onUpgrade. */
    private static void crearTablasNuevas(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE producto_lotes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "producto_id INTEGER NOT NULL REFERENCES productos(id) ON DELETE CASCADE," +
                "compra_id INTEGER REFERENCES compras(id)," +
                "costo_unitario INTEGER NOT NULL DEFAULT 0," +
                "cantidad_inicial INTEGER NOT NULL DEFAULT 0," +
                "cantidad_restante INTEGER NOT NULL DEFAULT 0," +
                "vencimiento INTEGER NOT NULL DEFAULT 0," +
                "recibido INTEGER NOT NULL DEFAULT 1," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_lotes_producto ON producto_lotes(producto_id, vencimiento, id)");
        db.execSQL("CREATE INDEX idx_lotes_compra ON producto_lotes(compra_id)");
        db.execSQL("CREATE TABLE venta_item_lotes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "item_id INTEGER NOT NULL REFERENCES venta_items(id) ON DELETE CASCADE," +
                "lote_id INTEGER NOT NULL REFERENCES producto_lotes(id)," +
                "cantidad INTEGER NOT NULL DEFAULT 0," +
                "costo_unitario INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX idx_item_lotes_item ON venta_item_lotes(item_id)");
        db.execSQL("CREATE TABLE cotizaciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "numero TEXT NOT NULL," +
                "fecha INTEGER NOT NULL," +
                "cliente_id INTEGER REFERENCES clientes(id)," +
                "cliente_nombre TEXT," +
                "subtotal INTEGER NOT NULL DEFAULT 0," +
                "descuento INTEGER NOT NULL DEFAULT 0," +
                "impuesto_pct REAL NOT NULL DEFAULT 0," +
                "impuesto INTEGER NOT NULL DEFAULT 0," +
                "total INTEGER NOT NULL DEFAULT 0," +
                "valida_hasta INTEGER NOT NULL DEFAULT 0," +
                "notas TEXT," +
                "estado TEXT NOT NULL DEFAULT 'abierta'," +
                "venta_id INTEGER NOT NULL DEFAULT 0," +
                "usuario_id INTEGER NOT NULL DEFAULT 0," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE cotizacion_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cotizacion_id INTEGER NOT NULL REFERENCES cotizaciones(id) ON DELETE CASCADE," +
                "producto_id INTEGER REFERENCES productos(id)," +
                "descripcion TEXT NOT NULL," +
                "cantidad INTEGER NOT NULL DEFAULT 1," +
                "precio_unitario INTEGER NOT NULL DEFAULT 0," +
                "impuesto_pct REAL NOT NULL DEFAULT -1," +
                "impuesto INTEGER NOT NULL DEFAULT 0," +
                "subtotal INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX idx_items_cotizacion ON cotizacion_items(cotizacion_id)");
        db.execSQL("CREATE TABLE agenda (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tipo TEXT NOT NULL," +
                "entidad_id INTEGER NOT NULL," +
                "fecha INTEGER NOT NULL," +
                "titulo TEXT," +
                "notas TEXT," +
                "aviso_dias INTEGER NOT NULL DEFAULT 0," +
                "recurrente TEXT NOT NULL DEFAULT ''," +
                "hecho INTEGER NOT NULL DEFAULT 0," +
                "creado INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_agenda_fecha ON agenda(fecha)");
        db.execSQL("CREATE INDEX idx_agenda_entidad ON agenda(tipo, entidad_id)");
    }

    /** Rellena lo que necesita el esquema 5 sobre datos que ya existen. */
    private static void migrarEsquema5(SQLiteDatabase db) {
        for (String sql : new String[]{
                "ALTER TABLE usuarios ADD COLUMN permisos TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE productos ADD COLUMN impuesto_pct REAL NOT NULL DEFAULT -1",
                "ALTER TABLE productos ADD COLUMN vence INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE productos ADD COLUMN dias_aviso INTEGER NOT NULL DEFAULT -1",
                "ALTER TABLE ventas ADD COLUMN entrega INTEGER NOT NULL DEFAULT 1",
                "ALTER TABLE ventas ADD COLUMN retroactiva INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE compras ADD COLUMN entrega INTEGER NOT NULL DEFAULT 1",
                "ALTER TABLE compras ADD COLUMN fecha_entrega INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE venta_items ADD COLUMN costo_total INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE venta_items ADD COLUMN impuesto_pct REAL NOT NULL DEFAULT -1",
                "ALTER TABLE venta_items ADD COLUMN impuesto INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE compra_items ADD COLUMN vencimiento INTEGER NOT NULL DEFAULT 0"
        }) {
            db.execSQL(sql);
        }
        crearTablasNuevas(db);
        /* El costo real de las ventas anteriores se conserva tal como estaba. */
        db.execSQL("UPDATE venta_items SET costo_total = costo_unitario * cantidad " +
                "WHERE costo_total = 0");
        /* Cada producto con existencia arranca con un lote «Inicial» a su costo actual.
           Así el stock sigue cuadrando y el historial de precios empieza desde hoy. */
        db.execSQL("INSERT INTO producto_lotes " +
                "(producto_id, compra_id, costo_unitario, cantidad_inicial, cantidad_restante, " +
                " vencimiento, recibido, creado) " +
                "SELECT id, NULL, costo, stock, stock, 0, 1, creado FROM productos WHERE stock > 0");
        /* Un producto que se agotó no necesita lote, pero deja su costo como estaba. */
        for (String clave : new String[]{"entrega_compra_suma_stock", "entrega_venta_descuenta_stock",
                "mostrar_saldo_favor", "kpi_empleados", "retro_dias", "aviso_vencimiento_dias",
                "categorias_vencen"}) {
            ContentValues v = new ContentValues();
            v.put("clave", clave);
            v.put("valor", ajustePorDefecto(clave));
            db.insertWithOnConflict("ajustes", null, v, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private static String ajustePorDefecto(String clave) {
        switch (clave) {
            case "entrega_compra_suma_stock": return "0";
            case "entrega_venta_descuenta_stock": return "1";
            case "mostrar_saldo_favor": return "0";
            case "kpi_empleados": return "0";
            case "retro_dias": return "1";
            case "aviso_vencimiento_dias": return "30";
            case "categorias_vencen": return "Alimentos,Bebidas,Droguería";
            default: return "";
        }
    }

    private static void ponerAjuste(SQLiteDatabase db, String clave, String valor) {
        ContentValues v = new ContentValues();
        v.put("clave", clave);
        v.put("valor", valor);
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
        if (anterior < 3) {
            db.execSQL("CREATE TABLE usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL," +
                    "rol TEXT NOT NULL DEFAULT 'empleado'," +
                    "menu TEXT," +
                    "activo INTEGER NOT NULL DEFAULT 1," +
                    "creado INTEGER NOT NULL)");
            long usuario = db.insert("usuarios", null, valoresUsuario("Administrador", "admin", ""));
            for (String tabla : new String[]{"ventas", "compras", "pagos", "gastos"}) {
                db.execSQL("ALTER TABLE " + tabla + " ADD COLUMN usuario_id INTEGER NOT NULL DEFAULT 0");
                db.execSQL("UPDATE " + tabla + " SET usuario_id=? WHERE usuario_id=0",
                        new Object[]{usuario});
            }
            ContentValues actual = new ContentValues();
            actual.put("clave", "usuario_actual_id");
            actual.put("valor", String.valueOf(usuario));
            db.insertWithOnConflict("ajustes", null, actual, SQLiteDatabase.CONFLICT_IGNORE);
        }
        if (anterior < 4) {
            db.execSQL("CREATE TABLE cajas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "usuario_id INTEGER NOT NULL DEFAULT 0," +
                    "apertura INTEGER NOT NULL," +
                    "cierre INTEGER NOT NULL DEFAULT 0," +
                    "monto_inicial INTEGER NOT NULL DEFAULT 0," +
                    "monto_contado INTEGER NOT NULL DEFAULT 0," +
                    "diferencia INTEGER NOT NULL DEFAULT 0," +
                    "notas TEXT," +
                    "estado TEXT NOT NULL DEFAULT 'abierta'," +
                    "creado INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX idx_cajas_estado ON cajas(estado, id)");
        }
        if (anterior < 5) {
            /* Lotes, entrega, impuesto por producto, cotizaciones, agenda y permisos.
               Los datos que ya existen no se tocan: se les añade lo nuevo con valores
               que reproducen exactamente el comportamiento anterior. */
            migrarEsquema5(db);
        }
    }

    private ContentValues valoresUsuario(String nombre, String rol, String menu) {
        ContentValues v = new ContentValues();
        v.put("nombre", nombre == null ? "" : nombre.trim());
        v.put("rol", "admin".equals(rol) ? "admin" : "empleado");
        v.put("menu", menu == null ? "" : menu);
        v.put("activo", 1);
        v.put("creado", Fechas.ahora());
        return v;
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
        return n.isEmpty() ? "Mi Negocio" : n;
    }

    // --------------------------------------------------------------- usuarios

    public List<Modelo.Usuario> usuarios() {
        List<Modelo.Usuario> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM usuarios WHERE activo=1 ORDER BY nombre COLLATE NOCASE, id", null)) {
            while (c.moveToNext()) lista.add(leerUsuario(c));
        }
        return lista;
    }

    public Modelo.Usuario usuario(long id) {
        if (id <= 0) return null;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM usuarios WHERE id=? AND activo=1", new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return leerUsuario(c);
        }
        return null;
    }

    public Modelo.Usuario usuarioActual() {
        Modelo.Usuario u = usuario(usuarioActualId());
        if (u != null) return u;
        List<Modelo.Usuario> lista = usuarios();
        return lista.isEmpty() ? null : lista.get(0);
    }

    public long usuarioActualId() {
        long id = ajusteNumero("usuario_actual_id", 0);
        if (usuario(id) != null) return id;
        List<Modelo.Usuario> lista = usuarios();
        if (lista.isEmpty()) {
            long nuevo = getWritableDatabase().insert("usuarios", null,
                    valoresUsuario("Administrador", "admin", ""));
            guardarAjuste("usuario_actual_id", nuevo);
            return nuevo;
        }
        long primero = lista.get(0).id;
        guardarAjuste("usuario_actual_id", primero);
        return primero;
    }

    public long guardarUsuario(Modelo.Usuario u) {
        ContentValues v = valoresUsuario(u.nombre, u.rol, u.menu);
        v.put("permisos", u.permisos == null ? "" : u.permisos.trim());
        SQLiteDatabase db = getWritableDatabase();
        if (u.id > 0) {
            db.update("usuarios", v, "id=?", new String[]{String.valueOf(u.id)});
            return u.id;
        }
        u.id = db.insert("usuarios", null, v);
        return u.id;
    }

    /** Desactiva la cuenta sin borrar sus ventas ni compras. */
    public boolean desactivarUsuario(long id) {
        if (id <= 0) return false;
        List<Modelo.Usuario> activos = usuarios();
        if (activos.size() <= 1) return false;
        getWritableDatabase().execSQL("UPDATE usuarios SET activo=0 WHERE id=?",
                new Object[]{id});
        if (usuarioActualId() == id) {
            for (Modelo.Usuario u : activos) {
                if (u.id != id) {
                    guardarAjuste("usuario_actual_id", u.id);
                    break;
                }
            }
        }
        return true;
    }

    public void cambiarUsuario(long id) {
        if (usuario(id) != null) guardarAjuste("usuario_actual_id", id);
    }

    public String menuUsuario(long id) {
        Modelo.Usuario u = usuario(id);
        return u == null || u.menu == null ? "" : u.menu;
    }

    public void guardarMenuUsuario(long id, String menu) {
        ContentValues v = new ContentValues();
        v.put("menu", menu == null ? "" : menu);
        getWritableDatabase().update("usuarios", v, "id=?", new String[]{String.valueOf(id)});
    }

    public long totalVentasUsuario(long id, long desde, long hasta) {
        return suma("SELECT COALESCE(SUM(total),0) FROM ventas WHERE anulada=0 AND usuario_id=? " +
                "AND fecha>=? AND fecha<?", String.valueOf(id), String.valueOf(desde), String.valueOf(hasta));
    }

    public int cantidadVentasUsuario(long id, long desde, long hasta) {
        return (int) suma("SELECT COUNT(*) FROM ventas WHERE anulada=0 AND usuario_id=? " +
                "AND fecha>=? AND fecha<?", String.valueOf(id), String.valueOf(desde), String.valueOf(hasta));
    }

    public long totalGastosUsuario(long id, long desde, long hasta) {
        return suma("SELECT COALESCE(SUM(monto),0) FROM gastos WHERE usuario_id=? " +
                "AND fecha>=? AND fecha<?", String.valueOf(id), String.valueOf(desde), String.valueOf(hasta));
    }

    private Modelo.Usuario leerUsuario(Cursor c) {
        Modelo.Usuario u = new Modelo.Usuario();
        u.id = c.getLong(c.getColumnIndexOrThrow("id"));
        u.nombre = n(c.getString(c.getColumnIndexOrThrow("nombre")));
        u.rol = n(c.getString(c.getColumnIndexOrThrow("rol")));
        int idxMenu = c.getColumnIndex("menu");
        u.menu = idxMenu >= 0 ? n(c.getString(idxMenu)) : "";
        int idxPermisos = c.getColumnIndex("permisos");
        u.permisos = idxPermisos >= 0 ? n(c.getString(idxPermisos)) : "";
        u.activo = c.getInt(c.getColumnIndexOrThrow("activo")) != 0;
        u.creado = c.getLong(c.getColumnIndexOrThrow("creado"));
        return u;
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
        int idxImp = c.getColumnIndex("impuesto_pct");
        p.impuestoPct = idxImp >= 0 ? c.getDouble(idxImp) : -1;
        int idxVence = c.getColumnIndex("vence");
        p.vence = idxVence >= 0 && c.getInt(idxVence) != 0;
        int idxAviso = c.getColumnIndex("dias_aviso");
        p.diasAviso = idxAviso >= 0 ? c.getInt(idxAviso) : -1;
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

    /** Busca un producto activo por el código escaneado. */
    public Modelo.Producto productoPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return null;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM productos WHERE activo=1 AND TRIM(codigo)=? COLLATE NOCASE LIMIT 1",
                new String[]{codigo.trim()})) {
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
        v.put("impuesto_pct", p.impuestoPct < 0 ? -1 : p.impuestoPct);
        v.put("vence", p.vence ? 1 : 0);
        v.put("dias_aviso", p.diasAviso);
        v.put("foto", p.foto == null ? "" : p.foto);
        v.put("activo", p.activo ? 1 : 0);
        if (p.creado == 0) p.creado = Fechas.ahora();
        v.put("creado", p.creado);
        SQLiteDatabase db = getWritableDatabase();
        if (p.id > 0) {
            long anterior = 0;
            try (Cursor c = db.rawQuery("SELECT stock FROM productos WHERE id=?",
                    new String[]{String.valueOf(p.id)})) {
                if (c.moveToFirst()) anterior = c.getLong(0);
            }
            db.update("productos", v, "id=?", new String[]{String.valueOf(p.id)});
            /* Si la ficha cambió la existencia a mano, los lotes se ajustan para
               que el stock siga siendo exactamente la suma de lo que hay. */
            if (p.stock != anterior) {
                ajustarStockConLotes(db, p.id, p.stock, p.costo);
            }
            return p.id;
        }
        p.id = db.insert("productos", null, v);
        if (p.stock > 0) {
            insertarLote(db, p.id, 0, p.costo, p.stock, 0, true, p.creado);
        }
        return p.id;
    }

    /** Crea un lote. Es la única puerta por la que entra mercancía al stock. */
    private void insertarLote(SQLiteDatabase db, long productoId, long compraId, long costo,
                              int cantidad, long vencimiento, boolean recibido, long creado) {
        ContentValues lote = new ContentValues();
        lote.put("producto_id", productoId);
        if (compraId > 0) lote.put("compra_id", compraId);
        lote.put("costo_unitario", Math.max(0, costo));
        lote.put("cantidad_inicial", cantidad);
        lote.put("cantidad_restante", recibido ? cantidad : 0);
        lote.put("vencimiento", vencimiento);
        lote.put("recibido", recibido ? 1 : 0);
        lote.put("creado", creado == 0 ? Fechas.ahora() : creado);
        db.insert("producto_lotes", null, lote);
    }

    /** Lleva el stock a una cifra exacta creando o consumiendo un lote de ajuste. */
    private void ajustarStockConLotes(SQLiteDatabase db, long productoId, int nuevoStock, long costo) {
        long sum = 0;
        try (Cursor c = db.rawQuery(
                "SELECT COALESCE(SUM(cantidad_restante),0) FROM producto_lotes " +
                        "WHERE producto_id=? AND recibido=1", new String[]{String.valueOf(productoId)})) {
            if (c.moveToFirst()) sum = c.getLong(0);
        }
        long delta = nuevoStock - sum;
        if (delta > 0) {
            insertarLote(db, productoId, 0, costo, (int) delta, 0, true, Fechas.ahora());
        } else if (delta < 0) {
            long porQuitar = -delta;
            try (Cursor c = db.rawQuery(
                    "SELECT id, cantidad_restante FROM producto_lotes " +
                            "WHERE producto_id=? AND cantidad_restante>0 AND recibido=1 " +
                            "ORDER BY CASE WHEN vencimiento=0 THEN 1 ELSE 0 END, vencimiento DESC, id DESC",
                    new String[]{String.valueOf(productoId)})) {
                while (c.moveToNext() && porQuitar > 0) {
                    int quitar = (int) Math.min(porQuitar, c.getInt(1));
                    db.execSQL("UPDATE producto_lotes SET cantidad_restante=cantidad_restante-? WHERE id=?",
                            new Object[]{quitar, c.getLong(0)});
                    porQuitar -= quitar;
                }
            }
        }
        db.execSQL("UPDATE productos SET stock=? WHERE id=?",
                new Object[]{nuevoStock, productoId});
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
        db.delete("producto_lotes", "producto_id=?", new String[]{String.valueOf(id)});
        db.delete("productos", "id=?", new String[]{String.valueOf(id)});
        return true;
    }

    public void ajustarStock(long id, int nuevoStock) {
        SQLiteDatabase db = getWritableDatabase();
        Modelo.Producto p = producto(id);
        ajustarStockConLotes(db, id, nuevoStock, p == null ? 0 : p.costo);
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

    /** true mientras no haya nada registrado: es cuando la app ofrece los datos de ejemplo. */
    public boolean sinDatos() {
        return contar("SELECT (SELECT COUNT(*) FROM productos) + (SELECT COUNT(*) FROM ventas)"
                + " + (SELECT COUNT(*) FROM compras) + (SELECT COUNT(*) FROM gastos)"
                + " + (SELECT COUNT(*) FROM clientes) + (SELECT COUNT(*) FROM proveedores)"
                + " + (SELECT COUNT(*) FROM pagos) + (SELECT COUNT(*) FROM cajas)") == 0;
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
        int idxEntrega = c.getColumnIndex("entrega");
        v.entregada = idxEntrega < 0 || c.getInt(idxEntrega) != 0;
        int idxRetro = c.getColumnIndex("retroactiva");
        v.retroactiva = idxRetro >= 0 && c.getInt(idxRetro) != 0;
        int idxUsuario = c.getColumnIndex("usuario_id");
        v.usuarioId = idxUsuario >= 0 ? c.getLong(idxUsuario) : 0;
        int idx = c.getColumnIndex("cliente_nombre");
        v.clienteNombre = idx >= 0 ? n(c.getString(idx)) : "";
        idx = c.getColumnIndex("usuario_nombre");
        v.usuarioNombre = idx >= 0 ? n(c.getString(idx)) : "";
        idx = c.getColumnIndex("num_items");
        v.numItems = idx >= 0 ? c.getInt(idx) : 0;
        return v;
    }

    private static final String SQL_VENTA =
            "SELECT v.*, c.nombre AS cliente_nombre, u.nombre AS usuario_nombre, " +
            "(SELECT COUNT(*) FROM venta_items vi WHERE vi.venta_id=v.id) AS num_items, " +
            "COALESCE((SELECT SUM(a.monto) FROM pago_aplicaciones a WHERE a.venta_id=v.id),0) AS abonado " +
            "FROM ventas v LEFT JOIN clientes c ON c.id=v.cliente_id " +
            "LEFT JOIN usuarios u ON u.id=v.usuario_id ";

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
                int idxCostoTotal = c.getColumnIndex("costo_total");
                i.costoTotal = idxCostoTotal >= 0
                        ? c.getLong(idxCostoTotal) : i.costoUnitario * i.cantidad;
                int idxPct = c.getColumnIndex("impuesto_pct");
                i.impuestoPct = idxPct >= 0 ? c.getDouble(idxPct) : -1;
                int idxImp = c.getColumnIndex("impuesto");
                i.impuesto = idxImp >= 0 ? c.getLong(idxImp) : 0;
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
            boolean descuenta = v.entregada || ventaDescuentaStock();
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
            cv.put("usuario_id", v.usuarioId > 0 ? v.usuarioId : usuarioActualId());
            cv.put("entrega", v.entregada ? 1 : 0);
            cv.put("retroactiva", v.retroactiva ? 1 : 0);
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
                ci.put("impuesto_pct", i.impuestoPct);
                ci.put("impuesto", i.impuesto);
                ci.put("subtotal", i.subtotal);
                ci.put("costo_total", 0);
                ci.put("costo_unitario", i.costoUnitario);
                i.id = db.insert("venta_items", null, ci);
                /* El costo de la línea sale de los lotes que se consumen, no del
                   costo actual del producto: así la utilidad de hoy no cambia
                   cuando el proveedor sube el precio mañana. La traza se anota
                   sobre la línea ya insertada. */
                long costoLinea = i.costoTotal;
                if (i.productoId > 0) {
                    costoLinea = descuenta
                            ? consumirLotes(db, i, true)
                            : costoLotesDisponibles(db, i.productoId, i.cantidad);
                }
                i.costoTotal = costoLinea;
                ContentValues costo = new ContentValues();
                costo.put("costo_total", costoLinea);
                costo.put("costo_unitario", i.cantidad > 0
                        ? Math.round((double) costoLinea / i.cantidad) : i.costoUnitario);
                db.update("venta_items", costo, "id=?", new String[]{String.valueOf(i.id)});
                if (i.productoId > 0 && descuenta) {
                    recalcularStock(db, i.productoId);
                    /* Venta por encima de la existencia: el stock queda negativo,
                       como el negocio lo permitió en Ajustes. */
                    if (i.faltante > 0) {
                        db.execSQL("UPDATE productos SET stock=stock-? WHERE id=?",
                                new Object[]{i.faltante, i.productoId});
                    }
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
                if (i.productoId <= 0) continue;
                boolean devuelto = devolverLotes(db, i.id);
                if (!devuelto) {
                    /* Venta anterior al esquema 5 sin traza de lote: la cantidad
                       vuelve a un lote de ajuste para que el stock siga cuadrando. */
                    Modelo.Producto p = producto(i.productoId);
                    int actual = p == null ? 0 : p.stock;
                    ajustarStockConLotes(db, i.productoId, actual + i.cantidad,
                            p == null ? 0 : p.costo);
                } else {
                    recalcularStock(db, i.productoId);
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

    /** Marca una venta «por entregar» como entregada y descuenta el stock en ese momento. */
    public boolean marcarVentaEntregada(long ventaId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Modelo.Venta v = venta(ventaId);
            if (v == null || v.anulada || v.entregada) {
                db.setTransactionSuccessful();
                return false;
            }
            long costoTotal = 0;
            for (Modelo.Item i : itemsVenta(ventaId)) {
                long costo = i.costoTotal;
                if (i.productoId > 0) {
                    costo = consumirLotes(db, i, true);
                    recalcularStock(db, i.productoId);
                    if (i.faltante > 0) {
                        db.execSQL("UPDATE productos SET stock=stock-? WHERE id=?",
                                new Object[]{i.faltante, i.productoId});
                    }
                }
                costoTotal += costo;
                ContentValues ci = new ContentValues();
                ci.put("costo_total", costo);
                ci.put("costo_unitario", i.cantidad > 0
                        ? Math.round((double) costo / i.cantidad) : i.costoUnitario);
                db.update("venta_items", ci, "id=?", new String[]{String.valueOf(i.id)});
            }
            ContentValues cv = new ContentValues();
            cv.put("entrega", 1);
            cv.put("costo", costoTotal);
            db.update("ventas", cv, "id=?", new String[]{String.valueOf(ventaId)});
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    /** Ventas registradas como «por entregar». */
    public List<Modelo.Venta> ventasPorEntregar() {
        List<Modelo.Venta> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                SQL_VENTA + "WHERE v.anulada=0 AND v.entrega=0 ORDER BY v.fecha, v.id", null)) {
            while (c.moveToNext()) lista.add(leerVenta(c));
        }
        return lista;
    }

    public boolean ventaDescuentaStock() {
        return ajusteSi("entrega_venta_descuenta_stock", true);
    }

    // ------------------------------------------------------------------ lotes

    /**
     * Consume cantidad de los lotes del producto, primero lo que vence antes y,
     * entre iguales, lo más viejo. Devuelve el costo real consumido.
     */
    private long consumirLotes(SQLiteDatabase db, Modelo.Item item, boolean anotar) {
        long productoId = item.productoId;
        int falta = item.cantidad;
        long costo = 0;
        item.faltante = 0;
        try (Cursor c = db.rawQuery(
                "SELECT id, costo_unitario, cantidad_restante FROM producto_lotes " +
                        "WHERE producto_id=? AND cantidad_restante>0 AND recibido=1 " +
                        "ORDER BY CASE WHEN vencimiento=0 THEN 1 ELSE 0 END, vencimiento, id",
                new String[]{String.valueOf(productoId)})) {
            while (c.moveToNext() && falta > 0) {
                long loteId = c.getLong(0);
                long costoUnitario = c.getLong(1);
                int restante = c.getInt(2);
                int tomar = Math.min(falta, restante);
                db.execSQL("UPDATE producto_lotes SET cantidad_restante=cantidad_restante-? WHERE id=?",
                        new Object[]{tomar, loteId});
                costo += costoUnitario * tomar;
                falta -= tomar;
                if (anotar && item.id > 0) {
                    ContentValues cv = new ContentValues();
                    cv.put("item_id", item.id);
                    cv.put("lote_id", loteId);
                    cv.put("cantidad", tomar);
                    cv.put("costo_unitario", costoUnitario);
                    db.insert("venta_item_lotes", null, cv);
                }
            }
        }
        /* Si el lote no alcanza (venta sin stock permitida), el resto se costea
           con el costo actual del producto; nadie queda con costo cero. */
        if (falta > 0) {
            Modelo.Producto p = producto(productoId);
            long costoUnitario = p == null ? 0 : p.costo;
            costo += costoUnitario * falta;
            item.faltante = falta;
        }
        return costo;
    }

    /** Costo estimado sin tocar los lotes (para ventas que aún no descuentan stock). */
    private long costoLotesDisponibles(SQLiteDatabase db, long productoId, int cantidad) {
        int falta = cantidad;
        long costo = 0;
        try (Cursor c = db.rawQuery(
                "SELECT costo_unitario, cantidad_restante FROM producto_lotes " +
                        "WHERE producto_id=? AND cantidad_restante>0 AND recibido=1 " +
                        "ORDER BY CASE WHEN vencimiento=0 THEN 1 ELSE 0 END, vencimiento, id",
                new String[]{String.valueOf(productoId)})) {
            while (c.moveToNext() && falta > 0) {
                int tomar = Math.min(falta, c.getInt(1));
                costo += c.getLong(0) * tomar;
                falta -= tomar;
            }
        }
        if (falta > 0) {
            Modelo.Producto p = producto(productoId);
            costo += (p == null ? 0 : p.costo) * falta;
        }
        return costo;
    }

    /** Devuelve a sus lotes lo que consumió una línea de venta. */
    private boolean devolverLotes(SQLiteDatabase db, long itemId) {
        boolean devolvio = false;
        try (Cursor c = db.rawQuery(
                "SELECT lote_id, cantidad FROM venta_item_lotes WHERE item_id=?",
                new String[]{String.valueOf(itemId)})) {
            while (c.moveToNext()) {
                db.execSQL("UPDATE producto_lotes SET cantidad_restante=cantidad_restante+? WHERE id=?",
                        new Object[]{c.getInt(1), c.getLong(0)});
                devolvio = true;
            }
        }
        if (devolvio) db.delete("venta_item_lotes", "item_id=?", new String[]{String.valueOf(itemId)});
        return devolvio;
    }

    /** El stock del producto es la suma de sus lotes; nadie más lo escribe. */
    private void recalcularStock(SQLiteDatabase db, long productoId) {
        long total = 0;
        try (Cursor c = db.rawQuery(
                "SELECT COALESCE(SUM(cantidad_restante),0), COUNT(*) FROM producto_lotes " +
                        "WHERE producto_id=? AND recibido=1",
                new String[]{String.valueOf(productoId)})) {
            if (!c.moveToFirst() || c.getInt(1) == 0) return; // sin lotes: se respeta el stock anterior
            total = c.getLong(0);
        }
        db.execSQL("UPDATE productos SET stock=? WHERE id=?",
                new Object[]{total, productoId});
    }

    public List<Modelo.Lote> lotes(long productoId) {
        List<Modelo.Lote> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM producto_lotes WHERE producto_id=? AND cantidad_restante>0 " +
                        "ORDER BY CASE WHEN vencimiento=0 THEN 1 ELSE 0 END, vencimiento, id",
                new String[]{String.valueOf(productoId)})) {
            while (c.moveToNext()) {
                Modelo.Lote l = new Modelo.Lote();
                l.id = c.getLong(c.getColumnIndexOrThrow("id"));
                l.productoId = c.getLong(c.getColumnIndexOrThrow("producto_id"));
                l.compraId = c.getLong(c.getColumnIndexOrThrow("compra_id"));
                l.costoUnitario = c.getLong(c.getColumnIndexOrThrow("costo_unitario"));
                l.cantidadInicial = c.getInt(c.getColumnIndexOrThrow("cantidad_inicial"));
                l.restante = c.getInt(c.getColumnIndexOrThrow("cantidad_restante"));
                l.vencimiento = c.getLong(c.getColumnIndexOrThrow("vencimiento"));
                l.recibido = c.getInt(c.getColumnIndexOrThrow("recibido")) != 0;
                l.creado = c.getLong(c.getColumnIndexOrThrow("creado"));
                lista.add(l);
            }
        }
        return lista;
    }

    /** Costo promedio ponderado de lo que hoy queda en los lotes. */
    public long costoPromedio(long productoId) {
        long unidades = 0;
        long valor = 0;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(cantidad_restante),0), " +
                        "COALESCE(SUM(cantidad_restante * costo_unitario),0) " +
                        "FROM producto_lotes WHERE producto_id=? AND cantidad_restante>0 AND recibido=1",
                new String[]{String.valueOf(productoId)})) {
            if (c.moveToFirst()) {
                unidades = c.getLong(0);
                valor = c.getLong(1);
            }
        }
        return unidades > 0 ? Math.round((double) valor / unidades) : 0;
    }

    /** Historial de compras del producto, con la variación frente a la anterior. */
    public List<Modelo.PrecioCompra> historialPrecios(long productoId, int limite) {
        List<Modelo.PrecioCompra> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT c.id, c.fecha, ci.cantidad, ci.costo_unitario, " +
                        "COALESCE(p.nombre,'') AS proveedor, COALESCE(ci.vencimiento,0) " +
                        "FROM compra_items ci JOIN compras c ON c.id=ci.compra_id " +
                        "LEFT JOIN proveedores p ON p.id=c.proveedor_id " +
                        "WHERE ci.producto_id=? AND c.anulada=0 AND ci.costo_unitario>0 " +
                        "ORDER BY c.fecha DESC, ci.id DESC LIMIT ?",
                new String[]{String.valueOf(productoId), String.valueOf(limite)})) {
            long anterior = 0;
            while (c.moveToNext()) {
                Modelo.PrecioCompra pc = new Modelo.PrecioCompra();
                pc.compraId = c.getLong(0);
                pc.fecha = c.getLong(1);
                pc.cantidad = c.getInt(2);
                pc.costoUnitario = c.getLong(3);
                pc.proveedor = n(c.getString(4));
                pc.vencimiento = c.getLong(5);
                pc.variacion = anterior > 0
                        ? (int) Math.round((pc.costoUnitario - anterior) * 100.0 / anterior) : 0;
                anterior = pc.costoUnitario;
                lista.add(pc);
            }
        }
        return lista;
    }

    /** Lotes que vencen dentro de los próximos días (0 = sin límite de días). */
    public List<Modelo.Lote> lotesPorVencer(int dias) {
        List<Modelo.Lote> lista = new ArrayList<>();
        long limite = Fechas.ahora() + Math.max(0, dias) * 24L * 60L * 60L * 1000L;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT l.*, pr.nombre AS producto_nombre FROM producto_lotes l " +
                        "JOIN productos pr ON pr.id=l.producto_id " +
                        "WHERE l.cantidad_restante>0 AND l.recibido=1 AND l.vencimiento>0 " +
                        "AND l.vencimiento<=? AND pr.activo=1 ORDER BY l.vencimiento, pr.nombre",
                new String[]{String.valueOf(limite)})) {
            while (c.moveToNext()) {
                Modelo.Lote l = new Modelo.Lote();
                l.id = c.getLong(c.getColumnIndexOrThrow("id"));
                l.productoId = c.getLong(c.getColumnIndexOrThrow("producto_id"));
                l.costoUnitario = c.getLong(c.getColumnIndexOrThrow("costo_unitario"));
                l.cantidadInicial = c.getInt(c.getColumnIndexOrThrow("cantidad_inicial"));
                l.restante = c.getInt(c.getColumnIndexOrThrow("cantidad_restante"));
                l.vencimiento = c.getLong(c.getColumnIndexOrThrow("vencimiento"));
                lista.add(l);
            }
        }
        return lista;
    }

    public int diasAvisoVencimiento() {
        return (int) ajusteNumero("aviso_vencimiento_dias", 30);
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
        int idxEntrega = c.getColumnIndex("entrega");
        p.recibida = idxEntrega < 0 || c.getInt(idxEntrega) != 0;
        int idxFechaEntrega = c.getColumnIndex("fecha_entrega");
        p.fechaEntrega = idxFechaEntrega >= 0 ? c.getLong(idxFechaEntrega) : 0;
        int idxUsuario = c.getColumnIndex("usuario_id");
        p.usuarioId = idxUsuario >= 0 ? c.getLong(idxUsuario) : 0;
        int idx = c.getColumnIndex("proveedor_nombre");
        p.proveedorNombre = idx >= 0 ? n(c.getString(idx)) : "";
        idx = c.getColumnIndex("usuario_nombre");
        p.usuarioNombre = idx >= 0 ? n(c.getString(idx)) : "";
        idx = c.getColumnIndex("num_items");
        p.numItems = idx >= 0 ? c.getInt(idx) : 0;
        return p;
    }

    private static final String SQL_COMPRA =
            "SELECT c.*, p.nombre AS proveedor_nombre, u.nombre AS usuario_nombre, " +
            "(SELECT COUNT(*) FROM compra_items ci WHERE ci.compra_id=c.id) AS num_items " +
            "FROM compras c LEFT JOIN proveedores p ON p.id=c.proveedor_id " +
            "LEFT JOIN usuarios u ON u.id=c.usuario_id ";

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
                int idxVence = c.getColumnIndex("vencimiento");
                i.vencimiento = idxVence >= 0 ? c.getLong(idxVence) : 0;
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
            cv.put("usuario_id", compra.usuarioId > 0 ? compra.usuarioId : usuarioActualId());
            cv.put("entrega", compra.recibida ? 1 : 0);
            cv.put("fecha_entrega", compra.fechaEntrega);
            cv.put("anulada", 0);
            cv.put("creado", Fechas.ahora());
            compra.id = db.insert("compras", null, cv);
            /* Una compra pendiente no suma existencias salvo que el negocio lo
               haya pedido en Ajustes: la mercancía que no ha llegado no está. */
            boolean entra = compra.recibida || compraSumaStockInmediato();
            for (Modelo.Item i : items) {
                ContentValues ci = new ContentValues();
                ci.put("compra_id", compra.id);
                if (i.productoId > 0) ci.put("producto_id", i.productoId);
                else ci.putNull("producto_id");
                ci.put("descripcion", i.descripcion);
                ci.put("cantidad", i.cantidad);
                ci.put("costo_unitario", i.costoUnitario);
                ci.put("vencimiento", i.vencimiento);
                ci.put("subtotal", i.subtotal);
                db.insert("compra_items", null, ci);
                if (i.productoId > 0) {
                    /* Cada línea de compra es un lote: su costo y su vencimiento
                       quedan guardados para siempre, aunque el precio cambie. */
                    ContentValues lote = new ContentValues();
                    lote.put("producto_id", i.productoId);
                    lote.put("compra_id", compra.id);
                    lote.put("costo_unitario", Math.max(0, i.costoUnitario));
                    lote.put("cantidad_inicial", i.cantidad);
                    lote.put("cantidad_restante", entra ? i.cantidad : 0);
                    lote.put("vencimiento", i.vencimiento);
                    lote.put("recibido", entra ? 1 : 0);
                    lote.put("creado", compra.fecha);
                    db.insert("producto_lotes", null, lote);
                    if (i.costoUnitario > 0) {
                        db.execSQL("UPDATE productos SET costo=? WHERE id=?",
                                new Object[]{i.costoUnitario, i.productoId});
                    }
                    recalcularStock(db, i.productoId);
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
                    /* Lo que quede de los lotes de esta compra se anula; lo ya
                       vendido sigue vendido y conserva su costo histórico. */
                    db.execSQL("UPDATE producto_lotes SET cantidad_restante=0 " +
                            "WHERE compra_id=? AND producto_id=?", new Object[]{id, i.productoId});
                    recalcularStock(db, i.productoId);
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

    /** Marca una compra pendiente como recibida: hasta ahora entra la mercancía al stock. */
    public boolean marcarCompraRecibida(long compraId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Modelo.Compra c = compra(compraId);
            if (c == null || c.anulada || c.recibida) {
                db.setTransactionSuccessful();
                return false;
            }
            db.execSQL("UPDATE producto_lotes SET recibido=1, " +
                            "cantidad_restante=cantidad_inicial " +
                            "WHERE compra_id=? AND cantidad_restante=0",
                    new Object[]{compraId});
            ContentValues cv = new ContentValues();
            cv.put("entrega", 1);
            cv.put("fecha_entrega", Fechas.ahora());
            db.update("compras", cv, "id=?", new String[]{String.valueOf(compraId)});
            for (Modelo.Item i : itemsCompra(compraId)) {
                if (i.productoId > 0) recalcularStock(db, i.productoId);
            }
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<Modelo.Compra> comprasPorRecibir() {
        List<Modelo.Compra> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                SQL_COMPRA + "WHERE c.anulada=0 AND c.entrega=0 ORDER BY c.fecha, c.id", null)) {
            while (c.moveToNext()) lista.add(leerCompra(c));
        }
        return lista;
    }

    public boolean compraSumaStockInmediato() {
        return ajusteSi("entrega_compra_suma_stock", false);
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
            cv.put("usuario_id", p.usuarioId > 0 ? p.usuarioId : usuarioActualId());
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
                "SELECT p.*, c.nombre AS cliente_nombre, u.nombre AS usuario_nombre " +
                        "FROM pagos p LEFT JOIN clientes c ON c.id=p.cliente_id " +
                        "LEFT JOIN usuarios u ON u.id=p.usuario_id " +
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
                int idxUsuario = c.getColumnIndex("usuario_id");
                p.usuarioId = idxUsuario >= 0 ? c.getLong(idxUsuario) : 0;
                int idxNombre = c.getColumnIndex("usuario_nombre");
                p.usuarioNombre = idxNombre >= 0 ? n(c.getString(idxNombre)) : "";
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
        int idxUsuario = c.getColumnIndex("usuario_id");
        g.usuarioId = idxUsuario >= 0 ? c.getLong(idxUsuario) : 0;
        int idxNombre = c.getColumnIndex("usuario_nombre");
        g.usuarioNombre = idxNombre >= 0 ? n(c.getString(idxNombre)) : "";
        return g;
    }

    public List<Modelo.Gasto> gastos(String filtro) {
        List<Modelo.Gasto> lista = new ArrayList<>();
        String f = filtro == null ? "" : filtro.trim().replace("%", " ").replace("_", " ");
        String sql = "SELECT g.*, u.nombre AS usuario_nombre FROM gastos g " +
                "LEFT JOIN usuarios u ON u.id=g.usuario_id ";
        List<String> args = new ArrayList<>();
        if (!f.isEmpty()) {
            sql += "WHERE g.descripcion LIKE ? OR g.categoria LIKE ? ";
            String patron = "%" + f + "%";
            args.add(patron);
            args.add(patron);
        }
        sql += "ORDER BY g.fecha DESC, g.id DESC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            while (c.moveToNext()) lista.add(leerGasto(c));
        }
        return lista;
    }

    public Modelo.Gasto gasto(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT g.*, u.nombre AS usuario_nombre FROM gastos g " +
                        "LEFT JOIN usuarios u ON u.id=g.usuario_id WHERE g.id=?",
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
        v.put("usuario_id", g.usuarioId > 0 ? g.usuarioId : usuarioActualId());
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


    // ---------------------------------------------------------------- caja

    public Modelo.Caja cajaAbierta() {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT c.id, c.usuario_id, COALESCE(u.nombre,''), c.apertura, c.cierre, " +
                        "c.monto_inicial, c.monto_contado, c.diferencia, COALESCE(c.notas,''), " +
                        "c.estado FROM cajas c LEFT JOIN usuarios u ON u.id=c.usuario_id " +
                        "WHERE c.estado='abierta' ORDER BY c.id DESC LIMIT 1", null)) {
            if (c.moveToFirst()) return leerCaja(c);
        }
        return null;
    }

    public List<Modelo.Caja> cajasCerradas(int limite) {
        List<Modelo.Caja> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT c.id, c.usuario_id, COALESCE(u.nombre,''), c.apertura, c.cierre, " +
                        "c.monto_inicial, c.monto_contado, c.diferencia, COALESCE(c.notas,''), " +
                        "c.estado FROM cajas c LEFT JOIN usuarios u ON u.id=c.usuario_id " +
                        "WHERE c.estado='cerrada' ORDER BY c.cierre DESC, c.id DESC LIMIT ?",
                new String[]{String.valueOf(Math.max(1, limite))})) {
            while (c.moveToNext()) lista.add(leerCaja(c));
        }
        return lista;
    }

    public Modelo.Caja caja(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT c.id, c.usuario_id, COALESCE(u.nombre,''), c.apertura, c.cierre, " +
                        "c.monto_inicial, c.monto_contado, c.diferencia, COALESCE(c.notas,''), " +
                        "c.estado FROM cajas c LEFT JOIN usuarios u ON u.id=c.usuario_id " +
                        "WHERE c.id=?", new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return leerCaja(c);
        }
        return null;
    }

    public boolean abrirCaja(long montoInicial, long usuarioId) {
        if (cajaAbierta() != null) return false;
        ContentValues v = new ContentValues();
        v.put("usuario_id", usuarioId);
        v.put("apertura", Fechas.ahora());
        v.put("monto_inicial", Math.max(0, montoInicial));
        v.put("estado", "abierta");
        v.put("creado", Fechas.ahora());
        return getWritableDatabase().insert("cajas", null, v) > 0;
    }

    public boolean cerrarCaja(long id, long montoContado, String notas) {
        Modelo.Caja c = caja(id);
        if (c == null || !"abierta".equals(c.estado)) return false;
        long ahora = Fechas.ahora();
        long esperado = resumenCaja(c.apertura, ahora, c.montoInicial).efectivoEsperado;
        ContentValues v = new ContentValues();
        v.put("cierre", ahora);
        v.put("monto_contado", Math.max(0, montoContado));
        v.put("diferencia", montoContado - esperado);
        v.put("notas", notas == null ? "" : notas);
        v.put("estado", "cerrada");
        return getWritableDatabase().update("cajas", v, "id=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public Modelo.ResumenCaja resumenCaja(long desde, long hasta, long montoInicial) {
        Modelo.ResumenCaja r = new Modelo.ResumenCaja();
        String d = String.valueOf(desde);
        String h = String.valueOf(hasta);
        r.ventasCantidad = (int) contar("SELECT COUNT(*) FROM ventas WHERE anulada=0 " +
                "AND fecha>=? AND fecha<=?", d, h);
        r.ventasTotal = suma("SELECT COALESCE(SUM(total),0) FROM ventas WHERE anulada=0 " +
                "AND fecha>=? AND fecha<=?", d, h);
        r.ventasEfectivo = suma("SELECT COALESCE(SUM(total),0) FROM ventas WHERE anulada=0 " +
                "AND estado<>'credito' AND forma_pago='Efectivo' AND fecha>=? AND fecha<=?", d, h);
        r.pagosTotal = suma("SELECT COALESCE(SUM(monto),0) FROM pagos WHERE fecha>=? AND fecha<=?", d, h);
        r.pagosEfectivo = suma("SELECT COALESCE(SUM(monto),0) FROM pagos " +
                "WHERE forma_pago='Efectivo' AND fecha>=? AND fecha<=?", d, h);
        r.comprasTotal = suma("SELECT COALESCE(SUM(total),0) FROM compras WHERE anulada=0 " +
                "AND fecha>=? AND fecha<=?", d, h);
        r.comprasEfectivo = suma("SELECT COALESCE(SUM(total),0) FROM compras WHERE anulada=0 " +
                "AND forma_pago='Efectivo' AND fecha>=? AND fecha<=?", d, h);
        r.gastosTotal = suma("SELECT COALESCE(SUM(monto),0) FROM gastos WHERE fecha>=? AND fecha<=?", d, h);
        r.gastosEfectivo = suma("SELECT COALESCE(SUM(monto),0) FROM gastos " +
                "WHERE forma_pago='Efectivo' AND fecha>=? AND fecha<=?", d, h);
        r.efectivoEsperado = Math.max(0, montoInicial) + r.ventasEfectivo + r.pagosEfectivo
                - r.gastosEfectivo - r.comprasEfectivo;
        return r;
    }

    private Modelo.Caja leerCaja(Cursor c) {
        Modelo.Caja caja = new Modelo.Caja();
        caja.id = c.getLong(0);
        caja.usuarioId = c.getLong(1);
        caja.usuarioNombre = n(c.getString(2));
        caja.apertura = c.getLong(3);
        caja.cierre = c.getLong(4);
        caja.montoInicial = c.getLong(5);
        caja.montoContado = c.getLong(6);
        caja.diferencia = c.getLong(7);
        caja.notas = n(c.getString(8));
        caja.estado = n(c.getString(9));
        return caja;
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
        r.ventasCredito = suma("SELECT COALESCE(SUM(total),0) FROM ventas " +
                "WHERE anulada=0 AND estado='credito' AND fecha>=? AND fecha<?", d, h);
        r.productosActivos = (int) contar("SELECT COUNT(*) FROM productos WHERE activo=1");
        r.productosAgotados = (int) contar("SELECT COUNT(*) FROM productos WHERE activo=1 AND stock<=0");
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
                        "SUM(vi.costo_total), COUNT(DISTINCT vi.venta_id) " +
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
                p.transacciones = c.getLong(4);
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
            /* Hijos antes que padres: pago_aplicaciones cae por CASCADE con pagos,
               pero su FK a ventas no tiene CASCADE, así que ventas va después. */
            db.delete("adjuntos", null, null);
            db.delete("venta_item_lotes", null, null);
            db.delete("venta_items", null, null);
            db.delete("compra_items", null, null);
            db.delete("pagos", null, null);
            db.delete("ventas", null, null);
            db.delete("compras", null, null);
            db.delete("producto_lotes", null, null);
            db.delete("cotizacion_items", null, null);
            db.delete("cotizaciones", null, null);
            db.delete("agenda", null, null);
            db.delete("gastos", null, null);
            db.delete("productos", null, null);
            db.delete("clientes", null, null);
            db.delete("proveedores", null, null);
            db.delete("cajas", null, null);
            /* Personas, datos de pago y nombre del negocio: se van también, porque
               la tablet puede venderse o cederse. usuarioActualId() vuelve a crear
               la cuenta «Administrador» sola en el siguiente arranque. */
            db.delete("usuarios", null, null);
            db.delete("ajustes", null, null);
            ajustesPorDefecto(db);
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
                "Calle 45 #12-30, Bogotá", "Prefiere pago por Bre-B.");
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
        v1.formaPago = "Transferencia";
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

    // ---------------------------------------------------------------- permisos

    /** ¿Esta cuenta puede abrir este módulo? Vacío = todos; el administrador ve todo. */
    public boolean permite(Modelo.Usuario u, String modulo) {
        if (u == null || modulo == null || modulo.isEmpty()) return true;
        if (u.esAdministrador()) return true;
        String permisos = u.permisos == null ? "" : u.permisos.trim();
        if (permisos.isEmpty()) return true;
        for (String p : permisos.split(",")) {
            if (p.trim().equalsIgnoreCase(modulo)) return true;
        }
        return false;
    }

    public boolean permite(String modulo) {
        return permite(usuarioActual(), modulo);
    }

    public boolean kpisParaEmpleados() {
        return ajusteSi("kpi_empleados", false);
    }

    // ------------------------------------------------------------ cotizaciones

    public static void calcularCotizacion(Modelo.Cotizacion c, List<Modelo.Item> items) {
        long subtotal = 0;
        for (Modelo.Item i : items) {
            i.subtotal = i.precioUnitario * i.cantidad;
            subtotal += i.subtotal;
        }
        c.subtotal = subtotal;
        long base = Math.max(0, subtotal - c.descuento);
        double factor = subtotal > 0 ? (double) base / subtotal : 0;
        long impuesto = 0;
        for (Modelo.Item i : items) {
            double pct = i.impuestoPct >= 0 ? i.impuestoPct : c.impuestoPct;
            i.impuesto = Math.round(i.subtotal * factor * pct / 100.0);
            impuesto += i.impuesto;
        }
        c.impuesto = impuesto;
        c.total = base + impuesto;
    }

    public long guardarCotizacion(Modelo.Cotizacion c, List<Modelo.Item> items) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (c.fecha == 0) c.fecha = Fechas.ahora();
            ContentValues cv = new ContentValues();
            cv.put("numero", "");
            cv.put("fecha", c.fecha);
            if (c.clienteId > 0) cv.put("cliente_id", c.clienteId);
            else cv.putNull("cliente_id");
            cv.put("cliente_nombre", c.clienteNombre == null ? "" : c.clienteNombre);
            cv.put("subtotal", c.subtotal);
            cv.put("descuento", c.descuento);
            cv.put("impuesto_pct", c.impuestoPct);
            cv.put("impuesto", c.impuesto);
            cv.put("total", c.total);
            cv.put("valida_hasta", c.validaHasta);
            cv.put("notas", c.notas);
            cv.put("estado", c.estado == null || c.estado.isEmpty() ? "abierta" : c.estado);
            cv.put("usuario_id", c.usuarioId > 0 ? c.usuarioId : usuarioActualId());
            cv.put("creado", Fechas.ahora());
            c.id = db.insert("cotizaciones", null, cv);
            c.numero = "COT-" + String.format(Locale.ROOT, "%05d", c.id);
            ContentValues num = new ContentValues();
            num.put("numero", c.numero);
            db.update("cotizaciones", num, "id=?", new String[]{String.valueOf(c.id)});
            for (Modelo.Item i : items) {
                ContentValues ci = new ContentValues();
                ci.put("cotizacion_id", c.id);
                if (i.productoId > 0) ci.put("producto_id", i.productoId);
                else ci.putNull("producto_id");
                ci.put("descripcion", i.descripcion);
                ci.put("cantidad", i.cantidad);
                ci.put("precio_unitario", i.precioUnitario);
                ci.put("impuesto_pct", i.impuestoPct);
                ci.put("impuesto", i.impuesto);
                ci.put("subtotal", i.subtotal);
                db.insert("cotizacion_items", null, ci);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return c.id;
    }

    private static final String SQL_COTIZACION =
            "SELECT c.*, u.nombre AS usuario_nombre, " +
                    "(SELECT COUNT(*) FROM cotizacion_items ci WHERE ci.cotizacion_id=c.id) AS num_items " +
                    "FROM cotizaciones c LEFT JOIN usuarios u ON u.id=c.usuario_id ";

    private Modelo.Cotizacion leerCotizacion(Cursor c) {
        Modelo.Cotizacion co = new Modelo.Cotizacion();
        co.id = c.getLong(c.getColumnIndexOrThrow("id"));
        co.numero = n(c.getString(c.getColumnIndexOrThrow("numero")));
        co.fecha = c.getLong(c.getColumnIndexOrThrow("fecha"));
        co.clienteId = c.getLong(c.getColumnIndexOrThrow("cliente_id"));
        co.clienteNombre = n(c.getString(c.getColumnIndexOrThrow("cliente_nombre")));
        co.subtotal = c.getLong(c.getColumnIndexOrThrow("subtotal"));
        co.descuento = c.getLong(c.getColumnIndexOrThrow("descuento"));
        co.impuestoPct = c.getDouble(c.getColumnIndexOrThrow("impuesto_pct"));
        co.impuesto = c.getLong(c.getColumnIndexOrThrow("impuesto"));
        co.total = c.getLong(c.getColumnIndexOrThrow("total"));
        co.validaHasta = c.getLong(c.getColumnIndexOrThrow("valida_hasta"));
        co.notas = n(c.getString(c.getColumnIndexOrThrow("notas")));
        co.estado = n(c.getString(c.getColumnIndexOrThrow("estado")));
        co.usuarioId = c.getLong(c.getColumnIndexOrThrow("usuario_id"));
        int idx = c.getColumnIndex("usuario_nombre");
        co.usuarioNombre = idx >= 0 ? n(c.getString(idx)) : "";
        idx = c.getColumnIndex("num_items");
        co.numItems = idx >= 0 ? c.getInt(idx) : 0;
        return co;
    }

    public List<Modelo.Cotizacion> cotizaciones() {
        List<Modelo.Cotizacion> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                SQL_COTIZACION + "ORDER BY c.fecha DESC, c.id DESC", null)) {
            while (c.moveToNext()) lista.add(leerCotizacion(c));
        }
        return lista;
    }

    public Modelo.Cotizacion cotizacion(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(SQL_COTIZACION + "WHERE c.id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return leerCotizacion(c);
        }
        return null;
    }

    public List<Modelo.Item> itemsCotizacion(long cotizacionId) {
        List<Modelo.Item> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM cotizacion_items WHERE cotizacion_id=? ORDER BY id",
                new String[]{String.valueOf(cotizacionId)})) {
            while (c.moveToNext()) {
                Modelo.Item i = new Modelo.Item();
                i.id = c.getLong(c.getColumnIndexOrThrow("id"));
                i.productoId = c.getLong(c.getColumnIndexOrThrow("producto_id"));
                i.descripcion = n(c.getString(c.getColumnIndexOrThrow("descripcion")));
                i.cantidad = c.getInt(c.getColumnIndexOrThrow("cantidad"));
                i.precioUnitario = c.getLong(c.getColumnIndexOrThrow("precio_unitario"));
                i.impuestoPct = c.getDouble(c.getColumnIndexOrThrow("impuesto_pct"));
                i.impuesto = c.getLong(c.getColumnIndexOrThrow("impuesto"));
                i.subtotal = c.getLong(c.getColumnIndexOrThrow("subtotal"));
                lista.add(i);
            }
        }
        return lista;
    }

    public void marcarCotizacionVendida(long id, long ventaId) {
        ContentValues cv = new ContentValues();
        cv.put("estado", "vendida");
        cv.put("venta_id", ventaId);
        getWritableDatabase().update("cotizaciones", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void eliminarCotizacion(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("cotizacion_items", "cotizacion_id=?", new String[]{String.valueOf(id)});
        db.delete("cotizaciones", "id=?", new String[]{String.valueOf(id)});
    }

    // ------------------------------------------------------------------ agenda

    public long guardarAgenda(Modelo.Agenda a) {
        ContentValues v = new ContentValues();
        v.put("tipo", "proveedor".equals(a.tipo) ? "proveedor" : "cliente");
        v.put("entidad_id", a.entidadId);
        v.put("fecha", a.fecha);
        v.put("titulo", a.titulo == null ? "" : a.titulo.trim());
        v.put("notas", a.notas == null ? "" : a.notas);
        v.put("aviso_dias", Math.max(0, a.avisoDias));
        v.put("recurrente", a.recurrente == null ? "" : a.recurrente);
        v.put("hecho", a.hecho ? 1 : 0);
        SQLiteDatabase db = getWritableDatabase();
        if (a.id > 0) {
            db.update("agenda", v, "id=?", new String[]{String.valueOf(a.id)});
            return a.id;
        }
        v.put("creado", Fechas.ahora());
        a.id = db.insert("agenda", null, v);
        return a.id;
    }

    public void marcarAgendaHecha(long id, boolean hecha) {
        ContentValues v = new ContentValues();
        v.put("hecho", hecha ? 1 : 0);
        getWritableDatabase().update("agenda", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void eliminarAgenda(long id) {
        getWritableDatabase().delete("agenda", "id=?", new String[]{String.valueOf(id)});
    }

    public List<Modelo.Agenda> agendaDe(String tipo, long entidadId) {
        List<Modelo.Agenda> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT a.*, CASE WHEN a.tipo='proveedor' THEN " +
                        "COALESCE((SELECT nombre FROM proveedores WHERE id=a.entidad_id),'') " +
                        "ELSE COALESCE((SELECT nombre FROM clientes WHERE id=a.entidad_id),'') END AS nom " +
                        "FROM agenda a WHERE a.tipo=? AND a.entidad_id=? ORDER BY a.fecha",
                new String[]{tipo, String.valueOf(entidadId)})) {
            while (c.moveToNext()) lista.add(leerAgenda(c));
        }
        return lista;
    }

    private Modelo.Agenda leerAgenda(Cursor c) {
        Modelo.Agenda a = new Modelo.Agenda();
        a.id = c.getLong(c.getColumnIndexOrThrow("id"));
        a.tipo = n(c.getString(c.getColumnIndexOrThrow("tipo")));
        a.entidadId = c.getLong(c.getColumnIndexOrThrow("entidad_id"));
        a.fecha = c.getLong(c.getColumnIndexOrThrow("fecha"));
        a.titulo = n(c.getString(c.getColumnIndexOrThrow("titulo")));
        a.notas = n(c.getString(c.getColumnIndexOrThrow("notas")));
        a.avisoDias = c.getInt(c.getColumnIndexOrThrow("aviso_dias"));
        a.recurrente = n(c.getString(c.getColumnIndexOrThrow("recurrente")));
        a.hecho = c.getInt(c.getColumnIndexOrThrow("hecho")) != 0;
        int idx = c.getColumnIndex("nom");
        a.nombre = idx >= 0 ? n(c.getString(idx)) : "";
        return a;
    }

    /**
     * Lo que hay para hoy: las citas de hoy y las que se repiten (semanal,
     * mensual, anual) que caen en esta fecha. También avisa con anticipación
     * si la cita tiene días de aviso.
     */
    public List<Modelo.Agenda> agendaDeHoy() {
        List<Modelo.Agenda> lista = new ArrayList<>();
        long hoy = Fechas.inicioDia(Fechas.ahora());
        long manana = Fechas.inicioDiaSiguiente(Fechas.ahora());
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT a.*, CASE WHEN a.tipo='proveedor' THEN " +
                        "COALESCE((SELECT nombre FROM proveedores WHERE id=a.entidad_id),'') " +
                        "ELSE COALESCE((SELECT nombre FROM clientes WHERE id=a.entidad_id),'') END AS nom " +
                        "FROM agenda a WHERE a.hecho=0 AND a.fecha<? ORDER BY a.fecha",
                new String[]{String.valueOf(manana + 40L * 24L * 60L * 60L * 1000L)})) {
            while (c.moveToNext()) {
                Modelo.Agenda a = leerAgenda(c);
                if (caeHoy(a, hoy)) lista.add(a);
            }
        }
        return lista;
    }

    private boolean caeHoy(Modelo.Agenda a, long hoy) {
        long dia = Fechas.inicioDia(a.fecha);
        if (dia == hoy) return true;
        Calendar h = Fechas.calendario(hoy);
        Calendar f = Fechas.calendario(dia);
        if ("semanal".equals(a.recurrente)) {
            return h.get(Calendar.DAY_OF_WEEK) == f.get(Calendar.DAY_OF_WEEK);
        }
        if ("mensual".equals(a.recurrente)) {
            return h.get(Calendar.DAY_OF_MONTH) == f.get(Calendar.DAY_OF_MONTH);
        }
        if ("anual".equals(a.recurrente)) {
            return h.get(Calendar.DAY_OF_MONTH) == f.get(Calendar.DAY_OF_MONTH)
                    && h.get(Calendar.MONTH) == f.get(Calendar.MONTH);
        }
        return false;
    }

    // -------------------------------------------------------------------- KPIs

    /** Los indicadores que guían el negocio, calculados con lo que ya existe. */
    public Modelo.Kpi kpis() {
        Modelo.Kpi k = new Modelo.Kpi();
        long ahora = Fechas.ahora();
        long hoy = Fechas.inicioDia(ahora);
        long mes = Fechas.inicioMes(ahora);
        long mesSiguiente = Fechas.inicioMesSiguiente(ahora);
        long mesAnterior = Fechas.inicioMesAnterior(ahora);
        long hace30 = hoy - 30L * 24L * 60L * 60L * 1000L;

        k.ventasHoy = suma("SELECT COALESCE(SUM(total),0) FROM ventas " +
                "WHERE anulada=0 AND fecha>=? AND fecha<?", String.valueOf(hoy),
                String.valueOf(Fechas.inicioDiaSiguiente(ahora)));
        long siete = 0;
        for (int i = 1; i <= 7; i++) {
            long desde = hoy - i * 24L * 60L * 60L * 1000L;
            siete += suma("SELECT COALESCE(SUM(total),0) FROM ventas WHERE anulada=0 " +
                    "AND fecha>=? AND fecha<?", String.valueOf(desde), String.valueOf(desde + 86400000L));
        }
        k.ventasPromedio7 = Math.round(siete / 7.0);

        Modelo.Resumen r = resumen(mes, mesSiguiente);
        k.ticketPromedio = r.ventas > 0 ? Math.round((double) r.ventasTotal / r.ventas) : 0;
        k.margenBruto = r.ventasTotal > 0
                ? (int) Math.round(r.utilidadBruta * 100.0 / r.ventasTotal) : 0;
        k.utilidadNetaMes = r.utilidadNeta;

        long inventario = inventarioAValorCosto();
        Calendar c = Fechas.calendario(ahora);
        int diasDelMes = Math.max(1, c.get(Calendar.DAY_OF_MONTH));
        k.diasInventario = r.costoVentas > 0
                ? (int) Math.round(inventario * (double) diasDelMes / r.costoVentas) : 0;
        k.sinMovimiento30 = (int) contar(
                "SELECT COUNT(*) FROM productos p WHERE p.activo=1 AND NOT EXISTS (" +
                        "SELECT 1 FROM venta_items vi JOIN ventas v ON v.id=vi.venta_id " +
                        "WHERE vi.producto_id=p.id AND v.anulada=0 AND v.fecha>=?)",
                String.valueOf(hace30));

        k.cartera = porCobrar();
        long vencida = 0;
        for (Modelo.Deuda d : deudas()) {
            if (d.fechaLimite > 0 && d.fechaLimite < hoy) vencida += d.saldo;
        }
        k.carteraVencidaPct = k.cartera > 0
                ? (int) Math.round(vencida * 100.0 / k.cartera) : 0;
        k.diasCobro = r.ventasCredito > 0
                ? (int) Math.round(k.cartera * (double) diasDelMes / r.ventasCredito) : 0;
        k.fiadoSobreVentas = r.ventasTotal > 0
                ? (int) Math.round(r.ventasCredito * 100.0 / r.ventasTotal) : 0;
        k.quiebresPct = r.productosActivos > 0
                ? (int) Math.round(r.productosAgotados * 100.0 / r.productosActivos) : 0;

        long mesPasado = suma("SELECT COALESCE(SUM(total),0) FROM ventas WHERE anulada=0 " +
                        "AND fecha>=? AND fecha<?", String.valueOf(mesAnterior), String.valueOf(mes));
        k.crecimientoMes = mesPasado > 0
                ? (int) Math.round((r.ventasTotal - mesPasado) * 100.0 / mesPasado) : 0;
        return k;
    }

    /** Valor del inventario al costo real de los lotes que quedan. */
    private long inventarioAValorCosto() {
        long valor = 0;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(l.cantidad_restante * l.costo_unitario),0) " +
                        "FROM producto_lotes l JOIN productos p ON p.id=l.producto_id " +
                        "WHERE p.activo=1 AND l.recibido=1 AND l.cantidad_restante>0", null)) {
            if (c.moveToFirst()) valor = c.getLong(0);
        }
        if (valor == 0) {
            valor = suma("SELECT COALESCE(SUM(MAX(stock,0) * costo),0) FROM productos WHERE activo=1");
        }
        return valor;
    }

    /** Serie de ventas por día para la gráfica de Inicio. */
    public List<Modelo.PuntoSerie> serieVentas(int dias) {
        List<Modelo.PuntoSerie> lista = new ArrayList<>();
        long hoy = Fechas.inicioDia(Fechas.ahora());
        for (int i = dias - 1; i >= 0; i--) {
            long desde = hoy - i * 24L * 60L * 60L * 1000L;
            long hasta = desde + 86400000L;
            Modelo.PuntoSerie p = new Modelo.PuntoSerie();
            p.desde = desde;
            p.etiqueta = Fechas.formato("dd/MM", desde);
            p.valor = suma("SELECT COALESCE(SUM(total),0) FROM ventas WHERE anulada=0 " +
                    "AND fecha>=? AND fecha<?", String.valueOf(desde), String.valueOf(hasta));
            lista.add(p);
        }
        return lista;
    }

    /** Serie de utilidad y gastos por mes, los últimos seis. */
    public List<Modelo.PuntoSerie> serieUtilidad(int meses) {
        List<Modelo.PuntoSerie> lista = new ArrayList<>();
        long base = Fechas.inicioMes(Fechas.ahora());
        for (int i = meses - 1; i >= 0; i--) {
            long desde = inicioMesAtras(base, i);
            long hasta = inicioMesAtras(base, i - 1);
            Modelo.Resumen r = resumen(desde, hasta);
            Modelo.PuntoSerie p = new Modelo.PuntoSerie();
            p.desde = desde;
            p.etiqueta = Fechas.mesCorto(desde);
            p.valor = r.utilidadBruta;
            p.valor2 = r.gastosTotal;
            lista.add(p);
        }
        return lista;
    }

    private static long inicioMesAtras(long mesActual, int meses) {
        Calendar c = Fechas.calendario(mesActual);
        c.add(Calendar.MONTH, -meses);
        return Fechas.inicioMes(c.getTimeInMillis());
    }

    /** Productos sin ninguna venta en los últimos días (capital dormido). */
    public List<Modelo.Producto> productosSinMovimiento(int dias, int limite) {
        List<Modelo.Producto> lista = new ArrayList<>();
        long desde = Fechas.inicioDia(Fechas.ahora()) - dias * 24L * 60L * 60L * 1000L;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM productos p WHERE p.activo=1 AND NOT EXISTS (" +
                        "SELECT 1 FROM venta_items vi JOIN ventas v ON v.id=vi.venta_id " +
                        "WHERE vi.producto_id=p.id AND v.anulada=0 AND v.fecha>=?) " +
                        "ORDER BY p.stock DESC, p.nombre LIMIT ?",
                new String[]{String.valueOf(desde), String.valueOf(limite)})) {
            while (c.moveToNext()) lista.add(leerProducto(c));
        }
        return lista;
    }

    public static void calcularVenta(Modelo.Venta v, List<Modelo.Item> items) {
        long costo = 0;
        for (Modelo.Item i : items) {
            if (i.costoTotal <= 0) i.costoTotal = i.costoUnitario * i.cantidad;
            costo += i.costoTotal;
        }
        Impuestos.Totales t = Impuestos.calcular(items, v.descuento, v.impuestoPct);
        v.subtotal = t.subtotal;
        v.costo = costo;
        v.impuesto = t.impuesto;
        v.total = t.total;
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
