# java-checksum-flyway-maven-plugin

[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://tldrlegal.com/license/mit-license#summary)
[![Build Status](https://travis-ci.org/agileek/java-checksum-flyway-maven-plugin.svg?branch=master)](https://travis-ci.org/agileek/java-checksum-flyway-maven-plugin)
[![codecov](https://codecov.io/gh/agileek/java-checksum-flyway-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/agileek/java-checksum-flyway-maven-plugin)



## Goal
The goal of this project is to help you generate checksum for your java migration using flyway.

There is a file checksum stored in the `schema_version` table, but only for the .sql

You have to provide your own checksum for java files.

This plugin scans the db/migration folder looking for java migrations, and for each one, compute an md5sum and add it to the `JavaMigrationChecksums` enum 


## Usages

Simply put this in your pom 
```xml
    <plugin>
        <groupId>io.github.agileek</groupId>
        <artifactId>java-checksum-flyway-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
            <execution>
                <goals>
                    <goal>generate</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
``` 
This will generate a file `${project.build.directory}/generated-sources/io/github/agileek/flywayJavaMigrationChecksums` and you can use it in your java migrations

In your migration class, implement `MigrationChecksumProvider` then

```java
    @Override
    public Integer getChecksum() {
        return JavaMigrationChecksums.valueOf(getClass().getSimpleName()).getChecksum();
    }

```
### Parameters

The parameters are:

* location
  * defaultValue = "/db/migration"
* outputDirectory
  * defaultValue = "${project.build.directory}/generated-sources"
