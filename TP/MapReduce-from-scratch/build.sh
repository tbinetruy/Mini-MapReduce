#!/bin/bash

cd src
javac -d ../bin $1.java
javac -d ../bin Helpers.java
cd ../bin
jar -cfm $1.jar $1.mf  Helpers.class $1.class
mv $1.jar ../build/$1.jar
