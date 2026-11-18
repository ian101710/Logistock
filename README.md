# 📦 LogiStock — Sistema SaaS de Gestión y Control de Inventarios

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)

**LogiStock** es una plataforma web bajo modelo **SaaS (Software as a Service)** diseñada para la gestión integral, control operativo y trazabilidad de inventarios. Permite administrar catálogos de productos, registrar movimientos de almacén (ingresos, ventas, mermas), prevenir quiebres mediante alertas de stock crítico y gestionar suscripciones de planes por usuario.

---

## 🚀 Características Principales

* **Gestión de Inventario y Movimientos:** Registro transaccional de entradas y salidas con validación estricta para evitar saldos negativos.
* **Alertas de Stock Crítico:** Panel dedicado que detecta y aísla automáticamente productos con existencias por debajo del umbral de seguridad (`< 5 unidades`).
* **Auditoría Inalterable:** Bitácora automática que registra cada operación realizada en el sistema (usuario, acción, fecha y variación de stock).
* **Modelo SaaS y Gestión de Planes:** Control de límites según el plan del cliente (Básico / Pro) y flujo de solicitud y aprobación de cambio de plan desde el panel administrativo.
* **Seguridad Híbrida (Web + API):** Autenticación y autorización basada en roles (`RBAC`) mediante **Spring Security** para sesiones web y **JWT (JSON Web Tokens)** para endpoints REST.
* **Interfaz Adaptable (Dark Mode):** Diseño responsivo construido con Bootstrap 5.3 y soporte global para modo claro y oscuro mediante variables CSS.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología | Descripción |
| :--- | :--- | :--- |
| **Backend Core** | Java 17+ / Spring Boot 3.x | Lógica de negocio, inyección de dependencias y servidor embebido. |
| **Persistencia** | Spring Data JPA / Hibernate | Mapeo objeto-relacional (ORM) y gestión transaccional. |
| **Base de Datos** | MySQL 8.x | Motor relacional para almacenamiento de catálogo, usuarios y auditoría. |
| **Seguridad** | Spring Security + JJWT | Control de acceso por roles, protección de rutas y tokens JWT. |
| **Frontend** | Thymeleaf + HTML5 + CSS3 | Renderizado dinámico del lado del servidor (SSR) con fragmentos reutilizables. |
| **Estilos & UI** | Bootstrap 5.3 + JS Vanilla | Componentes responsivos, modales operativos y alternancia de tema oscuro. |
| **Construcción** | Apache Maven | Gestión de dependencias y empaquetado de la aplicación. |

---

## 🏗️ Arquitectura del Proyecto

El sistema sigue una arquitectura en capas bajo el patrón **MVC (Modelo-Vista-Controlador)**:

```text
Logistock/
├── .gitignore
├── README.md
└── app3.0/
    ├── database_saas.sql                  # Script principal de creación de esquema SaaS
    ├── migracion_basico.sql               # Migración inicial de planes básicos
    ├── migracion_solicitudes_plan.sql     # Tabla y relaciones para solicitudes de cambio de plan
    ├── pom.xml                            # Dependencias Maven
    └── src/main/
        ├── java/com/logistock/app/
        │   ├── bootstrap/                 # DataSeeder (carga inicial de roles y datos)
        │   ├── catalog/                   # Definición y reglas de los planes SaaS
        │   ├── controller/                # Controladores Web MVC y API REST
        │   ├── dto/                       # Objetos de transferencia de datos (Login/JWT)
        │   ├── exception/                 # Manejo global de excepciones y límites de plan
        │   ├── model/                     # Entidades JPA (Usuario, Producto, Auditoria, etc.)
        │   ├── repository/                # Interfaces Spring Data JPA
        │   ├── security/                  # Configuración Spring Security y filtros JWT
        │   └── service/                   # Lógica de negocio y reglas transaccionales
        └── resources/
            ├── application.properties     # Configuración general del entorno
            ├── application-mysql.properties # Credenciales y conexión JDBC a MySQL
            ├── static/                    # Recursos estáticos (CSS, JS, imágenes)
            └── templates/                 # Vistas Thymeleaf (app/, layout/, auth)
⚙️ Requisitos Previos e Instalación1. Requisitos del SistemaJava Development Kit (JDK): Versión 17 o superior.MySQL Server: Versión 8.0 o superior.Maven: Versión 3.8+ (o usar el wrapper incluido ./mvnw).2. Configuración de la Base de DatosInicia tu servidor MySQL y crea la base de datos del proyecto:SQLCREATE DATABASE logistock_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
Ejecuta los scripts ubicados en app3.0/ en el siguiente orden para inicializar las tablas y migraciones:database_saas.sqlmigracion_basico.sqlmigracion_solicitudes_plan.sqlVerifica las credenciales de conexión en app3.0/src/main/resources/application-mysql.properties:Propertiesspring.datasource.url=jdbc:mysql://localhost:3306/logistock_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA
3. Ejecución del ProyectoDesde la terminal, ubícate dentro de la carpeta app3.0 y levanta el servidor:Bashcd app3.0
./mvnw spring-boot:run
(En Windows PowerShell o CMD puedes usar mvnw.cmd spring-boot:run).Una vez iniciado, abre tu navegador en:
👉 http://localhost:8080🔑 Rutas Principales del SistemaRutaDescripciónAcceso/Página de inicio / Landing del sistemaPúblico/loginInicio de sesión de usuariosPúblico/registroRegistro de nuevas cuentasPúblico/app/dashboardPanel principal con métricas de inventarioAutenticado/app/productosGestión de catálogo y movimientos de stockAutenticado/app/stock-criticoMonitor de productos con inventario bajoAutenticado/app/auditoriaHistorial completo de operacionesAutenticado/app/planesVisualización y solicitud de mejora de planAutenticado/app/admin/usuariosAdministración de usuarios y rolesAdministrador/app/admin/solicitudesAprobación/rechazo de cambios de planAdministrador👥 Equipo y Liderazgo TécnicoLíder Técnico & Desarrollo: Diego Alexander Chavez Villegas📄 LicenciaEste proyecto fue desarrollado con fines académicos y empresariales para la gestión logística de inventarios.
