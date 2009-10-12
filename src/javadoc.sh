#!/bin/csh

set ver=`grep version  dd/engine/Main.java  | perl -pe 's/[^0-9\.]//g'`
echo "As per Main.java, the current version is ${ver}"

set b=../../batik-1.7
set cp=${b}/batik.jar:../classes

javadoc -protected -d ../javadoc -sourcepath . -classpath ${cp} \
 -use -link http://java.sun.com/j2se/1.5.0/docs/api/ -header "<em>Deceptive Detection ${ver}</em>" -windowtitle "Deceptive Detection" -overview dd/overview.html  dd.engine dd.gui dd.util

# -sourcepath . -subpackages
