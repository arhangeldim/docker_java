FROM maven:3.5-jdk-8

WORKDIR /usr/src/mymaven
CMD ["mvn", "exec:java"]