#!/bin/bash
docker run -it --rm \
-p 8080:8080 \
-p 8094:8094 \
-p 8095:8095 \
--name servlet \
--env-file ./env.list \
-v "$PWD"/pom.xml:/usr/src/mymaven/pom.xml -v "$HOME/.m2":/root/.m2 -v "$PWD/target:/usr/src/mymaven/target" \
-w /usr/src/mymaven \
--network=cassandra_default \
maven/mymaven:1
