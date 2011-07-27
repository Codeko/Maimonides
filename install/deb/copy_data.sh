#!/bin/bash
echo "Eliminando carpeta de datos..."
rm -R data
echo "Creando carpeta de datos..."
mkdir data
echo "Copiando datos..."
cp -R ../../dist/lib data/
cp  ../../dist/Maimonides.jar data/
cp  maimonides.svg data/
cp  ../../license.txt data/
