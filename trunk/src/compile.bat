
rem set b=../../batik-1.7

rem set cp=$b/batik.jar


javac -classpath ../../batik-1.7/batik.jar  -Xlint:unchecked -target 1.5 -sourcepath . -d ../classes dd/engine/*.java dd/gui/*.java dd/util/*.java



