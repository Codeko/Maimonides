#!/bin/bash
if [ -d "lib" ]; then
	echo "Eliminando directorio lib..."
	rm -R lib/
fi
echo "Copiando directorio lib..."
cp -R ../../dist/lib lib/
echo "Firmando directorio..."
./firmar.sh lib
