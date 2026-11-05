# TP2 & TP3 - PostgreSQL, Arquitectura Hexagonal, Seguridad (JWT), Pruebas y CI/CD (Web 2 - UNViMe)

API REST desarrollada en Java con Spring Boot. Este proyecto evoluciona la API migrando el almacenamiento en memoria a una base de datos relacional (PostgreSQL) aplicando Arquitectura Hexagonal, migraciones automáticas con Flyway, control de transacciones, autenticación stateless con JWT, autorización por rol y propiedad de recursos, pruebas automatizadas (unitarias y de integración con Testcontainers), Integración Continua con GitHub Actions y Despliegue Continuo en Render.

## Requisitos previos
* Java 25
* Maven
* Docker Desktop (necesario para levantar el contenedor de la base de datos y correr Testcontainers)

## Instrucciones para levantar el proyecto
1. Clonar este repositorio:
```bash
git clone https://github.com/benjaeceiza/tp-1-web2
```

2. Posicionarse en el directorio raíz del proyecto:
```bash
cd tp-1-web2
```

3. Levantar la base de datos PostgreSQL usando Docker Compose:
```bash
docker compose up -d
```

4. Levantar la aplicación de Spring Boot:
```bash
./mvnw spring-boot:run
```

**Confirmación de Migraciones:** Al ejecutar el proyecto, Flyway se conectará automáticamente a PostgreSQL. En la consola de Spring Boot se podrá observar en los logs que las migraciones (`V1` a `V6`) se ejecutan y validan correctamente, preparando el esquema de la base de datos sin intervención manual.

---

## Ejecución de Pruebas y Cobertura (TP3 - Punto 5)

El proyecto cuenta con pruebas unitarias (`*Test`, ejecutadas por Maven Surefire) y pruebas de integración (`*IT`, ejecutadas por Maven Failsafe usando **Testcontainers** con un PostgreSQL real descartable y **Flyway**):

1. **Correr solo las pruebas unitarias** (rápido, no requiere Docker):
   ```bash
   ./mvnw clean test
   ```
2. **Correr toda la suite (unitarias + integración con Testcontainers + reporte JaCoCo)** (requiere Docker Desktop abierto):
   ```bash
   ./mvnw clean verify
   ```
3. **Ver el reporte de cobertura de JaCoCo**:
   Luego de ejecutar `verify`, abrir en el navegador el archivo:
   ```text
   target/site/jacoco/index.html
   ```

---

## Integración Continua con GitHub Actions (TP3 - Punto 6)

El archivo [`.github/workflows/ci.yml`](.github/workflows/ci.yml) define el pipeline de Integración Continua que se dispara automáticamente en cada `push` y `pull_request`:
1. Levanta un runner limpio `ubuntu-latest` (que ya incluye Docker para Testcontainers) e instala **Java 25 (Temurin)**.
2. Compila el código y los tests (`./mvnw --batch-mode test-compile`).
3. Ejecuta las pruebas unitarias (`./mvnw --batch-mode test`).
4. Ejecuta las pruebas de integración con Testcontainers y genera el reporte de cobertura (`./mvnw --batch-mode verify`).
5. Sube el reporte de JaCoCo (`target/site/jacoco`) como artefacto descargable (`reporte-de-cobertura`) en la pestaña **Actions** de GitHub.

---

## Despliegue en Render (TP3 - Punto 7)

El proyecto incluye [`Dockerfile`](Dockerfile) (multi-stage con `eclipse-temurin:25-jdk` y `eclipse-temurin:25-jre`), el endpoint público `GET /health` ([`HealthController`](src/main/java/ar/edu/unvime/apiblank/controller/HealthController.java)) y el Blueprint [`render.yaml`](render.yaml) con `autoDeployTrigger: checksPass`.

