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

Ejecuta cada petición contra la aplicación y anota el código recibido. Para las peticiones `POST`, usa el JSON que corresponda a los DTO de categoría y marca del proyecto. No se incluyen cuerpos de ejemplo porque el PDF no especifica sus campos.

| Recurso | Petición | Credencial | Esperado | Observado |
|---|---|---|---:|---:|
| Categorías | `GET /api/categorias` | Ninguna | 200 | ______ |
| Categorías | `POST /api/categorias` | Ninguna | 403* | ______ |
| Categorías | `POST /api/categorias` | Token `USER` | 403 | ______ |
| Categorías | `POST /api/categorias` | Token `ADMIN` | 200 o 201 | ______ |
| Marcas | `GET /api/marcas` | Ninguna | 200 | ______ |
| Marcas | `POST /api/marcas` | Ninguna | 403* | ______ |
| Marcas | `POST /api/marcas` | Token `USER` | 403 | ______ |
| Marcas | `POST /api/marcas` | Token `ADMIN` | 200 o 201 | ______ |

`*` La guía espera 403. Algunas configuraciones de Spring Security responden 401 cuando falta autenticación; si ocurre, revisa el `AuthenticationEntryPoint` y documenta el comportamiento real de tu aplicación.

**Pregunta de criterio:** Conviene proteger la escritura porque crear o modificar el catálogo cambia información del negocio y debe quedar bajo responsabilidad de usuarios autorizados. La lectura puede permanecer pública para que clientes consulten productos, categorías y marcas sin crear una cuenta.

## Ejercicio 2: diagnóstico de configuración insegura

La regla `authenticated()` solo comprueba que el usuario haya iniciado sesión; no exige que tenga el rol `ADMIN`. Por eso cualquier usuario autenticado, incluso uno con rol `USER`, podría crear productos. En un negocio real esto permitiría altas no autorizadas, productos falsos o alteraciones del catálogo. Cambiaría esa regla por una autorización que exija `hasRole("ADMIN")` para las peticiones `POST` a productos.

## Ejercicio 3: usuario nuevo y cambio de rol

Registra un usuario nuevo y conserva el token devuelto. Verifica en MySQL Workbench que la contraseña almacenada sea un hash BCrypt (por ejemplo, empiece con `$2a$` o `$2b$`), no la contraseña original. Inicia sesión con ese usuario y utiliza el token en las peticiones protegidas.

| Paso | Petición o acción | Resultado esperado | Observado |
|---|---|---|---|
| 1 | `POST /api/auth/register` | Registro exitoso; contraseña hasheada en la base | ______ |
| 2 | `POST /api/auth/login` | Login exitoso y token disponible | ______ |
| 3 | `GET /api/productos` sin token | 200 | ______ |
| 4 | `POST /api/productos` con token `USER` | 403 | ______ |
| 5 | Promover al usuario a `ADMIN` en la base de datos | Rol actualizado | ______ |
| 6 | Volver a iniciar sesión y usar el token nuevo | Login exitoso | ______ |
| 7 | `POST /api/productos` con token `ADMIN` nuevo | 200 o 201 | ______ |
| 8 | `GET /api/productos` con o sin token | 200 | ______ |

No ejecutes un `UPDATE` hasta confirmar el nombre de la tabla y de la columna de rol en el esquema de tu proyecto. Después de cambiar el rol, vuelve a iniciar sesión: el JWT anterior fue emitido antes del cambio y contiene las credenciales/claims con los que se autenticó el usuario. El token nuevo refleja el rol actualizado.

## Opcionales

- **Token de 15 minutos:** configura la expiración a 15 minutos. Una petición con un token vencido no debe autenticar al usuario; la API debe rechazarla (normalmente con 401) y el cliente debe iniciar sesión otra vez.
- **Correo repetido:** una respuesta `409 Conflict` comunica que el correo ya está registrado y permite al cliente corregir el dato. La API debe manejar la restricción de unicidad y evitar exponer detalles internos de la base de datos.

## Nota sobre verificación

Se creó el proyecto Spring Boot en `gestor-productos/`, con las rutas, roles y flujo JWT de esta guía. La suite automatizada pasó usando H2 en memoria, incluida la promoción de `USER` a `ADMIN` y la emisión de un token nuevo. La conexión con MySQL local aún requiere configurar sus credenciales y ejecutar la aplicación; completa la columna **Observado** con los resultados de tus propias peticiones.