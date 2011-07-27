#!/bin/bash
cp ../../dist/Maimonides.jar Maimonides.jar
./firmar.sh Maimonides.jar
if [ ! -d "lib/" ]; then
    ./cp_libs.sh
fi
rm -R ./dist/$1/jnlp_$1
mkdir ../dist/$1/jnlp_$1
cp -R lib/ ../$1/dist/jnlp_$1
cp Maimonides.jar ../dist/$1/jnlp_$1
cp ico.gif ../dist/$1/jnlp_$1
cp ico.png ../dist/$1/jnlp_$1
cp index.html ../dist/$1/jnlp_$1
cp jnlp.php ../dist/$1/jnlp_$1
cp ../../license.txt ../dist/$1/jnlp_$1
echo $1 >> ../dist/$1/jnlp_$1/version.txt
