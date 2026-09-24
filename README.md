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

## Configuración

```yaml
discord:
  token: "TU_TOKEN"              # Alternativa: variable MCREPORT_DISCORD_TOKEN
  guild-id: "ID_SERVIDOR"        # ID del servidor de Discord
  report-embed-channel: "ID"     # Canal donde va el embed de reportar
  report-ticket-category: "ID"   # Categoría donde se crean los tickets
  staff-roles:                   # Roles del staff
    - "ID_ROL_ADMIN"
    - "ID_ROL_MOD"
  admin-role-id: "ID_ROL_ADMIN"  # Rol de administrador
```

Para evitar guardar secretos en archivos, puedes definir `MCREPORT_DISCORD_TOKEN`
en el entorno del servidor; tiene prioridad sobre `discord.token`.

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