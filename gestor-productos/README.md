# Gestor de Productos

API REST de práctica para registro, login, JWT, roles y catálogo con Spring Boot, Spring Security y MySQL.

## Requisitos

- JDK 21 o superior
- MySQL 8

No hace falta instalar Maven: el proyecto incluye el Maven Wrapper.

## Base de datos

Crea la base de datos en MySQL Workbench:

```sql
CREATE DATABASE tienda;
```

La aplicación crea o actualiza las tablas al iniciar. Por defecto se conecta a `localhost:3306` con el usuario `root` y contraseña vacía. Si tu configuración es diferente, define las variables antes de ejecutarla en PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/tienda?serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "tu_clave_local"
```

## Ejecutar

Desde esta carpeta:

```powershell
./mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints

| Método | Ruta | Acceso |
|---|---|---|
| `POST` | `/api/auth/register` | Público; crea usuario `USER` y devuelve JWT |
| `POST` | `/api/auth/login` | Público; devuelve JWT |
| `GET` | `/api/productos` | Público |
| `POST` | `/api/productos` | `ADMIN` |
| `GET` | `/api/categorias` | Público |
| `POST` | `/api/categorias` | `ADMIN` |
| `GET` | `/api/marcas` | Público |
| `POST` | `/api/marcas` | `ADMIN` |

El token se envía en `Authorization: Bearer <token>`. El registro siempre asigna `USER`; el cliente no puede elegir su rol.

### Cuerpos de ejemplo

Registro o login:

```json
{
  "email": "vendedor@tienda.com",
  "password": "clave-segura-123"
}
```

Crear categoría o marca:

```json
{
  "name": "Cuidado personal"
}
```

Crear producto (los IDs deben existir):

```json
{
  "name": "Jabón líquido",
  "description": "Presentación de 500 ml",
  "price": 12.50,
  "categoryId": 1,
  "brandId": 1
}
```

## Promover un usuario a ADMIN

Después de registrarlo, actualiza su rol en MySQL Workbench:

```sql
UPDATE usuarios
SET role = 'ADMIN'
WHERE email = 'vendedor@tienda.com';
```

Vuelve a iniciar sesión después del cambio y usa el JWT nuevo.

## Pruebas

```powershell
./mvnw.cmd test
```

Las pruebas usan H2 en memoria; no necesitan una instancia de MySQL.

## Configuración JWT

El token dura una hora por defecto. Para probar una vigencia distinta, configura `JWT_EXPIRATION_MS`. `JWT_SECRET` debe contener al menos 32 caracteres en entornos de desarrollo; reemplaza el valor predeterminado antes de desplegar la aplicación.