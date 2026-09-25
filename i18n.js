const translations = {
  es: {
    "nav.home": "Inicio", "nav.install": "Instalación", "nav.label": "Navegación principal", "language.label": "Idioma",
    "home.title": "Reportes más rápidos, servidores más seguros.",
    "home.lead": "MCReportSystem conecta los reportes de tu servidor Minecraft con Discord para que el equipo de moderación pueda actuar con contexto y privacidad.",
    "home.install": "Ver instalación", "home.discord": "Dudas y problemas en Discord",
    "home.featuresTitle": "Todo lo necesario para moderar",
    "home.featureReportsTitle": "Reportes privados", "home.featureReports": "Un canal temporal por reporte, visible únicamente para el staff autorizado.",
    "home.featureActionsTitle": "Acciones desde Discord", "home.featureActions": "Ban, kick, warn y resolución desde botones, con notificación al jugador.",
    "home.featureEvidenceTitle": "Evidencia útil", "home.featureEvidence": "Consulta logs de chat, inventario, ubicación y la skin del jugador.",
    "home.featureBedrockTitle": "Java y Bedrock", "home.featureBedrock": "Compatibilidad con Geyser y Floodgate para comunidades mixtas.",
    "home.helpTitle": "¿Necesitas ayuda?", "home.help": "Para dudas, errores o sugerencias, únete al servidor oficial de Discord.", "home.joinDiscord": "Unirse a Discord →",
    "install.title": "Instalación y configuración", "install.lead": "Instala el plugin en Paper o Spigot 1.20.1 con Java 17 o posterior y conecta un bot de Discord.",
    "install.requirementsTitle": "Requisitos", "install.requirements": "Un servidor Minecraft con Paper o Spigot 1.20.1, Java 17 o posterior y una aplicación de Discord con un bot.",
    "install.stepsTitle": "Pasos de instalación", "install.step1": "Descarga el JAR más reciente de MCReportPlugin desde GitHub Releases.", "install.releases": "Abrir la última versión",
    "install.step2": "Detén el servidor, coloca el JAR en plugins/ e inicia el servidor una vez. Vuelve a detenerlo antes de editar la configuración generada.",
    "install.step3": "Crea una aplicación y un bot de Discord en el Portal de desarrolladores. En los ajustes del bot, genera el token y activa Server Members Intent y Message Content Intent.",
    "install.portal": "Abrir el Portal de desarrolladores de Discord",
    "install.step4": "Invita el bot al servidor con los ámbitos OAuth2 bot y applications.commands. Concede los permisos indicados a continuación.",
    "install.step5": "En plugins/MCReportPlugin/config.yml, configura el token del bot y los IDs del servidor de Discord, canales, categorías y roles del staff.",
    "install.step6": "Guarda la configuración e inicia el servidor Minecraft. Comprueba en la consola que la conexión a Discord se haya completado correctamente.",
    "install.permissionsTitle": "Permisos del bot", "install.permissions": "Concede Ver canales, Enviar mensajes, Insertar enlaces, Leer el historial de mensajes, Gestionar canales y Gestionar roles. El bot debe poder ver y enviar mensajes en ambos canales configurados y acceder a las categorías de tickets.",
    "install.idsTitle": "IDs de Discord en la configuración", "install.ids": "Activa el Modo desarrollador en Discord (Ajustes de usuario → Avanzado) y usa Copiar ID en el menú del servidor, canal, categoría o rol. Asigna discord.guild-id, discord.report-embed-channel, discord.appeal-channel, discord.report-ticket-category, discord.appeal-ticket-category, discord.staff-roles y discord.admin-role-id a sus IDs correspondientes. Los IDs deben escribirse entre comillas como texto. Si appeal-ticket-category está vacío, también se usará report-ticket-category para las apelaciones.",
    "install.secretTitle": "Protege el token del bot", "install.secret": "Genera o restablece el token en el Portal de desarrolladores y guárdalo únicamente en el config.yml del servidor. No lo compartas, publiques ni subas a un repositorio. Si quedó expuesto, restablécelo de inmediato y actualiza la configuración.",
    "install.support": "Si falla la configuración, comparte en Discord la versión del plugin y el error pertinente de la consola. Nunca incluyas el token del bot.", "install.discord": "Abrir Discord de soporte", "footer.license": "Código abierto para tu comunidad."
  },
  en: {
    "nav.home": "Home", "nav.install": "Installation", "nav.label": "Main navigation", "language.label": "Language", "home.title": "Faster reports, safer servers.",
    "home.lead": "MCReportSystem connects Minecraft server reports to Discord so moderators can act with context and privacy.", "home.install": "View installation", "home.discord": "Questions and issues on Discord",
    "home.featuresTitle": "Everything moderators need", "home.featureReportsTitle": "Private reports", "home.featureReports": "A temporary channel for each report, visible only to authorized staff.",
    "home.featureActionsTitle": "Actions from Discord", "home.featureActions": "Ban, kick, warn and resolve reports with buttons, including player notifications.",
    "home.featureEvidenceTitle": "Useful evidence", "home.featureEvidence": "Review chat logs, inventory, location and the player's skin.", "home.featureBedrockTitle": "Java and Bedrock", "home.featureBedrock": "Geyser and Floodgate support for mixed communities.",
    "home.helpTitle": "Need help?", "home.help": "For questions, bugs or suggestions, join the official Discord server.", "home.joinDiscord": "Join Discord →",
    "install.title": "Installation and configuration", "install.lead": "Install the plugin on Paper or Spigot 1.20.1 with Java 17 or later, then connect a Discord bot.",
    "install.requirementsTitle": "Requirements", "install.requirements": "A Minecraft server running Paper or Spigot 1.20.1, Java 17 or later, and a Discord application with a bot.",
    "install.stepsTitle": "Installation steps", "install.step1": "Download the latest MCReportPlugin JAR from GitHub Releases.", "install.releases": "Open latest release",
    "install.step2": "Stop the server, place the JAR in plugins/, and start the server once. Stop it again before editing the generated configuration.",
    "install.step3": "Create a Discord application and bot in the Developer Portal. In the Bot settings, generate the token and enable Server Members Intent and Message Content Intent.",
    "install.portal": "Open Discord Developer Portal",
    "install.step4": "Invite the bot to your server with the bot and applications.commands OAuth2 scopes. Grant the permissions listed below.",
    "install.step5": "In plugins/MCReportPlugin/config.yml, set the bot token and the IDs for your Discord server, channels, categories, and staff roles.",
    "install.step6": "Save the configuration and start the Minecraft server. Check the console for a successful Discord connection and setup messages.",
    "install.permissionsTitle": "Bot permissions", "install.permissions": "Grant View Channels, Send Messages, Embed Links, Read Message History, Manage Channels, and Manage Roles. The bot must be able to view and send messages in both configured embed channels and access the ticket categories.",
    "install.idsTitle": "Discord IDs in the configuration", "install.ids": "Enable Developer Mode in Discord (User Settings → Advanced), then use Copy ID from the server, channel, category, or role context menu. Set discord.guild-id, discord.report-embed-channel, discord.appeal-channel, discord.report-ticket-category, discord.appeal-ticket-category, discord.staff-roles, and discord.admin-role-id to the matching IDs. IDs must be quoted as strings. If appeal-ticket-category is empty, report-ticket-category is used for appeal tickets too.",
    "install.secretTitle": "Keep your bot token secret", "install.secret": "Generate or reset the token in the Developer Portal, then store it only in the server's config.yml. Do not share it, publish it, or commit it to a repository. If it has been exposed, reset it immediately and update the configuration.",
    "install.support": "If setup fails, share the plugin version and relevant console error on Discord. Never include your bot token.", "install.discord": "Open support Discord", "footer.license": "Open source for your community."
  }
};

