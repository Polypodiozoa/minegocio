-- Invariantes financieras de Mi Negocio.
-- Cada fila: nombre | valor.  Valor 0 = la invariante se cumple (no hay registros que la violen).
-- Se corren sobre una base real (la que trae una copia de seguridad), no sobre datos inventados.
SELECT nombre, valor FROM (
    -- Una venta bien formada: total = base + impuesto, y la base es subtotal menos descuento.
    SELECT 'ventas: total = subtotal - descuento + impuesto' AS nombre,
           COUNT(*) AS valor
      FROM ventas
     WHERE anulada = 0 AND total <> subtotal - descuento + impuesto

    UNION ALL
    -- El subtotal de la venta tiene que ser exactamente la suma de sus ítems.
    SELECT 'ventas: subtotal = suma de sus ítems',
           COUNT(*)
      FROM ventas v
     WHERE v.anulada = 0
       AND v.subtotal <> (SELECT COALESCE(SUM(i.precio_unitario * i.cantidad), 0)
                            FROM venta_items i WHERE i.venta_id = v.id)

    UNION ALL
    -- La utilidad del reporte es total - impuesto - costo: si el costo no cuadra, infla ganancias.
    SELECT 'ventas: costo = suma de sus ítems',
           COUNT(*)
      FROM ventas v
     WHERE v.anulada = 0
       AND v.costo <> (SELECT COALESCE(SUM(i.costo_unitario * i.cantidad), 0)
                         FROM venta_items i WHERE i.venta_id = v.id)

    UNION ALL
    -- Impuesto de venta: redondeo de (subtotal - descuento) × tarifa (tolerancia de 1 peso).
    SELECT 'ventas: impuesto = redondeo de la base × tarifa',
           COUNT(*)
      FROM ventas
     WHERE anulada = 0
       AND ABS(impuesto - ROUND(MAX(0, subtotal - descuento) * impuesto_pct / 100.0)) > 1

    UNION ALL
    -- Cada ítem guarda su subtotal como precio × cantidad.
    SELECT 'ventas: cada ítem guarda precio × cantidad',
           COUNT(*)
      FROM venta_items
     WHERE subtotal <> precio_unitario * cantidad

    UNION ALL
    -- Compras: total = subtotal + impuesto, y el subtotal sale de sus ítems.
    SELECT 'compras: total = subtotal + impuesto',
           COUNT(*)
      FROM compras
     WHERE anulada = 0 AND total <> subtotal + impuesto

    UNION ALL
    SELECT 'compras: subtotal = suma de sus ítems',
           COUNT(*)
      FROM compras c
     WHERE c.anulada = 0
       AND c.subtotal <> (SELECT COALESCE(SUM(i.costo_unitario * i.cantidad), 0)
                            FROM compra_items i WHERE i.compra_id = c.id)

    UNION ALL
    -- Nadie puede haber abonado más de lo que vale una venta.
    SELECT 'abonos: nunca se aplica más que el total de la venta',
           COUNT(*)
      FROM (SELECT a.venta_id FROM pago_aplicaciones a
             GROUP BY a.venta_id
            HAVING SUM(a.monto) > (SELECT v.total FROM ventas v WHERE v.id = a.venta_id))

    UNION ALL
    -- Todo el dinero recibido está repartido en ventas o queda como saldo a favor del cliente.
    SELECT 'abonos: lo repartido + saldo a favor = lo recibido',
           ABS((SELECT COALESCE(SUM(monto), 0) FROM pagos)
               - (SELECT COALESCE(SUM(monto), 0) FROM pago_aplicaciones)
               - (SELECT COALESCE(SUM(CASE WHEN s < 0 THEN -s ELSE 0 END), 0)
                    FROM (SELECT (SELECT COALESCE(SUM(v.total), 0) FROM ventas v
                                   WHERE v.cliente_id = c.id AND v.anulada = 0 AND v.estado = 'credito')
                               - (SELECT COALESCE(SUM(p.monto), 0) FROM pagos p
                                   WHERE p.cliente_id = c.id) AS s
                            FROM clientes c)))

    UNION ALL
    -- Una venta anulada no puede seguir recibiendo abonos.
    SELECT 'ventas anuladas: sin abonos aplicados',
           COUNT(*)
      FROM pago_aplicaciones a JOIN ventas v ON v.id = a.venta_id
     WHERE v.anulada = 1

    UNION ALL
    -- Arqueo de las cajas cerradas: diferencia = contado - efectivo esperado del turno.
    SELECT 'cajas cerradas: la diferencia cuadra con el arqueo',
           COUNT(*)
      FROM cajas c
     WHERE c.estado = 'cerrada' AND c.cierre > 0
       AND c.diferencia <> c.monto_contado - (
             MAX(0, c.monto_inicial)
             + (SELECT COALESCE(SUM(total), 0) FROM ventas
                 WHERE anulada = 0 AND estado <> 'credito' AND forma_pago = 'Efectivo'
                   AND fecha >= c.apertura AND fecha <= c.cierre)
             + (SELECT COALESCE(SUM(monto), 0) FROM pagos
                 WHERE forma_pago = 'Efectivo' AND fecha >= c.apertura AND fecha <= c.cierre)
             - (SELECT COALESCE(SUM(monto), 0) FROM gastos
                 WHERE forma_pago = 'Efectivo' AND fecha >= c.apertura AND fecha <= c.cierre)
             - (SELECT COALESCE(SUM(total), 0) FROM compras
                 WHERE anulada = 0 AND forma_pago = 'Efectivo'
                   AND fecha >= c.apertura AND fecha <= c.cierre))
);
