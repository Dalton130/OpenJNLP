#!/bin/sh

# $Id: openjnlp.sh,v 1.1.1.1 2004/09/16 14:05:36 matysiak Exp $

# if OPENJNLP_HOME not set, figure out what it should be
if [ "x${OPENJNLP_HOME}" = "x" ]; then
    PRG=$0
    progname=`basename $0`

    # resolve symlinks
    while [ -L "${PRG}" ]; do
        ls=`ls -ld "${PRG}"`
        link=`expr "$ls" : '.*-> \(.*\)$'`

        if expr "${link}" : '[/]' > /dev/null; then
            PRG="${link}"
        else
            PRG="`dirname ${PRG}`/${link}"
        fi
    done

    OPENJNLP_HOME=`dirname "${PRG}"`/..
fi

#if JAVA_HOME is set, use that, otherwise use default
if [ "x${JAVA_HOME}" = "x" ]; then
    JAVACMD=/usr/bin/java
else
    JAVACMD=${JAVA_HOME}/bin/java
fi

JAVAOPTS=""
PROPS=""

# check to see if running on Mac OS X (Darwin) and if so, add special props
if [ "`uname -s`" = "Darwin" ]; then
    JAVAOPTS="-Xdock:name=OpenJNLP"
    PROPS="-Dcom.apple.macos.useScreenMenuBar=true -Dcom.apple.mrj.application.apple.menu.about.name=OpenJNLP"
fi

JARDIR=${OPENJNLP_HOME}/lib
APP=${JARDIR}/openjnlp-app.jar
LAUNCHER=${JARDIR}/openjnlp-lib.jar:${JARDIR}/openjnlp-extra.jar:${JARDIR}/jnlp.jar

${JAVACMD} ${JAVAOPTS} -cp ${APP}:${LAUNCHER} ${PROPS} org.nanode.app.OpenJNLP $@
