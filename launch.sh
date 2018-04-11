#!/bin/bash
mvn package && java -jar target/rufus-1.0-SNAPSHOT.jar db2 migrate config.yml --migrations src/main/resources/migrationshsqldb.xml && java -jar target/rufus-1.0-SNAPSHOT.jar server config.yml

