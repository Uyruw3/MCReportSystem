# Changelog

Todos los cambios relevantes de MCReportPlugin se documentan en este archivo.

## [2.0.3] - 2026-09-24

### Añadido

- Selección de idioma mediante `language` en `config.yml`.
- Traducciones incluidas para español (`es`), inglés (`en`), portugués
  (`pt`) y francés (`fr`).
- Recursos de mensajes separados para facilitar futuras traducciones.

### Mejorado

- Los comandos administrativos y los mensajes principales del juego respetan
  el idioma configurado.
- El español sigue siendo el idioma predeterminado y las claves personalizadas
  existentes de `messages:` mantienen compatibilidad.
- Los idiomas no válidos o los recursos ausentes usan español como respaldo
  seguro y generan una advertencia en la consola.

### Documentación

- Añadidos ejemplos de configuración de idiomas en `README.md`.

## [2.0.2]

- Véase el release `v2.0.2` para los cambios de esa versión.

## [2.0.1]

- Véase el release `v2.0.1` para los cambios de esa versión.
