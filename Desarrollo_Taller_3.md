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

Resultados observados en la suite automatizada con MockMvc y H2 en memoria. Para una evidencia manual, ejecuta además las mismas peticiones en Swagger UI o Postman.

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
| 1 | `POST /api/auth/register` | Registro exitoso; contraseña hasheada en la base | 201; hash BCrypt verificado (`$2...`) |
| 2 | `POST /api/auth/login` | Login exitoso y token disponible | 200 |
| 3 | `GET /api/productos` sin token | 200 | 200 |
| 4 | `POST /api/productos` con token `USER` | 403 | 403 |
| 5 | Promover al usuario a `ADMIN` en la base de datos | Rol actualizado | `UPDATE` aplicado en H2 de prueba |
| 6 | Volver a iniciar sesión y usar el token nuevo | Login exitoso | 200 |
| 7 | `POST /api/productos` con token `ADMIN` nuevo | 200 o 201 | 201 |
| 8 | `GET /api/productos` con o sin token | 200 | 200 |

La suite verifica el ciclo completo en H2: BCrypt, registro, rechazo de escritura como `USER`, promoción y nuevo login, y creación de producto como `ADMIN`. Para repetir la promoción en MySQL Workbench, confirma el esquema y ejecuta `UPDATE usuarios SET role = 'ADMIN' WHERE email = 'vendedor@tienda.com';`; después vuelve a iniciar sesión. El token anterior conserva el rol `USER` con el que se emitió.

## Opcionales

- **Token de 15 minutos:** configura la expiración a 15 minutos. Una petición con un token vencido no debe autenticar al usuario; la API debe rechazarla (normalmente con 401) y el cliente debe iniciar sesión otra vez.
- **Correo repetido:** una respuesta `409 Conflict` comunica que el correo ya está registrado y permite al cliente corregir el dato. La API debe manejar la restricción de unicidad y evitar exponer detalles internos de la base de datos.

## Nota sobre verificación

Se creó el proyecto Spring Boot en `gestor-productos/`, con las rutas, roles, JWT y Swagger UI de esta guía. La suite automatizada valida los códigos HTTP anotados en las tablas usando H2 en memoria. La ejecución manual con MySQL Workbench aún depende de configurar tus credenciales locales; los resultados indicados corresponden a las pruebas automatizadas, no a una conexión observada a MySQL.