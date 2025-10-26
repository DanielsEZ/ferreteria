# Sistema de Gestión para Ferretería

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.java.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Sistema de gestión integral para ferretería desarrollado en Java con arquitectura web, que permite administrar inventario, ventas, compras, proveedores y generación de reportes.

## 🚀 Características

- **Gestión de Inventario**: Control completo de productos, categorías y existencias.
- **Ventas**: Registro de ventas con generación de facturas.
- **Compras**: Gestión de órdenes de compra a proveedores.
- **Proveedores**: Administración de información de proveedores.
- **Reportes**: Generación de reportes de ventas, compras e inventario.
- **Seguridad**: Autenticación y control de acceso basado en roles.

## 🛠️ Tecnologías Utilizadas

- **Backend**: Java 17, Servlets, JSP
- **Base de Datos**: MySQL
- **Frontend**: HTML5, CSS3, JavaScript, Bootstrap
- **Herramientas**: Maven, JasperReports
- **Servidor**: Apache Tomcat 10.x

## 📋 Requisitos Previos

- Java JDK 17 o superior
- Apache Maven 3.8.6 o superior
- MySQL 8.0 o superior
- Apache Tomcat 10.x
- Navegador web moderno (Chrome, Firefox, Edge, etc.)

## 🚀 Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/DanielsEZ/ferreteria.git
   cd ferreteria
   ```

2. **Configurar la base de datos**
   - Importar el archivo `ferreteria.sql` a tu servidor MySQL
   - Configurar las credenciales en `src/main/resources/db.properties`

3. **Compilar el proyecto**
   ```bash
   mvn clean package
   ```

4. **Desplegar en Tomcat**
   - Copiar el archivo `target/ferreteria-web-1.0.0-SNAPSHOT.war` a la carpeta `webapps` de Tomcat
   - Iniciar el servidor Tomcat

5. **Acceder a la aplicación**
   Abre tu navegador y ve a:
   ```
   http://localhost:8080/ferreteria-web-1.0.0-SNAPSHOT/
   ```

## 📂 Estructura del Proyecto

```
ferreteria/
├── ferreteria-web/           # Módulo web (WAR)
│   ├── src/main/java/       # Código fuente Java
│   ├── src/main/resources/  # Archivos de configuración
│   └── src/main/webapp/     # Recursos web (JSP, CSS, JS)
├── .gitignore
└── README.md
```


## 🤝 Contribuir

Las contribuciones son bienvenidas. Por favor, lee las [pautas de contribución](CONTRIBUTING.md) antes de enviar un pull request.

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## ✉️ Contacto

DanielsEZ - [@daniels_ez](https://x.com/Daniels_EZ)

Enlace del proyecto: [https://github.com/DanielsEZ/ferreteria](https://github.com/DanielsEZ/ferreteria)
