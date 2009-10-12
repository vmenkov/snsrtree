#!/bin/csh

#  set b=../batik-1.7

#-- batik.jar is a file that just contains the classpath listing all the other
#-- jars. So we only need this one

#set cp=${b}/batik.jar:classes
set cp=classes


time java -cp $cp  -Xmx512m -Dverbosity=0 dd.engine.Main $1
