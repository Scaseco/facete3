# Dockerization project for Hobbit benchmarks
This project dockerizes the faceted browsing benchmark, but it is also intended to serve as a best-practice template for dockerization of other benchmarks.

It uses [Spotify's Dockerfile Maven Plugin](https://github.com/spotify/dockerfile-maven) to provide you with these features:


Note: The virtuoso (docker) is currently needed by the task generator for legacy reasons - the intention is to switch to an embedded RDF store for this purpose.


## Build / Features

* Building docker containers for all benchmark components with maven
```bash
mvn clean install
```

* Deploy the image to a repository - such as the hobbit platform one. Requires write access to that repository, with your credentials available in a typical `<server>` section in your [~/.m2/settings.xml](~/.m2/settings.xml) configuration.

```bash
mvn deploy
```

* The parent [pom.xml](pom.xml) holds docker-related variables which are referenced in the child modules and their `Dockerfile` templates.
Packaging a typical benchmark components written in Java only requires the docker images to include the appropriate jar files and the Dockerfile to launch the appropriate Main class. 
Hence, by default, the Dockerfile template for all components is the same.


## Quick Links to the configuration files

### Benchmark specific image configurations

* [pom.xml](pom.xml)
* [benchmark-controller/pom.xml](benchmark-controller/pom.xml)
* [benchmark-controller/Dockerfile](benchmark-controller/Dockerfile)
* [data-generator/pom.xml](data-generator/pom.xml)
* [data-generator/Dockerfile](data-generator/Dockerfile)
* [evaluation-module/pom.xml](evaluation-module/pom.xml)
* [evaluation-module/Dockerfile](evaluation-module/Dockerfile)

### Image configurations that could be reused across benchmarks
* [evaluation-storage/pom.xml](evaluation-storage/pom.xml)
* [evaluation-storage/Dockerfile](evaluation-storage/Dockerfile)
* [docker-service-manager-server/pom.xml](docker-service-manager-server/pom.xml)
* [docker-service-manager-server/Dockerfile](docker-service-manager-server/Dockerfile)
* [qpid-server/pom.xml](qpid-server/pom.xml)
* [qpid-server/Dockerfile](qpid-server/Dockerfile)


## Adapting this project layout

* Copy this whole folder including all subfolders
* Adjust the group/artifact/version of the all modules
* Adjust the `MAINTAINER` field in the Dockerfiles
* The parent `pom.xml` aggregates nearly all configuration options, such as all image names for the docker containers to build
* Typically, in the component `pom.xml`s (e.g. benchmark-controller, datagenerator, ...) you only need to adjust the dependency (or dependencies) to your benchmark project. Of course you can adjust anything to your needs.



### Tooling

You can search replace over all `pom.xml` with this handy command line (if there is a maven plugin for that, please let me know :)

```bash
find ~/project -name "pom.xml" | xargs sed -i 's/<groupId>org.old.groupId<\/groupId>/<groupId>org.new.groupId<\/groupId>/g'
```

* Adjusting versions is a matter of adapting this command to your needs
```bash
mvn versions:set -DnewVersion=1.0.0-SNAPSHOT`
```


