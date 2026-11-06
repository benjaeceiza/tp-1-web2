# TP2 & TP3 - PostgreSQL, Arquitectura Hexagonal, Seguridad (JWT), Pruebas y CI/CD (Web 2 - UNViMe)

API REST desarrollada en Java 25 con Spring Boot. Este proyecto evoluciona la API migrando el almacenamiento en memoria a una base de datos relacional (PostgreSQL) aplicando Arquitectura Hexagonal, migraciones automáticas con Flyway, control de transacciones, autenticación stateless con JWT, autorización por rol y propiedad de recursos, pruebas automatizadas (unitarias y de integración con Testcontainers), Integración Continua con GitHub Actions y Despliegue en Render.

* **URL pública desplegada en Render**: [https://tp-web2-api.onrender.com](https://tp-web2-api.onrender.com)
* **Health Check en vivo**: [https://tp-web2-api.onrender.com/health](https://tp-web2-api.onrender.com/health)
* **Swagger UI en vivo**: [https://tp-web2-api.onrender.com/swagger-ui/index.html](https://tp-web2-api.onrender.com/swagger-ui/index.html)
* **Ejecución exitosa de GitHub Actions**: [https://github.com/benjaeceiza/tp-1-web2/actions/runs/42039036920](https://github.com/benjaeceiza/tp-1-web2/actions/runs/42039036920)

---

## Requisitos previos
* Java 25
* Maven
* Docker Desktop (necesario para levantar el contenedor de la base de datos local y correr las pruebas de integración con Testcontainers)

## Instrucciones para levantar el proyecto en local
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

## Cómo correr los tests y ver el reporte de cobertura (TP3 - Punto 5)

El proyecto separa las pruebas según su tipo y sufijo:
* **Pruebas Unitarias (`*Test`)**: Ejecutadas por **Maven Surefire**. Prueban clases aisladas (`ListaServiceTest`, `AuthServiceTest`, `JwtServiceTest`, `HealthControllerTest`) usando mocks con Mockito, sin levantar Spring ni base de datos.
* **Pruebas de Integración (`*IT`)**: Ejecutadas por **Maven Failsafe** (`ListaControllerIT`). Levantan el contexto real de Spring Boot (`@SpringBootTest`, `@AutoConfigureMockMvc`) contra una base **PostgreSQL real descartable en Docker mediante Testcontainers**, aplicando todas las migraciones de **Flyway** (`V1` a `V6`) en cada ejecución.

### Comandos:
1. **Ejecutar únicamente las pruebas unitarias** (rápido, no requiere Docker):
   ```bash
   ./mvnw clean test
   ```
   *(En Windows PowerShell: `./mvnw.cmd clean test`)*

2. **Ejecutar toda la suite (unitarias + integración con Testcontainers + reporte JaCoCo)** (requiere **Docker Desktop abierto**):
   ```bash
   ./mvnw clean verify
   ```
   *(En Windows PowerShell: `./mvnw.cmd clean verify`)*

3. **Abrir el reporte de cobertura de JaCoCo**:
   Al finalizar `./mvnw clean verify`, JaCoCo genera el reporte HTML en:
   ```text
   target/site/jacoco/index.html
   ```

---

## Cómo leer el resultado de CI en GitHub Actions (TP3 - Punto 6)

El pipeline de Integración Continua está definido en [`.github/workflows/ci.yml`](.github/workflows/ci.yml) y se ejecuta automáticamente en cada `push` y en cada `pull_request` sobre un entorno limpio (`ubuntu-latest` con Docker y Java 25 Temurin):

1. En el repositorio de GitHub, ingresar a la pestaña **Actions**.
2. Cada ejecución muestra su estado:
   * 🟡 **Amarillo (`In progress`)**: El workflow se está ejecutando.
   * 🟢 **Verde (`Success`)**: El código compiló y pasaron tanto las pruebas unitarias (`*Test`) como las de integración (`*IT` con Testcontainers).
   * 🔴 **Rojo (`Failure`)**: Algún paso falló. Haciendo clic en la ejecución y luego en el job **Compilar y probar**, se despliegan los pasos (`1 — Compilar`, `2 — Pruebas unitarias`, `3 — Pruebas de integración y cobertura`) para ver exactamente qué test falló y por qué.
3. Al final de la página de resumen de cada ejecución (**Summary**), en la sección **Artifacts**, se puede descargar el archivo `reporte-de-cobertura` que contiene el `index.html` generado por JaCoCo durante el CI.

---

## Pasos de despliegue seguidos en Render (TP3 - Punto 7)

El proyecto se desplegó en **Render** utilizando contenedorización con [`Dockerfile`](Dockerfile) (multi-stage con `eclipse-temurin:25-jdk` para compilación y `eclipse-temurin:25-jre` para ejecución) y el archivo Blueprint [`render.yaml`](render.yaml):

1. **Preparación del código**:
   * Se creó el endpoint público `GET /health` ([`HealthController`](src/main/java/ar/edu/unvime/apiblank/controller/HealthController.java)) para el chequeo de salud de Render (`healthCheckPath: /health`).
   * Se parametrizaron en [`application.properties`](src/main/resources/application.properties) las variables de entorno `PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD` y `JWT_SECRET`.
   * Se configuró `autoDeployTrigger: checksPass` en [`render.yaml`](render.yaml) para que Render despliegue automáticamente solo cuando el workflow de GitHub Actions en `main` pase en verde.
2. **Creación del Blueprint en Render**:
   * Con la rama `main` en verde en GitHub Actions, se ingresó a [render.com](https://render.com/) $\rightarrow$ **New + $\rightarrow$ Blueprint** y se conectó el repositorio `benjaeceiza/tp-1-web2` (rama `main`).
   * Render detectó `render.yaml` y creó los dos recursos: la base PostgreSQL remota (`web2-db`) y el servicio web (`tp-web2-api`). En el primer paso se colocó `DB_URL=pendiente` hasta que la base terminara de aprovisionarse.
3. **Configuración de `DB_URL` y puesta en línea (`Live`)**:
   * Desde la sección **Info / Connections** de la base `web2-db` en Render se obtuvieron el **Hostname** interno (`dpg-dbm97qd6e07s73bhj070-a`) y el nombre de **Database** (`web2_np4s`), armando la URL JDBC:
     ```text
     jdbc:postgresql://dpg-dbm97qd6e07s73bhj070-a:5432/web2_np4s
     ```
   * En el servicio `tp-web2-api` $\rightarrow$ **Environment**, se actualizó la variable `DB_URL` con esa cadena JDBC (mientras que `DB_USER`, `DB_PASSWORD` se inyectan desde `web2-db` y `JWT_SECRET` fue autogenerada por Render) y se lanzó el redespliegue.
4. **Verificación del despliegue**:
   * En la pestaña **Logs** de Render se verificó que **Flyway** creó la tabla `flyway_schema_history` y aplicó las migraciones `V1` a `V6` sobre el PostgreSQL remoto, finalizando con `==> Your service is live`.
   * Se comprobó que `GET https://tp-web2-api.onrender.com/health` responde `200 OK` con `{"status":"ok","service":"api-blank"}` y que Swagger UI funciona desde `https://tp-web2-api.onrender.com/swagger-ui/index.html`.

---

## Evidencias del TP3 (en carpeta `/evidencias`)

* **GitHub Actions CI en verde**: [`evidencias/5-github-actions-ci-verde.png`](evidencias/5-github-actions-ci-verde.png)
* **Pull Request con checks aprobados y mergeado a `main`**: [`evidencias/6-pull-request-merged.png`](evidencias/6-pull-request-merged.png)
* **Servicio en estado `Live` en Render**: [`evidencias/7-render-deploy-live.jpg`](evidencias/7-render-deploy-live.jpg)
* **Migraciones de Flyway (`V1` a `V6`) aplicadas en PostgreSQL remoto (Render Logs)**: [`evidencias/8-render-flyway-migraciones.png`](evidencias/8-render-flyway-migraciones.png)
* **Endpoint `/health` respondiendo `200 OK` en la URL pública**: [`evidencias/9-render-health-check.png`](evidencias/9-render-health-check.png)
* **Swagger UI público en Render con botón Authorize**: [`evidencias/10-render-swagger-publico.png`](evidencias/10-render-swagger-publico.png)

---

## TP3 - Punto 4: Autorización por propiedad de recursos y rol

### ¿Dónde vive la regla de autorización?
La regla de autorización por propiedad de recursos y rol vive en la **capa de Servicio** (`ListaService`), centralizada en el método privado `buscarPropiaOFallar(Long id)` (y en la lógica de `obtenerTodas()` y `guardar()`), apoyándose en el componente `ContextoSeguridad` para consultar el `usuarioActualId()` y si `esAdmin()`.

### ¿Por qué se eligió el `Service` y no el `Controller`, un filtro o anotaciones?
1. **Es una regla de negocio sobre el estado del dominio**: Para saber si la lista `id` pertenece al usuario autenticado, primero es necesario **buscar la lista en el repositorio** (y devolver `404 Not Found` si no existe, antes de evaluar si corresponde un `403 Forbidden`). En un filtro HTTP o en el `Controller` todavía no tenemos cargado el objeto de dominio `Lista`.
2. **Un único punto de control (DRY y consistencia)**: Al centralizar la validación en `buscarPropiaOFallar` dentro de `ListaService`, todas las operaciones sobre una lista puntual (`obtenerPorId`, `eliminar`, `favoritosDeUnaLista` y tanto la lista origen como destino en `moverFavoritos`) pasan obligatoriamente por la misma validación sin duplicar `if` en cada endpoint del `Controller`.
3. **Responsabilidad del `Controller` limpia**: El `Controller` solo se encarga de la capa HTTP (recibir y validar DTOs de entrada y devolver códigos de estado). Si otro servicio o componente interno invocara a `ListaService`, la regla de seguridad se seguiría cumpliendo igual, cosa que no pasaría si la validación estuviera pegada en el `Controller`.
4. **Desacoplamiento mediante `ContextoSeguridad`**: `ListaService` no manipula directamente `SecurityContextHolder` ni clases internas de Spring Security, sino que delega la lectura de la identidad actual en `ContextoSeguridad`, facilitando además el testeo unitario con mocks.

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
