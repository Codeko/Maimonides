#!/bin/bash
cp ../../dist/Maimonides.jar Maimonides.jar
./firmar.sh Maimonides.jar
if [ ! -d "lib/" ]; then
    ./cp_libs.sh
fi

DIR="../dist/$1/jnlp_$1"

if [ -d $DIR ]; then
    rm -R $DIR
fi

mkdir -p $DIR
cp -R lib/ $DIR
cp Maimonides.jar $DIR
cp ico.gif $DIR
cp ico.png $DIR
cp index.html $DIR
cp jnlp.php $DIR
cp ../../license.txt $DIR
echo $1 > "$DIR/version.txt"
