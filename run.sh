#!/bin/csh

cd `dirname ${0}`
pwd


set b=../batik-1.7

#-- batik.jar is a file that just contains the classpath listing all the other
#-- jars. So we only need this one

set cp=${b}/batik.jar:lib/dndo.jar


java -cp $cp -Xmx512m dd.gui.DDGUI $1
