@echo off

set b="..\batik-1.7"

rem batik.jar is a file that just contains the classpath listing all the other
rem jars. So we only need this one

set cp=%b%\batik.jar;classes

java -cp %cp%  -Xmx512m dd.gui.DDGUI