translations.pt = {
  "nav.home": "Início", "nav.install": "Instalação", "nav.label": "Navegação principal", "language.label": "Idioma",
  "home.title": "Relatórios mais rápidos, servidores mais seguros.",
  "home.lead": "O MCReportSystem liga os relatórios do seu servidor Minecraft ao Discord para que a equipa de moderação possa agir com contexto e privacidade.",
  "home.install": "Ver instalação", "home.discord": "Dúvidas e problemas no Discord", "home.featuresTitle": "Tudo o que a moderação precisa",
  "home.featureReportsTitle": "Relatórios privados", "home.featureReports": "Um canal temporário por relatório, visível apenas para a equipa autorizada.",
  "home.featureActionsTitle": "Ações no Discord", "home.featureActions": "Banir, expulsar, advertir e resolver relatórios através de botões, com notificações ao jogador.",
  "home.featureEvidenceTitle": "Evidências úteis", "home.featureEvidence": "Consulte logs do chat, inventário, localização e a skin do jogador.",
  "home.featureBedrockTitle": "Java e Bedrock", "home.featureBedrock": "Compatibilidade com Geyser e Floodgate para comunidades mistas.",
  "home.helpTitle": "Precisa de ajuda?", "home.help": "Para dúvidas, erros ou sugestões, entre no servidor oficial do Discord.", "home.joinDiscord": "Entrar no Discord →",
  "install.title": "Instalação e configuração", "install.lead": "Instale o plugin no Paper ou Spigot 1.20.1 com Java 17 ou superior e ligue um bot do Discord.",
  "install.requirementsTitle": "Requisitos", "install.requirements": "Um servidor Minecraft com Paper ou Spigot 1.20.1, Java 17 ou superior e uma aplicação do Discord com um bot.",
  "install.stepsTitle": "Passos de instalação", "install.step1": "Transfira o JAR mais recente do MCReportPlugin em GitHub Releases.", "install.releases": "Abrir a versão mais recente",
  "install.step2": "Pare o servidor, coloque o JAR em plugins/ e inicie o servidor uma vez. Volte a pará-lo antes de editar a configuração gerada.",
  "install.step3": "Crie uma aplicação e um bot no Portal de Programadores do Discord. Nas definições do bot, gere o token e ative Server Members Intent e Message Content Intent.",
  "install.portal": "Abrir o Portal de Programadores do Discord",
  "install.step4": "Convide o bot para o servidor com os âmbitos OAuth2 bot e applications.commands. Conceda as permissões indicadas abaixo.",
  "install.step5": "Em plugins/MCReportPlugin/config.yml, defina o token do bot e os IDs do servidor Discord, canais, categorias e cargos da equipa.",
  "install.step6": "Guarde a configuração e inicie o servidor Minecraft. Confirme na consola que a ligação ao Discord foi bem-sucedida.",
  "install.permissionsTitle": "Permissões do bot", "install.permissions": "Conceda Ver canais, Enviar mensagens, Incorporar links, Ler o histórico de mensagens, Gerir canais e Gerir cargos. O bot tem de poder ver e enviar mensagens nos dois canais configurados e aceder às categorias dos tickets.",
  "install.idsTitle": "IDs do Discord na configuração", "install.ids": "Ative o Modo de programador no Discord (Definições de utilizador → Avançado) e use Copiar ID no menu do servidor, canal, categoria ou cargo. Defina discord.guild-id, discord.report-embed-channel, discord.appeal-channel, discord.report-ticket-category, discord.appeal-ticket-category, discord.staff-roles e discord.admin-role-id com os IDs correspondentes. Os IDs devem ser escritos entre aspas como texto. Se appeal-ticket-category estiver vazio, report-ticket-category também será usado para tickets de recurso.",
  "install.secretTitle": "Proteja o token do bot", "install.secret": "Gere ou redefina o token no Portal de Programadores e guarde-o apenas no config.yml do servidor. Não o partilhe, publique ou envie para um repositório. Se tiver sido exposto, redefina-o imediatamente e atualize a configuração.",
  "install.support": "Se a configuração falhar, partilhe no Discord a versão do plugin e o erro relevante da consola. Nunca inclua o token do bot.", "install.discord": "Abrir Discord de suporte", "footer.license": "Código aberto para a sua comunidade."
};
translations.fr = {
  "nav.home": "Accueil", "nav.install": "Installation", "nav.label": "Navigation principale", "language.label": "Langue",
  "home.title": "Des signalements plus rapides, des serveurs plus sûrs.", "home.lead": "MCReportSystem relie les signalements de votre serveur Minecraft à Discord pour permettre à la modération d'agir avec contexte et confidentialité.",
  "home.install": "Voir l'installation", "home.discord": "Questions et problèmes sur Discord", "home.featuresTitle": "Tout ce qu'il faut pour modérer",
  "home.featureReportsTitle": "Signalements privés", "home.featureReports": "Un canal temporaire par signalement, visible uniquement par le staff autorisé.", "home.featureActionsTitle": "Actions depuis Discord", "home.featureActions": "Bannir, expulser, avertir et résoudre avec des boutons, avec notification du joueur.",
  "home.featureEvidenceTitle": "Preuves utiles", "home.featureEvidence": "Consultez les logs du chat, l'inventaire, la position et le skin du joueur.", "home.featureBedrockTitle": "Java et Bedrock", "home.featureBedrock": "Prise en charge de Geyser et Floodgate pour les communautés mixtes.",
  "home.helpTitle": "Besoin d'aide ?", "home.help": "Pour toute question, erreur ou suggestion, rejoignez le serveur Discord officiel.", "home.joinDiscord": "Rejoindre Discord →",
  "install.title": "Installation et configuration", "install.lead": "Installez le plugin sur Paper ou Spigot 1.20.1 avec Java 17 ou une version ultérieure, puis connectez un bot Discord.",
  "install.requirementsTitle": "Prérequis", "install.requirements": "Un serveur Minecraft sous Paper ou Spigot 1.20.1, Java 17 ou une version ultérieure et une application Discord avec un bot.",
  "install.stepsTitle": "Étapes d'installation", "install.step1": "Téléchargez le dernier JAR de MCReportPlugin depuis GitHub Releases.", "install.releases": "Ouvrir la dernière version",
  "install.step2": "Arrêtez le serveur, placez le JAR dans plugins/ et démarrez le serveur une fois. Arrêtez-le de nouveau avant de modifier la configuration générée.",
  "install.step3": "Créez une application et un bot dans le portail développeur Discord. Dans les paramètres du bot, générez le token et activez Server Members Intent et Message Content Intent.",
  "install.portal": "Ouvrir le portail développeur Discord",
  "install.step4": "Invitez le bot sur votre serveur avec les scopes OAuth2 bot et applications.commands. Accordez les autorisations ci-dessous.",
  "install.step5": "Dans plugins/MCReportPlugin/config.yml, renseignez le token du bot et les IDs du serveur Discord, des salons, catégories et rôles du staff.",
  "install.step6": "Enregistrez la configuration et démarrez le serveur Minecraft. Vérifiez dans la console que la connexion à Discord a réussi.",
  "install.permissionsTitle": "Autorisations du bot", "install.permissions": "Accordez Voir les salons, Envoyer des messages, Intégrer des liens, Voir l'historique des messages, Gérer les salons et Gérer les rôles. Le bot doit pouvoir voir et écrire dans les deux salons configurés et accéder aux catégories de tickets.",
  "install.idsTitle": "IDs Discord dans la configuration", "install.ids": "Activez le mode développeur dans Discord (Paramètres utilisateur → Avancé), puis utilisez Copier l'identifiant dans le menu du serveur, salon, catégorie ou rôle. Renseignez discord.guild-id, discord.report-embed-channel, discord.appeal-channel, discord.report-ticket-category, discord.appeal-ticket-category, discord.staff-roles et discord.admin-role-id avec les IDs correspondants. Les IDs doivent être entre guillemets. Si appeal-ticket-category est vide, report-ticket-category est aussi utilisé pour les tickets d'appel.",
  "install.secretTitle": "Protégez le token du bot", "install.secret": "Générez ou réinitialisez le token dans le portail développeur, puis stockez-le uniquement dans le config.yml du serveur. Ne le partagez pas, ne le publiez pas et ne l'ajoutez pas à un dépôt. S'il a été exposé, réinitialisez-le immédiatement et mettez à jour la configuration.",
  "install.support": "En cas d'échec, partagez sur Discord la version du plugin et l'erreur pertinente de la console. N'incluez jamais le token du bot.", "install.discord": "Ouvrir le Discord d'assistance", "footer.license": "Open source pour votre communauté."
};
translations.de = {
  "nav.home": "Startseite", "nav.install": "Installation", "nav.label": "Hauptnavigation", "language.label": "Sprache",
  "home.title": "Schnellere Meldungen, sicherere Server.", "home.lead": "MCReportSystem verbindet Minecraft-Servermeldungen mit Discord, damit Moderatoren mit Kontext und Privatsphäre handeln können.",
  "home.install": "Installation ansehen", "home.discord": "Fragen und Probleme auf Discord", "home.featuresTitle": "Alles für die Moderation",
  "home.featureReportsTitle": "Private Meldungen", "home.featureReports": "Ein temporärer Kanal pro Meldung, nur für autorisierte Teammitglieder sichtbar.", "home.featureActionsTitle": "Aktionen über Discord", "home.featureActions": "Bannen, kicken, verwarnen und erledigen per Button, inklusive Spielerbenachrichtigung.",
  "home.featureEvidenceTitle": "Nützliche Beweise", "home.featureEvidence": "Chat-Logs, Inventar, Standort und den Spielerskin prüfen.", "home.featureBedrockTitle": "Java und Bedrock", "home.featureBedrock": "Geyser- und Floodgate-Unterstützung für gemischte Communities.",
  "home.helpTitle": "Brauchst du Hilfe?", "home.help": "Bei Fragen, Fehlern oder Vorschlägen kannst du dem offiziellen Discord-Server beitreten.", "home.joinDiscord": "Discord beitreten →",
  "install.title": "Installation und Konfiguration", "install.lead": "Installiere das Plugin mit Java 17 oder höher auf Paper oder Spigot 1.20.1 und verbinde anschließend einen Discord-Bot.",
  "install.requirementsTitle": "Voraussetzungen", "install.requirements": "Ein Minecraft-Server mit Paper oder Spigot 1.20.1, Java 17 oder höher und eine Discord-Anwendung mit Bot.",
  "install.stepsTitle": "Installationsschritte", "install.step1": "Lade das neueste MCReportPlugin-JAR von GitHub Releases herunter.", "install.releases": "Neueste Version öffnen",
  "install.step2": "Stoppe den Server, lege das JAR in plugins/ und starte den Server einmal. Stoppe ihn erneut, bevor du die erzeugte Konfiguration bearbeitest.",
  "install.step3": "Erstelle im Discord-Entwicklerportal eine Anwendung und einen Bot. Erzeuge in den Bot-Einstellungen das Token und aktiviere Server Members Intent und Message Content Intent.",
  "install.portal": "Discord-Entwicklerportal öffnen",
  "install.step4": "Lade den Bot mit den OAuth2-Scopes bot und applications.commands auf deinen Server ein. Erteile die unten genannten Berechtigungen.",
  "install.step5": "Trage in plugins/MCReportPlugin/config.yml das Bot-Token und die IDs deines Discord-Servers, der Kanäle, Kategorien und Teamrollen ein.",
  "install.step6": "Speichere die Konfiguration und starte den Minecraft-Server. Prüfe in der Konsole, ob die Discord-Verbindung erfolgreich hergestellt wurde.",
  "install.permissionsTitle": "Bot-Berechtigungen", "install.permissions": "Erteile Kanäle ansehen, Nachrichten senden, Links einbetten, Nachrichtenverlauf ansehen, Kanäle verwalten und Rollen verwalten. Der Bot muss beide konfigurierten Embed-Kanäle sehen und beschreiben sowie auf die Ticket-Kategorien zugreifen können.",
  "install.idsTitle": "Discord-IDs in der Konfiguration", "install.ids": "Aktiviere den Entwicklermodus in Discord (Benutzereinstellungen → Erweitert) und wähle im Kontextmenü von Server, Kanal, Kategorie oder Rolle ID kopieren. Trage die passenden IDs für discord.guild-id, discord.report-embed-channel, discord.appeal-channel, discord.report-ticket-category, discord.appeal-ticket-category, discord.staff-roles und discord.admin-role-id ein. IDs müssen als Zeichenfolgen in Anführungszeichen stehen. Ist appeal-ticket-category leer, wird für Einspruchstickets ebenfalls report-ticket-category verwendet.",
  "install.secretTitle": "Bot-Token geheim halten", "install.secret": "Erzeuge oder setze das Token im Entwicklerportal zurück und speichere es nur in der config.yml des Servers. Teile oder veröffentliche es nicht und committe es nicht in ein Repository. Wurde es offengelegt, setze es sofort zurück und aktualisiere die Konfiguration.",
  "install.support": "Falls die Einrichtung fehlschlägt, teile auf Discord die Plugin-Version und den relevanten Konsolenfehler. Füge niemals dein Bot-Token bei.", "install.discord": "Support-Discord öffnen", "footer.license": "Open Source für deine Community."
};
translations.it = {
  "nav.home": "Home", "nav.install": "Installazione", "nav.label": "Navigazione principale", "language.label": "Lingua",
  "home.title": "Segnalazioni più rapide, server più sicuri.", "home.lead": "MCReportSystem collega le segnalazioni del server Minecraft a Discord, così lo staff può agire con contesto e privacy.",
  "home.install": "Vedi installazione", "home.discord": "Domande e problemi su Discord", "home.featuresTitle": "Tutto ciò che serve per moderare",
  "home.featureReportsTitle": "Segnalazioni private", "home.featureReports": "Un canale temporaneo per ogni segnalazione, visibile solo allo staff autorizzato.", "home.featureActionsTitle": "Azioni da Discord", "home.featureActions": "Ban, kick, warn e risoluzione tramite pulsanti, con notifica al giocatore.",
  "home.featureEvidenceTitle": "Prove utili", "home.featureEvidence": "Controlla log della chat, inventario, posizione e skin del giocatore.", "home.featureBedrockTitle": "Java e Bedrock", "home.featureBedrock": "Supporto Geyser e Floodgate per community miste.",
  "home.helpTitle": "Hai bisogno di aiuto?", "home.help": "Per domande, bug o suggerimenti, entra nel server Discord ufficiale.", "home.joinDiscord": "Entra in Discord →",
  "install.title": "Installazione e configurazione", "install.lead": "Installa il plugin su Paper o Spigot 1.20.1 con Java 17 o versioni successive e collega un bot Discord.",
  "install.requirementsTitle": "Requisiti", "install.requirements": "Un server Minecraft con Paper o Spigot 1.20.1, Java 17 o versioni successive e un'applicazione Discord con un bot.",
  "install.stepsTitle": "Procedura di installazione", "install.step1": "Scarica il JAR più recente di MCReportPlugin da GitHub Releases.", "install.releases": "Apri l'ultima versione",
  "install.step2": "Arresta il server, inserisci il JAR nella cartella plugins/ e avvia il server una volta. Arrestalo di nuovo prima di modificare la configurazione generata.",
  "install.step3": "Crea un'applicazione e un bot nel Portale sviluppatori Discord. Nelle impostazioni del bot, genera il token e attiva Server Members Intent e Message Content Intent.",
  "install.portal": "Apri il Portale sviluppatori Discord",
  "install.step4": "Invita il bot nel server con gli scope OAuth2 bot e applications.commands. Concedi le autorizzazioni elencate qui sotto.",
  "install.step5": "In plugins/MCReportPlugin/config.yml, imposta il token del bot e gli ID del server Discord, dei canali, delle categorie e dei ruoli dello staff.",
  "install.step6": "Salva la configurazione e avvia il server Minecraft. Controlla nella console che la connessione a Discord sia riuscita.",
  "install.permissionsTitle": "Autorizzazioni del bot", "install.permissions": "Concedi Visualizzare i canali, Inviare messaggi, Incorporare link, Leggere la cronologia dei messaggi, Gestire i canali e Gestire i ruoli. Il bot deve poter visualizzare e inviare messaggi in entrambi i canali configurati e accedere alle categorie dei ticket.",
  "install.idsTitle": "ID Discord nella configurazione", "install.ids": "Attiva la Modalità sviluppatore in Discord (Impostazioni utente → Avanzate), poi usa Copia ID dal menu contestuale del server, canale, categoria o ruolo. Inserisci gli ID corrispondenti in discord.guild-id, discord.report-embed-channel, discord.appeal-channel, discord.report-ticket-category, discord.appeal-ticket-category, discord.staff-roles e discord.admin-role-id. Gli ID devono essere racchiusi tra virgolette. Se appeal-ticket-category è vuoto, anche i ticket di appello useranno report-ticket-category.",
  "install.secretTitle": "Proteggi il token del bot", "install.secret": "Genera o reimposta il token nel Portale sviluppatori e conservalo solo nel config.yml del server. Non condividerlo, pubblicarlo o inserirlo in un repository. Se è stato esposto, reimpostalo subito e aggiorna la configurazione.",
  "install.support": "Se la configurazione non riesce, condividi su Discord la versione del plugin e l'errore pertinente della console. Non includere mai il token del bot.", "install.discord": "Apri Discord di supporto", "footer.license": "Open source per la tua community."
};
translations.nl = {
  "nav.home": "Home", "nav.install": "Installatie", "nav.label": "Hoofdnavigatie", "language.label": "Taal",
  "home.title": "Snellere meldingen, veiligere servers.", "home.lead": "MCReportSystem koppelt Minecraft-servermeldingen aan Discord, zodat moderators met context en privacy kunnen handelen.",
  "home.install": "Installatie bekijken", "home.discord": "Vragen en problemen op Discord", "home.featuresTitle": "Alles wat moderators nodig hebben",
  "home.featureReportsTitle": "Privémeldingen", "home.featureReports": "Een tijdelijk kanaal per melding, alleen zichtbaar voor geautoriseerde medewerkers.", "home.featureActionsTitle": "Acties vanuit Discord", "home.featureActions": "Bannen, kicken, waarschuwen en oplossen met knoppen, inclusief meldingen aan spelers.",
  "home.featureEvidenceTitle": "Bruikbaar bewijs", "home.featureEvidence": "Bekijk chatlogs, inventaris, locatie en de skin van de speler.", "home.featureBedrockTitle": "Java en Bedrock", "home.featureBedrock": "Ondersteuning voor Geyser en Floodgate voor gemengde communities.",
  "home.helpTitle": "Hulp nodig?", "home.help": "Ga voor vragen, fouten of suggesties naar de officiële Discord-server.", "home.joinDiscord": "Naar Discord →",
  "install.title": "Installatie en configuratie", "install.lead": "Installeer de plugin op Paper of Spigot 1.20.1 met Java 17 of hoger en koppel een Discord-bot.",
  "install.requirementsTitle": "Vereisten", "install.requirements": "Een Minecraft-server met Paper of Spigot 1.20.1, Java 17 of hoger en een Discord-applicatie met een bot.",
  "install.stepsTitle": "Installatiestappen", "install.step1": "Download de nieuwste MCReportPlugin-JAR via GitHub Releases.", "install.releases": "Nieuwste release openen",
  "install.step2": "Stop de server, plaats de JAR in plugins/ en start de server één keer. Stop hem opnieuw voordat je de aangemaakte configuratie bewerkt.",
  "install.step3": "Maak een Discord-applicatie en -bot aan in het Developer Portal. Genereer bij de botinstellingen het token en schakel Server Members Intent en Message Content Intent in.",
  "install.portal": "Discord Developer Portal openen",
  "install.step4": "Nodig de bot uit met de OAuth2-scopes bot en applications.commands. Ken de onderstaande rechten toe.",
  "install.step5": "Stel in plugins/MCReportPlugin/config.yml de bot-token en de ID's van je Discord-server, kanalen, categorieën en staffrollen in.",
  "install.step6": "Sla de configuratie op en start de Minecraft-server. Controleer in de console of de Discord-verbinding is gelukt.",
  "install.permissionsTitle": "Botrechten", "install.permissions": "Geef de rechten Kanalen bekijken, Berichten sturen, Links insluiten, Berichtgeschiedenis lezen, Kanalen beheren en Rollen beheren. De bot moet beide ingestelde embedkanalen kunnen bekijken en berichten sturen, en toegang hebben tot de ticketcategorieën.",
  "install.idsTitle": "Discord-ID's in de configuratie", "install.ids": "Schakel de ontwikkelaarsmodus in Discord in (Gebruikersinstellingen → Geavanceerd) en kies ID kopiëren in het contextmenu van de server, het kanaal, de categorie of de rol. Vul de bijbehorende ID's in bij discord.guild-id, discord.report-embed-channel, discord.appeal-channel, discord.report-ticket-category, discord.appeal-ticket-category, discord.staff-roles en discord.admin-role-id. Zet ID's tussen aanhalingstekens als tekst. Als appeal-ticket-category leeg is, wordt report-ticket-category ook gebruikt voor appeal-tickets.",
  "install.secretTitle": "Houd je bot-token geheim", "install.secret": "Genereer of reset het token in het Developer Portal en bewaar het alleen in de config.yml op de server. Deel of publiceer het niet en commit het niet naar een repository. Is het blootgesteld, reset het dan direct en werk de configuratie bij.",
  "install.support": "Lukt de configuratie niet? Deel de pluginversie en relevante consolefout op Discord. Voeg nooit je bot-token toe.", "install.discord": "Support-Discord openen", "footer.license": "Open source voor jouw community."
};

const select = document.querySelector("#language-select");
const locale = new URLSearchParams(window.location.search).get("lang") || localStorage.getItem("mcreport-language") || "en";

function applyLanguage(language) {
  const selected = translations[language] ? language : "en";
  document.documentElement.lang = selected;
  document.querySelectorAll("[data-i18n-aria-label]").forEach((element) => {
    const value = translations[selected][element.dataset.i18nAriaLabel];
    if (value) element.setAttribute("aria-label", value);
  });
  document.querySelectorAll("[data-i18n]").forEach((element) => {
    const value = translations[selected][element.dataset.i18n];
    if (value) element.textContent = value;
  });
  if (select) select.value = selected;
  localStorage.setItem("mcreport-language", selected);
}

if (select) select.addEventListener("change", () => applyLanguage(select.value));
applyLanguage(locale);
