# Prototype for scheduling conversion of git repositories to rdf files

## Dependency notice

In order to remove duplicate database jpa definitions, a shared database commons project was introduced, which is used
in all projects, that share the mentioned jpa entities and logic.
The project can be found under: https://github.com/git2RDFLab/database-shared-common

In order to compile this project you have to pull the shared database common project and install the maven artifact locally
(via: `mvn clean install`) as dependency, so this project can find the necessary dependency.

The database shared common dependency is already included in this project as a dependency in the pom with

```
<dependency>
	<groupId>de.leipzig.htwk.gitrdf.database</groupId>
	<artifactId>common</artifactId>
	<version>${de.leipzig.htwk.gitrdf.database.common.version}</version>
</dependency>
```

## Spring Initializr Template
https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.2.2&packaging=jar&jvmVersion=21&groupId=de.leipzig.htwk.gitrdf&artifactId=worker&name=worker&description=Archetype%20project%20for%20HTWK%20Leipzig%20-%20Project%20to%20transform%20git%20to%20RDF&packageName=de.leipzig.htwk.gitrdf.worker&dependencies=lombok,devtools,data-jpa,postgresql,testcontainers,integration

## New Spring Initializr Template
https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.2.2&packaging=jar&jvmVersion=21&groupId=de.leipzig.htwk.gitrdf&artifactId=worker&name=worker&description=Archetype%20project%20for%20HTWK%20Leipzig%20-%20Project%20to%20transform%20git%20to%20RDF&packageName=de.leipzig.htwk.gitrdf.worker&dependencies=lombok,devtools,data-jpa,postgresql,testcontainers,integration,flyway
