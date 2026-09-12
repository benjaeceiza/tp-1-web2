# TP1 - Consumo de APIs y Spring Boot (Web 2 - UNViMe)

API REST desarrollada en Java con Spring Boot para el consumo de la API externa DummyJSON y la gestión local de productos favoritos en memoria.

## Requisitos previos
* Java 25
* Maven

## Instrucciones para levantar el proyecto
1. Clonar este repositorio:
```bash
git clone https://github.com/benjaeceiza/tp-1-web2
```


1. Posicionarse en el directorio raíz del proyecto:
```bash
   cd api-blank
```

3. Limpiar y compilar las dependencias:
```bash
   ./mvnw clean install
```

4. Levantar la aplicación:
```bash
   ./mvnw spring-boot:run
```

### 2. Captura de Swagger UI
Con tu proyecto levantado, entrá a `http://localhost:8080/swagger-ui/index.html`. Desplegá los bloques de **Productos** y **Favoritos** para que se vean todos los endpoints (GET, POST, PUT, DELETE) con las descripciones que escribiste en las etiquetas `@Operation`, y sacale una captura de pantalla.

### 3. Evidencia de Pruebas (Postman)
Para cumplir con el caso de éxito y de error por cada recurso, armá estas 4 peticiones en Postman. Sacale captura a cada una asegurándote de que se vea la URL, el método HTTP, la respuesta y el **Código de Estado (Status Code)** arriba a la derecha:

*   **Recurso: Productos**
    *   ✅ **Éxito (200 OK):** Hacé un **GET** a `http://localhost:8080/api/productos/1`. Capturá la respuesta exitosa mostrando tu DTO filtrado (con los nombres en español).
    *   ❌ **Error (503 Service Unavailable):** Apagá el Wi-Fi de tu computadora por cinco segundos y hacé un **GET** a `http://localhost:8080/api/productos`. Al no tener internet, tu `RestClient` va a fallar y va a disparar la excepción que armamos para la caída de DummyJSON.
*   **Recurso: Favoritos**
    *   ✅ **Éxito (201 Created):** Hacé un **POST** a `http://localhost:8080/api/favoritos` mandando un JSON válido en el body. Capturá la respuesta mostrando el ID autogenerado.
    *   ❌ **Error (400 Bad Request):** Hacé un **POST** mandando los campos vacíos (`"productoId": null`, `"notaPersonal": ""`). Capturá la respuesta donde salta la validación de Jakarta mostrando exactamente qué campo falló. (Opcionalmente, podés hacer un **GET** al ID 99 para capturar el error `404 Not Found`).