### Pasos para desplegar en Render:
1. Asegurarse de que los cambios estén en la rama `main` y el workflow de GitHub Actions esté en verde ✅.
2. Ingresar a [render.com](https://render.com/) con la cuenta de GitHub e ir a **New $\rightarrow$ Blueprint**.
3. Seleccionar el repositorio `tp-1-web2` (rama `main`). Render detectará `render.yaml` para crear la base PostgreSQL (`web2-db`) y el servicio web (`tp-web2-api`).
4. Cuando pida `DB_URL`, escribir temporalmente `pendiente` y hacer clic en **Deploy Blueprint**.
5. Una vez creada la base `web2-db`, entrar a su pestaña **Info**, copiar el **Hostname** interno (`dpg-...-a`) y el nombre de **Database** (`web2_...`), y armar la URL JDBC:
   ```text
   jdbc:postgresql://HOSTNAME:5432/DATABASE
   ```
6. En el servicio web `tp-web2-api` $\rightarrow$ **Environment**, editar `DB_URL` con esa URL JDBC y hacer clic en **Save, rebuild, and deploy**.
7. Verificar en **Logs** que Flyway aplicó las migraciones `V1` a `V6` y que `GET https://<tu-servicio>.onrender.com/health` responde `200 OK` con `{"status":"ok","service":"api-blank"}`.

---

## Evidencia y Pruebas (Swagger UI / Postman)
Con la aplicación corriendo, ingresar a `http://localhost:8080/swagger-ui/index.html`. Allí se encuentra la documentación interactiva mostrando los recursos: **Autenticación**, **Productos**, **Favoritos** y **Listas** con sus respectivas descripciones (`@Operation`) y el botón **Authorize** para probar con el token JWT.

*Nota: Las capturas de pantalla o colecciones de pruebas solicitadas por la cátedra se encuentran adjuntas en la entrega o en la carpeta `/evidencias` del repositorio.*

---

## TP2 - Punto 4: Puertos y Adaptadores - Análisis de la Migración

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

## TP2 - Punto 6: Evolución del esquema

**¿Por qué se resuelve con una migración nueva (V4) y no modificando V1, V2 o V3?**
Porque Flyway guarda un registro inmutable (un *checksum*) de cada migración aplicada en una tabla interna de control. Modificar un archivo anterior corrompe el historial de Flyway y causaría un error crítico al intentar levantar la aplicación en otros entornos o en producción, ya que el estado real de la base de datos perdería la sincronización con el código fuente. Las bases de datos siempre deben evolucionar hacia adelante.

---

## TP2 - Punto 7: Transacciones y propiedades ACID

El método `moverFavoritos` del `ListaService` fue marcado con la anotación `@Transactional`. En el contexto de las propiedades **ACID** de las bases de datos, esta anotación garantiza la **Atomicidad** (la operación se ejecuta "todo o nada") y protege la **Consistencia**. 

Si quitáramos el `@Transactional` y el sistema fallara a mitad del proceso (por ejemplo, justo después de actualizar los favoritos pero antes de ejecutar el `deleteById` de la lista origen), la base de datos quedaría en un estado inconsistente: los favoritos estarían movidos a la lista nueva, pero la lista de origen quedaría abandonada en el sistema, vacía y sin borrar. Gracias a la transacción, si la eliminación final falla, Spring hace un *rollback* automático y deshace todos los UPDATEs previos de los favoritos, dejando la base de datos exactamente en el mismo estado en el que estaba antes de intentar la operación.

---

## TP3 - Punto 4: Autorización por propiedad de recursos y rol

### ¿Dónde vive la regla de autorización?
La regla de autorización por propiedad de recursos y rol vive en la **capa de Servicio** (`ListaService`), centralizada en el método privado `buscarPropiaOFallar(Long id)` (y en la lógica de `obtenerTodas()` y `guardar()`), apoyándose en el componente `ContextoSeguridad` para consultar el `usuarioActualId()` y si `esAdmin()`.

### ¿Por qué se eligió el `Service` y no el `Controller`, un filtro o anotaciones?
1. **Es una regla de negocio sobre el estado del dominio**: Para saber si la lista `id` pertenece al usuario autenticado, primero es necesario **buscar la lista en el repositorio** (y devolver `404 Not Found` si no existe, antes de evaluar si corresponde un `403 Forbidden`). En un filtro HTTP o en el `Controller` todavía no tenemos cargado el objeto de dominio `Lista`.
2. **Un único punto de control (DRY y consistencia)**: Al centralizar la validación en `buscarPropiaOFallar` dentro de `ListaService`, todas las operaciones sobre una lista puntual (`obtenerPorId`, `eliminar`, `favoritosDeUnaLista` y tanto la lista origen como destino en `moverFavoritos`) pasan obligatoriamente por la misma validación sin duplicar `if` en cada endpoint del `Controller`.
3. **Responsabilidad del `Controller` limpia**: El `Controller` solo se encarga de la capa HTTP (recibir y validar DTOs de entrada y devolver códigos de estado). Si otro servicio o componente interno invocara a `ListaService`, la regla de seguridad se seguiría cumpliendo igual, cosa que no pasaría si la validación estuviera pegada en el `Controller`.
4. **Desacoplamiento mediante `ContextoSeguridad`**: `ListaService` no manipula directamente `SecurityContextHolder` ni clases internas de Spring Security, sino que delega la lectura de la identidad actual en `ContextoSeguridad`, facilitando además el testeo unitario con mocks.
