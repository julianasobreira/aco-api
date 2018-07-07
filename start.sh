#!/bin/bash
clear
export DB_URL="mysql://localhost:3306/aco"
export DB_USER="root"
export DB_PWD="root"
java -cp target/classes:target/dependency/* com.aco.heroku.Main