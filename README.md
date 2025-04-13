# Groupe 2 - Client Lourd Java 
POUPARD Thomas, VIGNAUD Célian, TA Van Anh

## Description 
Scrapper pour chercher des évènements en fonction du type d'évènement et de la ville. 

## Lancer JavaFx
mvn clean

mvn javafx:run -f pom.xml

## Fonctionnalités disponibles
- Scraping d’événements depuis plusieurs sites (ex : Feverup, Eventbrite…)
- Récupération des éléments : titre, date, lieu, description, lien, etc.
- Adaptation à des structures HTML différentes selon l'URL/source
- Bouton "En savoir plus" pour chaque événement, ouvrant le lien dans un navigateur
- Export des données scrapped dans un fichier Excel. 
- Gestion du thème général de l'application 
- Ajout d'un plugin 
