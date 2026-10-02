INSERT INTO categorias (name) VALUES ('Accesorios');
INSERT INTO marcas (name) VALUES ('Marca de demostración');
INSERT INTO productos (name, description, price, category_id, brand_id)
SELECT 'Audífonos de prueba', 'Producto de ejemplo para verificar el catálogo', 39.90, categoria.id, marca.id
FROM categorias categoria
CROSS JOIN marcas marca
WHERE categoria.name = 'Accesorios'
  AND marca.name = 'Marca de demostración';