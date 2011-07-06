#!/bin/bash
#Uso: sign.sh <archivo_o_carpeta> <clave_key_store> <fichero_ks> <alias_ks>
PASS="$2"
KS=$(readlink -f "$3")
ALIAS="$4"
echo "Usando archivo de claves $KS"
if [ ! -f "$KS" ]; then
echo "No existe el archivo de claves se va a crear."
echo "A continuación se solicitará una clave."
echo "SE DEBE USAR $PASS"
keytool -genkey -alias $ALIAS -keypass $PASS -validity 10000 -keystore $KS
fi

if [ -f "$KS" ]; then
	if [ -d "$1" ]; then
		echo "Firmando archivos jar de directorio $1"
		cd $1
		for file in `dir -d *.jar` ; do
			echo "Firmando $file"
			jarsigner -keystore $KS -keypass $PASS -storepass $PASS $file $ALIAS
		done
	fi
	if [ -f "$1" ]; then
		echo "Firmando $1"
		jarsigner -keystore $KS -keypass $PASS -storepass $PASS $1 $ALIAS
	fi
fi
