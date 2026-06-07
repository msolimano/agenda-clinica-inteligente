# I-Clinical Technology

Information Clinical Technology

Conectando informacion clinica con inteligencia

## Alcance de esta entrega

Esta primera base incluye solo:

- Estructura inicial de proyecto backend, frontend y Docker.
- Configuracion base para PostgreSQL local.
- Configuracion base de Spring Boot con Flyway.
- Migracion inicial de base de datos para el modelo minimo del prompt.
- Frontend Vite/React/TypeScript minimo, sin pantallas funcionales.

No incluye todavia autenticacion, endpoints REST, servicios, CRUDs, pantallas de negocio, carga de archivos, integracion OpenAI ni integracion AWS S3.

## Estructura

```text
.
├── backend
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java/com/iclinical/technology
│       │   └── resources
│       │       ├── application.yml
│       │       └── db/migration/V1__initial_schema.sql
│       └── test/java/com/iclinical/technology
├── frontend
│   ├── package.json
│   ├── index.html
│   └── src
└── docker-compose.yml
```

## Ejecucion local prevista

1. Levantar PostgreSQL:

```bash
docker compose up -d postgres
```

2. Levantar backend:

```bash
cd backend
mvn spring-boot:run
```

3. Levantar frontend:

```bash
cd frontend
npm install
npm run dev
```

El backend queda configurado por defecto en `http://localhost:8080` y el frontend en `http://localhost:5173`.
