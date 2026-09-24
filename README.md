# MCReportPlugin

Sistema de reportes entre Minecraft y Discord para Paper/Spigot 1.20.1.

## Funcionalidades

- Embed permanente en Discord con el botón **📋 Reportar jugador**.
- Formulario modal para jugador, motivo, descripción y enlaces de evidencia.
- Canal privado temporal para cada reporte, visible solo para el equipo de moderación.
- Skin del jugador en los embeds mediante `mc-heads.net`.
- Acciones de moderación: ban, kick, warn y resolver.
- Tres advertencias pueden producir un ban automático (límite configurable).
- Aviso por mensaje directo al reportero cuando se toma una decisión.
- Tiempo de espera entre reportes para reducir el spam.
- Evidencia opcional: chat, inventario y ubicación del jugador.
- Almacenamiento en YAML o SQLite.

## Requisitos

- Java 17 o posterior.
- Paper/Spigot 1.20.1.
- Un bot de Discord configurado para tu servidor.

## Instalación

Consulta la [guía de instalación](docs/INSTALACION.md) para crear el bot, configurar sus permisos e instalar el plugin sin publicar credenciales.

## Compilación

```bash
mvn clean package
```

El JAR sombreado se genera en `target/MCReportPlugin-2.0.0.jar`.

## Configuración rápida

Después de iniciar el servidor una vez, edita `plugins/MCReportPlugin/config.yml`. Usa valores propios para tu servidor y conserva el token únicamente en ese archivo local; nunca lo subas a GitHub ni lo compartas en capturas o mensajes.

```yaml
discord:
  token: "TU_TOKEN_DEL_BOT"
  guild-id: "ID_DE_TU_SERVIDOR"
  report-embed-channel: "ID_DEL_CANAL_DE_REPORTES"
  report-ticket-category: "ID_DE_LA_CATEGORIA"
  staff-roles:
    - "ID_DEL_ROL_DE_MODERACION"
  admin-role-id: "ID_DEL_ROL_ADMIN"
```

Los nombres de las claves deben conservarse tal como aparecen en el archivo de configuración generado por el plugin. Para todos los parámetros disponibles, consulta la copia local de `config.yml`.

## Permisos de Discord

El bot necesita, como mínimo, permisos para ver canales, enviar mensajes, insertar enlaces, adjuntar archivos y gestionar canales. Limita esos permisos a las categorías y canales que utilice el sistema.

## Obtener IDs de Discord

1. Activa el **Modo desarrollador** en Discord desde **Ajustes → Avanzado**.
2. Haz clic derecho en el servidor, canal, categoría o rol y selecciona **Copiar ID**.
3. Pega esos valores solo en tu configuración local.

## Seguridad

El token del bot es una credencial. Si alguna vez se expone, revócalo y genera uno nuevo desde el portal de desarrolladores de Discord. No incluyas tokens, IDs privados ni archivos de configuración reales en incidencias, pull requests o documentación.
