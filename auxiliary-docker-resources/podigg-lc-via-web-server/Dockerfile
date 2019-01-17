#FROM httpd:2.4
#FROM mhart/alpine-node:9.2.0
FROM node:6.12.1

VOLUME /srv/httpd/

RUN npm install http-server -g

# Install podigg
RUN git clone https://github.com/PoDiGG/podigg-lc.git /podigg

WORKDIR /podigg
RUN npm install

WORKDIR bin

ADD start.sh .
ENTRYPOINT ["./start.sh"]


