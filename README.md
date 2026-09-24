# MCReportPlugin

Sistema de reportes Minecraft-Discord para Paper/Spigot 1.20.1

## Funcionalidades

- **Embed permanente** en Discord con botón "📋 Reportar Jugador"
- **Formulario modal** con campos: jugador, razón, descripción y links de evidencia
- **Canal temporal privado** por cada reporte (solo visible para staff)
- **Skin del jugador** mostrada en los embeds (via mc-heads.net)
- **Botones de acción**: Ban, Kick, Warn, Resolver
- **Sistema de warns**: 3 warnings = ban automático (configurable)
- **Notificación por DM** al reportero cuando se toma acción
- **Cooldown** entre reportes para prevenir spam
- **Evidencia**: logs de chat, inventario y ubicación del jugador
- **Almacenamiento** en YAML (reports.yml, warns.yml)

## Requisitos

- Java 17+
- Paper/Spigot 1.20.1
- Bot de Discord (token)

## Compilación

```bash
mvn clean package
```

El JAR se genera en `target/MCReportPlugin-2.0.0.jar`.

## Instalación

1. Coloca el JAR en la carpeta `plugins/` de tu servidor
2. Reinicia el servidor
3. Edita `plugins/MCReportPlugin/config.yml`
4. Reinicia nuevamente

### Actualizaciones en hosts gestionados (Play.Hosting/Linux)

La actualización nunca sobrescribe el JAR que está cargado en caliente. Si
`update.enabled` está activo, el plugin descarga el nuevo archivo como
`plugins/MCReportPlugin-new.jar` y muestra su tamaño y SHA-256 con
`/mcreport updatestatus`.

Para aplicarlo desde el panel del host:

1. Ejecuta `/mcreport updateinstructions` o detén el servidor desde el panel.
2. Abre el gestor de archivos y entra en `plugins/`.
3. Renombra `MCReportPlugin.jar` a `MCReportPlugin.jar.bak` (no lo borres todavía).
4. Renombra `MCReportPlugin-new.jar` a `MCReportPlugin.jar`.
5. Inicia el servidor y ejecuta `/mcreport updatestatus` para confirmar la versión.

Este procedimiento funciona en Linux y no necesita scripts, SSH ni permisos para
ejecutar comandos. No uses `/reload` ni sustituyas ningún JAR mientras el
servidor esté encendido. Si el panel no permite renombrar archivos, descarga
`MCReportPlugin-new.jar`, detén el servidor, reemplaza el archivo desde el
panel y vuelve a iniciar.

## Configuración

```yaml
discord:
  token: "TU_TOKEN"              # Token del bot
  guild-id: "ID_SERVIDOR"        # ID del servidor de Discord
  report-embed-channel: "ID"     # Canal donde va el embed de reportar
  report-ticket-category: "ID"   # Categoría donde se crean los tickets
  staff-roles:                   # Roles del staff
    - "ID_ROL_ADMIN"
    - "ID_ROL_MOD"
  admin-role-id: "ID_ROL_ADMIN"  # Rol de administrador
```

## Permisos en Discord

El bot necesita los siguientes permisos en tu servidor de Discord:
- Enviar mensajes
- Gestionar canales
- Ver canales
- Enviar mensajes embebidos
- Adjuntar archivos

## Cómo obtener los IDs

1. Activa el Modo Desarrollador en Discord (Ajustes → Avanzado)
2. Haz clic derecho en el canal/categoría/rol → "Copiar ID"
3. Para el ID del servidor: clic derecho en el icono del servidor → "Copiar ID"