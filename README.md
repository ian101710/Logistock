# Logistock
# LOGISTOCK 📦
### Sistema de Gestión de Inventario

**LOGISTOCK** es una aplicación web orientada a la administración y control de inventarios, que permite gestionar productos, categorías, usuarios y movimientos relacionados con el stock de manera organizada.

El proyecto está desarrollado con Java y Spring Boot, utilizando MySQL como sistema de gestión de base de datos y Thymeleaf para la presentación de las interfaces web.

---

## 🚀 Características principales

- **Gestión de productos:** registro, consulta, actualización y administración de productos.
- **Gestión de categorías:** organización de los productos mediante categorías.
- **Control de inventario:** seguimiento de cantidades disponibles y visualización de stock crítico.
- **Panel administrativo:** administración de usuarios y funcionalidades del sistema.
- **Gestión de usuarios:** registro, autenticación y control de acceso.
- **Seguridad:** implementación de Spring Security y autenticación mediante JWT.
- **Auditoría:** registro y consulta de actividades del sistema.
- **Gestión de planes:** visualización de planes y administración de solicitudes de cambio de plan.

---

## 🛠️ Tecnologías utilizadas

| Tecnología | Descripción |
|---|---|
| Java 20 | Lenguaje de programación |
| Spring Boot 3.5.14 | Framework de desarrollo |
| Spring Security | Seguridad y control de acceso |
| Spring Data JPA | Persistencia de datos |
| Thymeleaf | Motor de plantillas web |
| MySQL | Base de datos |
| Maven | Gestión de dependencias |
| JWT | Autenticación mediante tokens |
| HTML5 / CSS3 / JavaScript | Tecnologías de interfaz web |
| Git / GitHub | Control de versiones y colaboración |

---

## 📁 Estructura del proyecto

```text
LOGISTOCK/
│
├── README.md
├── .gitignore
│
└── app3.0/
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    │
    ├── database_saas.sql
    ├── migracion_basico.sql
    ├── migracion_solicitudes_plan.sql
    │
    └── src/
        ├── main/
        │   ├── java/com/logistock/app/
        │   │   ├── controller/
        │   │   ├── service/
        │   │   ├── repository/
        │   │   ├── model/
        │   │   ├── security/
        │   │   ├── exception/
        │   │   ├── dto/
        │   │   └── bootstrap/
        │   │
        │   └── resources/
        │       ├── templates/
        │       ├── static/
        │       ├── application.properties
        │       └── application-mysql.properties
        │
        └── test/
```

---

## ⚙️ Requisitos de instalación

Para ejecutar el proyecto se necesita:

- JDK 20.
- MySQL Server.
- Git.
- Conexión a Internet para descargar las dependencias de Maven.

## 💻 Instalación y ejecución

**1. Clonar el repositorio**

```bash
git clone https://github.com/USUARIO/LOGISTOCK.git
```

**2. Ingresar a la carpeta del proyecto**

```bash
cd LOGISTOCK/app3.0
```

**3. Crear la base de datos**

Crear una base de datos MySQL denominada:

```sql
CREATE DATABASE logistock_db;
```

Importar el script SQL correspondiente y ejecutar las migraciones necesarias según la versión de la base de datos.

**4. Configurar la conexión**

Revisar los archivos:

- `application.properties`
- `application-mysql.properties`

Configurar los datos de conexión a MySQL y el secreto JWT antes de iniciar la aplicación. No publicar contraseñas ni claves privadas en GitHub.

**5. Ejecutar la aplicación**

En Windows:

```bash
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

**6. Acceder al sistema**

Abrir el navegador y acceder a:

```text
http://localhost:8080
```

---

## 🌿 Estrategia de control de versiones

El desarrollo colaborativo de LOGISTOCK utiliza Git y GitHub para organizar las modificaciones y mantener un historial de cambios.

Se establece la siguiente estructura de ramas:

| Rama | Función |
|---|---|
| `main` | Contiene la versión estable del proyecto. |
| `develop` | Integra las modificaciones de los colaboradores. |
| `feature/*` | Permite desarrollar mejoras y funcionalidades independientes. |

Las ramas de funcionalidades se integran mediante Pull Requests. Una vez verificadas y fusionadas, se eliminan para mantener organizado el repositorio.

---

## 👥 Colaboradores

**Diego**  
Desarrollo, optimización y mantenimiento del proyecto.

**Ian**  
Desarrollo, optimización y mantenimiento del proyecto.

Ambos integrantes participan en la gestión del repositorio, integración de cambios y control de versiones mediante Git y GitHub.

---

## 🎯 Objetivo del proyecto

Desarrollar y mantener una solución de software para la gestión de inventarios, aplicando buenas prácticas de programación, organización del código y desarrollo colaborativo mediante herramientas de control de versiones.

---

**LOGISTOCK – Sistema de Gestión de Inventario**

*Desarrollo colaborativo con Git y GitHub.*
