-- 1. Crear lista por defecto si no existe
INSERT INTO listas (nombre) 
SELECT 'Sin clasificar' 
WHERE NOT EXISTS (SELECT 1 FROM listas WHERE nombre = 'Sin clasificar');

-- 2. Actualizar los favoritos huérfanos (los que tienen lista_id nulo)
UPDATE favoritos 
SET lista_id = (SELECT id FROM listas WHERE nombre = 'Sin clasificar' LIMIT 1) 
WHERE lista_id IS NULL;

-- 3. Recién ahora, declarar la columna como NOT NULL
ALTER TABLE favoritos ALTER COLUMN lista_id SET NOT NULL;