# BetterAlarm - Application d'Alarme Android

Une application d'alarme complète pour Android avec des fonctionnalités avancées comme les sous-alarmes et la répétition personnalisable. Inspirée de l'application Horloge d'OxygenOS 13 pour OnePlus Nord CE 5G.

## Fonctionnalités

### 🎯 Fonctionnalités principales
- **Création, modification et suppression d'alarmes**
- **Sous-alarmes** : Ajoutez des alarmes supplémentaires avec un décalage configurable (+5min, +6min, etc.)
- **Répétition flexible** :
  - Une fois (par défaut)
  - Tous les jours
  - Jours de la semaine (sélection multiple)
  - Dates spécifiques dans le calendrier
- **Personnalisation** : Label, vibration, durée de snooze

### 🔔 Gestion des alarmes
- Notifications en premier plan avec son et vibration
- Boutons d'action : Arrêter et Répéter (Snooze)
- Réveil de l'appareil si éteint ou en veille

## Architecture

- **MVVM** (Model-View-ViewModel)
- **Room Database** pour la persistance
- **AlarmManager** pour la gestion des alarmes
- **Foreground Service** pour les alarmes en arrière-plan

## Installation

### Prérequis
- Android Studio 2023.2 ou supérieur
- SDK Android 34 (targetSdk)
- Minimum SDK 24 (Android 7.0)

### Configuration
1. Clonez le dépôt : `git clone https://github.com/Baptou-33/BetterAlarm.git`
2. Ouvrez dans Android Studio
3. Synchronisez les dépendances Gradle
4. Exécutez sur un appareil ou émulateur

## Permissions requises
- POST_NOTIFICATIONS (Android 13+)
- SCHEDULE_EXACT_ALARM (Android 12+)
- VIBRATE
- WAKE_LOCK
- FOREGROUND_SERVICE

## Utilisation

### Créer une alarme
1. Appuyez sur le bouton + (FAB)
2. Définissez l'heure
3. Donnez un nom
4. Configurez la répétition et les sous-alarmes
5. Sauvegardez

### Sous-alarmes
- Ajoutez des sous-alarmes avec décalage en minutes
- Chaque sous-alarme peut être activée/désactivée individuellement

## Contribution
Les contributions sont les bienvenues !

## Licence
MIT

---
Créé pour OnePlus Nord CE 5G avec OxygenOS 13
