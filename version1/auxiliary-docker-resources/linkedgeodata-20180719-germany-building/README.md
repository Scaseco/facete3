## sparql endpoint via virtuoso for linkedgeodata buildings data

* Use the `make docker-image` to create the docker image (TODO implement this). This will download a preloaded virtuoso.db file, start the server once to create all initial files (using docker-compose), and eventually create a new image with the virtuoso.db file and any otherwise generated files in `target/data`.

##Build and run
```bash
docker build -t linkedgeodata-20180719-germany-building .
docker run -it linkedgeodata-20180719-germany-building
```

Deployment
```
docker tag linkedgeodata-20180719-germany-building git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/linkedgeodata-20180719-germany-building
docker login git.project-hobbit.eu:4567
docker push git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/linkedgeodata-20180719-germany-building
```


