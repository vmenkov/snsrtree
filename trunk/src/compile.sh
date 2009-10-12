#!/bin/csh

echo arg0=${0}
cd `dirname ${0}`
pwd

#-- the location of the Batik directory
set b=../../batik-1.7

#-- batik.jar is a file that just contains the classpath listing all the other
#-- jars. So we only need this one
set cp=$b/batik.jar

# -target 1.5
#javac -classpath $cp  -target 1.5 -sourcepath . -d ../classes dd/engine/*.java dd/gui/*.java dd/util/*.java

javac -classpath $cp  -target 1.5 -sourcepath . -d ../classes dd/engine/*.java dd/gui/*.java dd/util/*.java
