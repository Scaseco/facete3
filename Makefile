CWD = $(shell pwd)

POM = -f sansa-bom/pom.xml

# Maven Clean Install Skip ; skip tests, javadoc, scaladoc, etc
MS = mvn -DskipTests -Dmaven.javadoc.skip=true -Dskip
MCCS = $(MS) clean compile
MCIS = $(MS) clean install

# Source: https://stackoverflow.com/questions/4219255/how-do-you-get-the-list-of-targets-in-a-makefile
.PHONY: help
help:  ## Show these help instructions
	@sed -rn 's/^([a-zA-Z_-]+):.*?## (.*)$$/"\1" "\2"/p' < $(MAKEFILE_LIST) | xargs printf "make %-20s# %s\n"

mcis: ## mvn skip clean install (minimal build of all modules) - Passing args:  make mcis ARGS="-X"
	$(MCIS) $(POM) $(ARGS)


vaadin-production: ## Build facete-vaadin in production mode
	$(MS) -Pproduction -pl :facete3-app-vaadin clean install


vaadin-docker: ## Build vaadin docker image
	mvn -pl :facete3-pkg-app-docker-web jib:dockerBuild

run-vaadin-docker: ## Run the vaadin docker image
	docker run -it -p8000:8000 aklakan/facete3-web

