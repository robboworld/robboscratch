@ECHO OFF

SETLOCAL ENABLEDELAYEDEXPANSION

SET ALL_JARS=

cd lib
FOR /R %%f IN (*.jar) DO (
   SET ALL_JARS=%%f;!ALL_JARS!
)
cd ..



start java -classpath "%ALL_JARS%"  -Xmx512m scratchduino.robot.Main

