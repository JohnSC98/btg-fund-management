# Prueba Técnica - Consulta SQL

## Enunciado

> **Obtener los nombres de los clientes que tienen inscrito algún producto disponible solo en las sucursales que visitan.**

---

## 1. Configuración del Entorno

Levantar una instancia de PostgreSQL en Docker:

```bash
docker run --name btg_db -e POSTGRES_PASSWORD=btg_pass -p 5432:5432 -d postgres
```

---

## 2. Creación de Tablas

Se define el modelo relacional con las entidades **cliente**, **producto**, **sucursal** y sus relaciones: **inscripcion**, **disponibilidad** y **visitan**.

```sql
-- Tabla de clientes registrados en el sistema
CREATE TABLE cliente (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(100),
    apellidos VARCHAR(100),
    ciudad    VARCHAR(100)
);

-- Catálogo de productos financieros ofrecidos por la entidad
CREATE TABLE producto (
    id           SERIAL PRIMARY KEY,
    nombre       VARCHAR(100),
    tipoProducto VARCHAR(50)
);

-- Sucursales físicas donde se atienden clientes
CREATE TABLE sucursal (
    id     SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    ciudad VARCHAR(100)
);

-- Relación N:M entre productos y clientes.
-- Registra a qué productos está inscrito cada cliente.
CREATE TABLE inscripcion (
    idProducto INT REFERENCES producto(id),
    idCliente  INT REFERENCES cliente(id),
    PRIMARY KEY (idProducto, idCliente)
);

-- Relación N:M entre sucursales y productos.
-- Indica en qué sucursales está disponible cada producto.
CREATE TABLE disponibilidad (
    idSucursal INT REFERENCES sucursal(id),
    idProducto INT REFERENCES producto(id),
    PRIMARY KEY (idSucursal, idProducto)
);

-- Relación N:M entre sucursales y clientes.
-- Registra qué sucursales ha visitado cada cliente y en qué fecha.
CREATE TABLE visitan (
    idSucursal  INT REFERENCES sucursal(id),
    idCliente   INT REFERENCES cliente(id),
    fechaVisita DATE NOT NULL,
    PRIMARY KEY (idSucursal, idCliente)
);
```

---

## 3. Datos de Prueba

### Datos maestros

```sql
-- Dos clientes: John en Bogotá y Maria en Medellín
INSERT INTO cliente VALUES
    (1, 'John',  'Suarez', 'Bogotá'),
    (2, 'Maria', 'Gomez',  'Medellín');

-- Dos productos financieros: un FPV y un FIC
INSERT INTO producto VALUES
    (10, 'Fondo Ecopetrol', 'FPV'),
    (20, 'Fondo Dinámica',  'FIC');

-- Tres sucursales distribuidas en Bogotá y Medellín
INSERT INTO sucursal VALUES
    (100, 'Sucursal Centro',  'Bogotá'),
    (200, 'Sucursal Norte',   'Bogotá'),
    (300, 'Sucursal Poblado', 'Medellín');
```

### Caso EXITOSO — John debe aparecer en el resultado

```sql
-- John se inscribe al producto "Fondo Ecopetrol" (id=10)
INSERT INTO inscripcion VALUES (10, 1);

-- John visita la Sucursal Centro (id=100)
INSERT INTO visitan VALUES (100, 1, '2025-06-20');

-- El producto "Fondo Ecopetrol" está disponible en la Sucursal Centro (id=100)
-- Como John visita la sucursal donde su producto está disponible, CUMPLE la condición.
INSERT INTO disponibilidad VALUES (100, 10);
```

### Caso FALLIDO — Maria NO debe aparecer en el resultado

```sql
-- Maria se inscribe al producto "Fondo Dinámica" (id=20)
INSERT INTO inscripcion VALUES (20, 2);

-- Maria visita la Sucursal Poblado (id=300) en Medellín
INSERT INTO visitan VALUES (300, 2, '2025-06-21');

-- Sin embargo, "Fondo Dinámica" solo está disponible en la Sucursal Centro (id=100)
-- Maria visita la sucursal 300, pero su producto está en la 100 → NO CUMPLE la condición.
INSERT INTO disponibilidad VALUES (100, 20);
```

---

## 4. Solución

La consulta cruza las tres relaciones para encontrar clientes cuyo producto inscrito coincide en la misma sucursal que visitan:

- **`inscripcion`** vincula al cliente con su producto.
- **`disponibilidad`** vincula al producto con la sucursal donde está disponible.
- **`visitan`** vincula al cliente con la sucursal que frecuenta.
- El `JOIN` entre `disponibilidad` y `visitan` sobre `idSucursal` garantiza que **el producto esté disponible precisamente en una sucursal que el cliente visita**.

```sql
SELECT DISTINCT c.nombre, c.apellidos
FROM cliente c
         JOIN inscripcion i    ON c.id = i.idCliente
         JOIN disponibilidad d ON i.idProducto = d.idProducto
         JOIN visitan v        ON c.id = v.idCliente
                              AND d.idSucursal = v.idSucursal;
```

### Resultado Esperado

| nombre | apellidos |
|--------|-----------|
| John   | Suarez    |

> Maria no aparece porque, aunque está inscrita en "Fondo Dinámica", este producto solo está disponible en la Sucursal Centro (100) y ella únicamente visita la Sucursal Poblado (300).
