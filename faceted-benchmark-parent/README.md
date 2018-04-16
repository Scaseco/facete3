Before running mvn install

Make sure the following commands succeeded:
```
docker pull tenforce/virtuoso

cd ../podigg 
docker build -t podigg .
```


== Recent changes that potentially broke other integrations
* In ConfigRabbitMqConnectionFactory - setting vhost to "default" apparently fails in the platform.  local unit tests work either way, but this might break when trying to connect to the dedicated qpid docker
