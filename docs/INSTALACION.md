# Instalación de MCReportPlugin

Esta guía explica cómo instalar MCReportPlugin en un servidor Paper/Spigot y conectarlo con Discord. Los valores mostrados son ejemplos: sustitúyelos por los de tu propio servidor y no publiques credenciales reales.

## Requisitos previos

- Paper o Spigot 1.20.1.
- Java 17 o posterior.
- Acceso de administrador al servidor de Minecraft.
- Permisos de administrador en el servidor de Discord.

## 1. Crear el bot de Discord

1. Abre el [Portal de desarrolladores de Discord](https://discord.com/developers/applications) y crea una aplicación.
2. En **Bot**, crea el bot y copia el token una sola vez en un gestor de secretos o lugar local seguro.
3. En **OAuth2 → URL Generator**, selecciona los scopes `bot` y `applications.commands`.
4. Concede al bot estos permisos: **Ver canales**, **Enviar mensajes**, **Insertar enlaces**, **Adjuntar archivos** y **Gestionar canales**.
5. Invita el bot al servidor de Discord. No publiques la URL de invitación si contiene permisos más amplios de los necesarios.

El token es equivalente a una contraseña. Nunca lo guardes en Git, en una incidencia, en una captura o en una página pública. Si se filtra, regénéralo inmediatamente desde la sección **Bot** del portal.

## 2. Obtener los identificadores de Discord

1. Activa **Modo desarrollador** en **Ajustes de usuario → Avanzado**.
2. Copia el ID del servidor, del canal donde aparecerá el embed, de la categoría de tickets y de los roles de moderación.
3. Guarda esos IDs únicamente en el `config.yml` local del servidor.

## 3. Instalar el plugin

1. Descarga el JAR desde la sección **Releases** del repositorio o compílalo:

   ```bash
   mvn clean package
   ```

2. Copia `MCReportPlugin-2.0.0.jar` en la carpeta `plugins/` de tu servidor.
3. Inicia el servidor una vez y detenlo cuando termine de generar los archivos.
4. Edita `plugins/MCReportPlugin/config.yml`.
5. Inicia el servidor de nuevo y revisa la consola para confirmar que el bot se conectó.

## 4. Configurar el plugin

Rellena los valores de Discord con tus propios datos:

```yaml
discord:
  token: "TU_TOKEN_DEL_BOT"
  guild-id: "ID_DE_TU_SERVIDOR"
  report-embed-channel: "ID_DEL_CANAL_DE_REPORTES"
  report-ticket-category: "ID_DE_LA_CATEGORIA_DE_TICKETS"
  staff-roles:
    - "ID_DEL_ROL_DE_MODERACION"
  admin-role-id: "ID_DEL_ROL_ADMIN"
```

También puedes ajustar `storage`, `reports`, `appeals`, `evidence`, `web` y `update` según las necesidades del servidor. Haz una copia de seguridad antes de cambiar el tipo de almacenamiento o activar funciones adicionales.

## 5. Comprobar la instalación

1. Comprueba que el bot aparece conectado en Discord.
2. Ejecuta `/mcreport` con un usuario autorizado para verificar que el comando está registrado.
3. Publica el embed de reportes siguiendo el flujo previsto por tu instalación.
4. Envía un reporte de prueba y confirma que se crea el canal privado.
5. Revisa que los roles de moderación pueden actuar y que el reportero recibe el mensaje directo correspondiente.

## Solución de problemas

### El bot no inicia

Revisa que el token sea válido, que no tenga espacios adicionales y que el bot esté invitado al servidor correcto. Si el token estuvo expuesto, regénéralo; no lo pegues en la consola ni en una incidencia.

### No se crean canales de tickets

Comprueba que el bot tenga **Gestionar canales** en la categoría indicada y que el ID pertenezca al servidor configurado.

### Los comandos o botones no responden

Confirma que el bot tenga los scopes `bot` y `applications.commands`, que el servidor configurado sea el correcto y que la consola no muestre errores de JDA.

### El plugin no carga

Verifica la versión de Java, que el servidor sea Paper/Spigot compatible y que el JAR se encuentre directamente dentro de `plugins/`. Corrige primero cualquier error de YAML mostrado en la consola.

## Seguridad y mantenimiento

- Mantén el token solo en el archivo local del servidor.
- No subas `config.yml`, bases de datos ni logs con datos reales.
- Concede al bot únicamente los permisos necesarios.
- Haz copias de seguridad de `plugins/MCReportPlugin/` antes de actualizar.
- Regenera el token ante cualquier sospecha de exposición.
