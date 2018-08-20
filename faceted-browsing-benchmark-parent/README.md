Before running mvn install

Make sure the following commands succeeded:
```
docker pull tenforce/virtuoso

cd ../podigg 
docker build -t podigg .
```


== Important classes / files

=== Tailoring of the SDK to the Facted Browsing Benchmark
* Virtual image registry [org.hobbit.benchmark.faceted_browsing.config.ConfigVirtualDockerServiceFactory](faceted-benchmark-core/src/main/java/org/hobbit/benchmark/faceted_browsing/config/ConfigVirtualDockerServiceFactory.java)
* Service configuration [org.hobbit.benchmark.faceted_browsing.config.ConfigBenchmarkControllerFacetedBrowsingServices](faceted-benchmark-core/src/main/java/org/hobbit/benchmark/faceted_browsing/config/ConfigBenchmarkControllerFacetedBrowsingServices.java)


== Recent changes that potentially broke other integrations
* In ConfigRabbitMqConnectionFactory - setting vhost to "default" apparently fails in the platform.  local unit tests work either way, but this might break when trying to connect to the dedicated qpid docker
