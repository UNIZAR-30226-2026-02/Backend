# INSTRUCCIONES PARA RESUBIR LA BASE DE DATOS

**IMPORTANTE**: cercionarse de que los archivos que hay subidos en la terminal de azure son los correctos.

Para conectarse al servidor de la base de datos:

```shell
# Conéctate a la base de datos por defecto (postgres)
psql -h codenames-db.postgres.database.azure.com -U red_panda_us -d postgres
```

dentro de la base:

```sql
-- 1. Expulsar a todos los usuarios conectados a esa base de datos
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = 'clasified_files' AND pid <> pg_backend_pid();

-- 2. Ahora sí, borrar la base de datos
DROP DATABASE clasified_files;

-- 3. Volver a crearla vacía
CREATE DATABASE clasified_files;

-- 4. Salir
\q
```

Una vez borrada y creada de nuevo:

```shell
psql -h codenames-db.postgres.database.azure.com -U red_panda_us -d clasified_files -f init.sql

psql -h codenames-db.postgres.database.azure.com -U red_panda_us -d clasified_files -f triggers.sql
```
