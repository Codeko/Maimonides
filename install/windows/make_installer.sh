#!/bin/bash
LAUNCHER=$(readlink -f "maimonides.exe")
if [ ! -f "$KS" ]; then
echo "No existe el lanzador $LAUNCHER."
echo "Creando lanzador"
./make_launcher.sh
fi
echo "Creando instalador"
wine ~/.wine/drive_c/Archivos\ de\ programa/NSIS/makensis.exe -DPRODUCT_VERSION=$1 instalador.nsi
wine ~/.wine/drive_c/Archivos\ de\ programa/NSIS/makensis.exe -DPRODUCT_VERSION=$1 -DMAKE_UPDATER=1 instalador.nsi
