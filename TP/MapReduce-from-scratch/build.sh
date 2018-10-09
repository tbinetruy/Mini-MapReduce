#!/bin/bash

javac -d bin src/$1.java
cd bin
jar -cfm $1.jar $1.mf $1.class
mv $1.jar ../build/$1.jar
