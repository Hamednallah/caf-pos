#!/bin/bash
mvn -DskipTests package
java -jar target/cafeteria-pos-1.0.0-jar-with-dependencies.jar
