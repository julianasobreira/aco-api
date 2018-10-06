#!/bin/bash
clear
export DB_DATABASE="localhost"
export DB_USER="root"
export DB_PWD="root"
export SECRET_KEY="FRASE_SEGREDO"
mvn clean compile
mvn exec:java
# java -cp target/classes:target/dependency/* com.aco.heroku.Main