

```bash
docker compose up -d
```

El `-d` es para que corra en segundo plano. La primera vez tardará un poco porque descarga la imagen de PostgreSQL.


**4. Comprueba que está corriendo**

```bash
docker compose ps
```

Deberías ver el contenedor `files` con estado `healthy`.

---

**5. Conéctate para verificar que las tablas se crearon bien**

```bash
docker exec -it files psql -U red_panda_us -d clasified_files
```

Una vez dentro puedes ejecutar:

```sql
-- Ver todas las tablas
\dt

-- Ver la estructura de una tabla concreta
\d jugador

-- Salir
\q
```

---

```bash
# Parar el contenedor
docker compose down

# Parar Y borrar los datos (para empezar desde cero)
docker compose down -v

# Ver los logs si algo falla
docker compose logs postgres
```

---

**Importante:** si haces `docker compose down -v` y vuelves a levantar el contenedor, el `init.sql` se vuelve a ejecutar automáticamente y las tablas se recrean desde cero.