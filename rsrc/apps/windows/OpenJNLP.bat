@echo off

REM
REM Batch file for starting OpenJNLP on Windows
REM

REM execute OpenJNLP
start javaw -classpath lib\openjnlp-app.jar;lib\openjnlp-lib.jar;lib\openjnlp-extra.jar;lib\jnlp.jar org.nanode.app.OpenJNLP %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
