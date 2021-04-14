#!/bin/bash

if [[ ! -e Student.json ]]; then 
	touch Student.json
fi

if [[ ! -e Progetti.json ]]; then 
	touch Progetti.json
fi

if [[ ! -e Indirizzi.json ]]; then 
	touch Indirizzi.json
fi

javac -cp out/production/Progetto:lib/gson-2.8.6.jar -d out/production/Progetto src/Progettoreti/callback/*.java src/Progettoreti/registrazione/*.java src/Progettoreti/server/*.java src/Progettoreti/client/*.java

java -cp out/production/Progetto:lib/gson-2.8.6.jar Progettoreti.server.MainClass



