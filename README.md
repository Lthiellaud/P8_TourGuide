# P8_TourGuide
## Trip Master - Tour Guide
### Description

TourGuide est une application disponible sur mobile et PC, destinée à toutes les personnes qui aiment voyager. 
Elle permet aux utilisateurs de voir quelles sont les attractions touristiques à proximité de leur position et d’obtenir, via des points de récompense, des réductions sur les séjours à l’hôtel ainsi que sur les billets pour différents spectacles grâce à tout un réseau de partenaires. 


### Solutions techniques
-   `Langage JAVA` (version 1.8)
-   `Framework Spring` (version v5.3.4) – Starters utilisés  :
    *   Spring Boot (version 2.1.6.RELEASE)
    *   Gradle (version 4.8.1)
    *   Spring Web
    *   Spring Cloud Routing (Version Greenwich.SR6) => OpenFeign
-   `Docker`
-   `images DockerHub` (version 8.0.22) :
    *   [th71/gps-microservice - Docker Image | Docker Hub](https://registry.hub.docker.com/r/lth71/gps-microservice)
    *   [lth71/trippricer-microservice - Docker Image | Docker Hub](https://registry.hub.docker.com/r/lth71/trippricer-microservice)
    *   [lth71/reward-microservice - Docker Image | Docker Hub](https://registry.hub.docker.com/r/lth71/reward-microservice)

### Documentation
-   Schéma d'architecture
![Schéma d'architecture](https://github.com/Lthiellaud/P8_TourGuide/blob/develop/Architecture_TourGuide_App.png)
