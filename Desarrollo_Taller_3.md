# Desarrollo: Seguridad backend y JWT

## Ejercicio 1: proteger categorías y marcas

En `SecurityConfig.java`, agrega las reglas de categorías y marcas antes de `.anyRequest().authenticated()`. Conserva las reglas existentes para autenticación y productos:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers(HttpMethod.GET,
        "/api/productos/**",
        "/api/categorias", "/api/categorias/**",
        "/api/marcas", "/api/marcas/**").permitAll()
    .requestMatchers(HttpMethod.POST,
        "/api/productos/**",
        "/api/categorias", "/api/categorias/**",
        "/api/marcas", "/api/marcas/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

`hasRole("ADMIN")` busca la autoridad `ROLE_ADMIN`. Confirma que el `UserDetails` de la aplicación convierta el rol a ese formato. Si ya existen reglas equivalentes para productos, puedes conservarlas y añadir únicamente las rutas de categorías y marcas.

### Registro de pruebas

Resultados observados en la suite automatizada con MockMvc y H2 en memoria. El ejercicio 3 también se comprobó manualmente contra MySQL desde Workbench y la API.

| Recurso | Petición | Credencial | Esperado | Observado |
|---|---|---|---:|---:|
| Categorías | `GET /api/categorias` | Ninguna | 200 | 200 |
| Categorías | `POST /api/categorias` | Ninguna | 403* | 403 |
| Categorías | `POST /api/categorias` | Token `USER` | 403 | 403 |
| Categorías | `POST /api/categorias` | Token `ADMIN` | 200 o 201 | 201 |
| Marcas | `GET /api/marcas` | Ninguna | 200 | 200 |
| Marcas | `POST /api/marcas` | Ninguna | 403* | 403 |
| Marcas | `POST /api/marcas` | Token `USER` | 403 | 403 |
| Marcas | `POST /api/marcas` | Token `ADMIN` | 200 o 201 | 201 |

`*` La guía espera 403. Algunas configuraciones de Spring Security responden 401 cuando falta autenticación; si ocurre, revisa el `AuthenticationEntryPoint` y documenta el comportamiento real de tu aplicación.

**Pregunta de criterio:** Conviene proteger la escritura porque crear o modificar el catálogo cambia información del negocio y debe quedar bajo responsabilidad de usuarios autorizados. La lectura puede permanecer pública para que clientes consulten productos, categorías y marcas sin crear una cuenta.

## Ejercicio 2: diagnóstico de configuración insegura

La regla `authenticated()` solo comprueba que el usuario haya iniciado sesión; no exige que tenga el rol `ADMIN`. Por eso cualquier usuario autenticado, incluso uno con rol `USER`, podría crear productos. En un negocio real esto permitiría altas no autorizadas, productos falsos o alteraciones del catálogo. Cambiaría esa regla por una autorización que exija `hasRole("ADMIN")` para las peticiones `POST` a productos.

## Ejercicio 3: usuario nuevo y cambio de rol

Registra un usuario nuevo y conserva el token devuelto. Verifica en MySQL Workbench que la contraseña almacenada sea un hash BCrypt (por ejemplo, empiece con `$2a$` o `$2b$`), no la contraseña original. Inicia sesión con ese usuario y utiliza el token en las peticiones protegidas.

| Paso | Petición o acción | Resultado esperado | Observado |
|---|---|---|---|
| 1 | `POST /api/auth/register` | Registro exitoso; contraseña hasheada en la base | HTTP 201; prefijo BCrypt `$2a$` verificado en MySQL |
| 2 | `POST /api/auth/login` | Login exitoso y token disponible | HTTP 200 |
| 3 | `GET /api/productos` sin token | 200 | HTTP 200 (prueba automatizada) |
| 4 | `POST /api/productos` con token `USER` | 403 | HTTP 403 (comprobado contra MySQL) |
| 5 | Promover al usuario a `ADMIN` en la base de datos | Rol actualizado | Workbench: 1 fila encontrada y cambiada; rol `ADMIN` |
| 6 | Volver a iniciar sesión y usar el token nuevo | Login exitoso | HTTP 200; token nuevo con rol `ADMIN` |
| 7 | `POST /api/productos` con token `ADMIN` nuevo | 200 o 201 | HTTP 201 (producto guardado en MySQL) |
| 8 | `GET /api/productos` con o sin token | 200 | HTTP 200; producto persistido visible con JWT `ADMIN` |

La suite verifica el ciclo completo en H2; además, el flujo se ejecutó manualmente contra MySQL. En Workbench, `vendedor@tienda.com` quedó como `ADMIN` con prefijo BCrypt `$2a$`, y la API creó un producto con HTTP 201 usando un JWT nuevo. El token anterior conserva el rol `USER` con el que se emitió.

## Opcionales

- **Token de 15 minutos:** configura la expiración a 15 minutos. Una petición con un token vencido no debe autenticar al usuario; la API debe rechazarla (normalmente con 401) y el cliente debe iniciar sesión otra vez.
- **Correo repetido:** una respuesta `409 Conflict` comunica que el correo ya está registrado y permite al cliente corregir el dato. La API debe manejar la restricción de unicidad y evitar exponer detalles internos de la base de datos.

## Nota sobre verificación

Se creó el proyecto Spring Boot en `gestor-productos/`, con las rutas, roles, JWT y Swagger UI de esta guía. La suite automatizada pasó con H2 y el ejercicio 3 también se verificó manualmente en MySQL Workbench y contra la API local. La contraseña real nunca se incluyó en el repositorio.