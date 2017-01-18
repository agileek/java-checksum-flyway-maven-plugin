#!/bin/bash
set -evx

if [ "${TRAVIS_JDK_VERSION}" = "openjdk6" ]; then
	mvn clean package -Dmaven.test.skip=true
else
    mvn clean package
fi