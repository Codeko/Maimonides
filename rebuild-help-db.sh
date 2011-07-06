#!/bin/bash
#Reconstruye la base de datos de la ayuda de la aplicacion
javahelp=$(readlink -f "lib/jhelp/jhall.jar")
if [ -f $javahelp ];
then
    cd src/com/codeko/apps/maimonides/ayuda
    java -classpath $javahelp com.sun.java.help.search.Indexer -verbose html
else
   echo "No existe el archivo: $javahelp"
fi



