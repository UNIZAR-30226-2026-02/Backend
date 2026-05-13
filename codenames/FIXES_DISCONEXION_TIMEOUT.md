# Informe de Arreglos: Desconexión, Reconexión y Timeouts

Este documento resume los fallos críticos identificados y las soluciones implementadas para estabilizar el sistema de gestión de sesiones WebSocket y timeouts de turno en el proyecto Codenames.

## 1. Problemas Identificados

### A. El Bug de las "Múltiples Pestañas"
- **Causa**: El sistema identificaba usuarios por `idGoogle` pero no rastreaba el número de conexiones activas.
- **Síntoma**: Al cerrar una pestaña teniendo otras abiertas, se iniciaba el temporizador de abandono para el usuario, resultando en una expulsión injusta a los 60 segundos.

### B. Fuga y Sobrescritura de Tareas (Task Leakage)
- **Causa**: El mapa de tareas de desconexión sobrescribía entradas sin cancelar las tareas previamente programadas.
- **Síntoma**: Si un usuario se desconectaba y reconectaba rápido varias veces, tareas "huérfanas" quedaban programadas en el servidor y acababan expulsando al jugador aunque estuviera conectado.

### C. Condiciones de Carrera en el Timeout del Turno
- **Causa**: Los métodos `votar` y `forzarFinTurno` no utilizaban bloqueos de base de datos para serializar el acceso a la partida.
- **Síntoma**: Si el tiempo de turno expiraba justo cuando un jugador enviaba el último voto, ambos hilos intentaban procesar el fin de turno simultáneamente, causando estados inconsistentes o errores de base de datos.

---

## 2. Soluciones Implementadas

### WebSocketEventListener.java
- **Contador de Sesiones**: Se implementó `userSessions` (un `ConcurrentHashMap`) para contar cuántas pestañas tiene abiertas cada usuario. Solo se inicia el proceso de abandono cuando el contador llega a 0.
- **Cancelación Explícita**: Ahora se asegura la cancelación de cualquier tarea de abandono previa mediante `scheduledTask.cancel(false)` antes de sobrescribirla o al reconectar.
- **Doble Validación**: El proceso de abandono verifica nuevamente si el usuario tiene sesiones activas justo antes de ejecutar la lógica de expulsión.

### JuegoService.java
- **Bloqueo Pesimista (Pessimistic Locking)**: Se integró `partidaRepository.findByIdForUpdate(idPartida)` al inicio de `votar` y `forzarFinTurno`.
- **Efecto**: Esto garantiza que el servidor procese un solo cambio de estado a la vez para una partida específica, eliminando colisiones entre el temporizador y las acciones del usuario.

---

## 3. Verificación Realizada

### Pruebas Unitarias Estructurales
Se creó `WebSocketEventListenerTest.java` para validar:
1. **Escenario Multi-pestaña**: Cierre de una pestaña sin activar abandono si quedan otras (EXITO).
2. **Abandono Real**: Activación de tarea solo al cerrar la última pestaña (EXITO).
3. **Cancelación por Reconexión**: Verificación de que la tarea programada se detiene inmediatamente al volver a entrar (EXITO).

### Pruebas de Integridad de Juego
Se ejecutaron las pruebas de `JuegoServiceTest` para asegurar que la introducción de bloqueos no afectó la lógica de negocio (EXITO).

---

## 4. Archivos Modificados
- `src/main/java/com/secretpanda/codenames/config/WebSocketEventListener.java`
- `src/main/java/com/secretpanda/codenames/service/JuegoService.java`
- `src/test/java/com/secretpanda/codenames/Unitarios/service/WebSocketEventListenerTest.java` (Nuevo)
