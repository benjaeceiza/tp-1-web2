# TP2 - Migración a PostgreSQL, Arquitectura Hexagonal y Transacciones (Web 2 - UNViMe)

API REST desarrollada en Java con Spring Boot. Este proyecto es la evolución del TP1, migrando el almacenamiento en memoria a una base de datos relacional (PostgreSQL) aplicando Arquitectura Hexagonal, migraciones automáticas con Flyway y control de transacciones.

## Requisitos previos
* Java 25
* Maven
* Docker Desktop (necesario para levantar el contenedor de la base de datos)

## Instrucciones para levantar el proyecto
1. Clonar este repositorio:
```bash
git clone https://github.com/benjaeceiza/tp-1-web2
```

2. Posicionarse en el directorio raíz del proyecto:
```bash
cd api-blank
```

3. Levantar la base de datos PostgreSQL usando Docker Compose:
```bash
docker compose up -d
```

4. Levantar la aplicación de Spring Boot:
```bash
./mvnw spring-boot:run
```

**Confirmación de Migraciones:** Al ejecutar el proyecto, Flyway se conectará automáticamente a PostgreSQL. En la consola de Spring Boot se podrá observar en los logs que las migraciones (`V1`, `V2`, `V3` y `V4`) se ejecutan y validan correctamente, preparando el esquema de la base de datos sin intervención manual.

---

## Evidencia y Pruebas (Swagger UI / Postman)
Con la aplicación corriendo, ingresar a `http://localhost:8080/swagger-ui/index.html`. Allí se encuentra la documentación interactiva mostrando los recursos: **Productos**, **Favoritos** y **Listas** con sus respectivas descripciones (@Operation).

*Nota: Las capturas de pantalla o colecciones de pruebas solicitadas por la cátedra se encuentran adjuntas en la entrega o en la carpeta `/evidencias` del repositorio.*

### Casos probados recomendados:
*   **Listas - Éxito:** POST a `/api/listas` creando una lista (Devuelve `201 Created`).
*   **Listas - Error (409 Conflict):** Intentar hacer un DELETE a una lista que todavía tiene favoritos asociados. El sistema ataja la excepción de integridad (DataIntegrityViolationException) y responde con código 409.
*   **Mover Favoritos - Éxito:** POST a `/api/listas/{origenId}/mover-favoritos` pasando un `destinoId` válido. Transfiere los datos y elimina la lista original (Devuelve `204 No Content`).
*   **Mover Favoritos - Error (404 Not Found):** POST al mismo endpoint pero enviando un `destinoId` que no existe. La operación se cancela y devuelve error.

---

## Punto 4: Puertos y Adaptadores - Análisis de la Migración

### ¿Qué cambió?
Para migrar la persistencia de memoria a una base de datos real (PostgreSQL con JPA), **solo fue necesario modificar la capa de infraestructura**. Las clases que se tocaron o crearon fueron:
*   `FavoritoRepositoryAdapter` (antes `FavoritoRepositoryImpl`): Se modificó para dejar de usar un `ArrayList` y pasar a depender de Spring Data JPA. Su función ahora es transformar el objeto de dominio `Favorito` en una entidad y viceversa.
*   `FavoritoEntity`: Clase nueva creada para mapear la tabla `favoritos` en la base de datos usando anotaciones de Hibernate.
*   `FavoritoJpaRepository`: Interfaz nueva que extiende `JpaRepository` para delegar el CRUD a Spring Data.

### ¿Qué NO cambió?
El núcleo de la aplicación se mantuvo **intacto**. Las clases que no sufrieron ninguna modificación fueron:
*   `Favorito` (El modelo del dominio / Record).
*   `FavoritoService` (La lógica de negocio).
*   `FavoritoController` (La capa de presentación / API REST).
*   `FavoritoDTO` (Los objetos de transferencia de datos).

### ¿Por qué fue posible este cambio sin romper nada?
Esto fue posible gracias al patrón de Arquitectura Hexagonal y la Inversión de Dependencias. 
La interfaz `FavoritoRepository` funciona como un **Puerto** (un contrato estricto). La capa de servicio (`FavoritoService`) solo conoce este puerto, no le importa cómo o dónde se guardan los datos. 
Tanto la antigua clase en memoria como el nuevo `FavoritoRepositoryAdapter` funcionan como **Adaptadores** intercambiables. Al cambiar un adaptador por otro detrás de la misma interfaz, la capa de dominio y la lógica de negocio ni siquiera se enteran de que pasamos de usar una lista temporal en Java a un motor robusto como PostgreSQL.    

---

## Punto 6: Evolución del esquema

**¿Por qué se resuelve con una migración nueva (V4) y no modificando V1, V2 o V3?**
Porque Flyway guarda un registro inmutable (un *checksum*) de cada migración aplicada en una tabla interna de control. Modificar un archivo anterior corrompe el historial de Flyway y causaría un error crítico al intentar levantar la aplicación en otros entornos o en producción, ya que el estado real de la base de datos perdería la sincronización con el código fuente. Las bases de datos siempre deben evolucionar hacia adelante.

---

## Punto 7: Transacciones y propiedades ACID

El método `moverFavoritos` del `ListaService` fue marcado con la anotación `@Transactional`. En el contexto de las propiedades **ACID** de las bases de datos, esta anotación garantiza la **Atomicidad** (la operación se ejecuta "todo o nada") y protege la **Consistencia**. 

Si quitáramos el `@Transactional` y el sistema fallara a mitad del proceso (por ejemplo, justo después de actualizar los favoritos pero antes de ejecutar el `deleteById` de la lista origen), la base de datos quedaría en un estado inconsistente: los favoritos estarían movidos a la lista nueva, pero la lista de origen quedaría abandonada en el sistema, vacía y sin borrar. Gracias a la transacción, si la eliminación final falla, Spring hace un *rollback* automático y deshace todos los UPDATEs previos de los favoritos, dejando la base de datos exactamente en el mismo estado en el que estaba antes de intentar la operación.