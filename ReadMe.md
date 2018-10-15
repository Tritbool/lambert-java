# lambert-scala

[![Build Status](https://travis-ci.org/Tritbool/lambert-scala.svg?branch=master)](https://travis-ci.org/Tritbool/lambert-scala)
![Build Status](https://sonarcloud.io/api/project_badges/measure?project=Lambert-Scala&metric=alert_status)
![Build Status](https://sonarcloud.io/api/project_badges/measure?project=Lambert-Scala&metric=bugs)
![Build Status](https://sonarcloud.io/api/project_badges/measure?project=Lambert-Scala&metric=code_smells)
![Build Status](https://sonarcloud.io/api/project_badges/measure?project=Lambert-Scala&metric=sqale_rating)
![Build Status](https://sonarcloud.io/api/project_badges/measure?project=Lambert-Scala&metric=reliability_rating)
![Build Status](https://sonarcloud.io/api/project_badges/measure?project=Lambert-Scala&metric=security_rating)
![Build Status](https://sonarcloud.io/api/project_badges/measure?project=Lambert-Scala&metric=sqale_index)
![Build Status](https://sonarcloud.io/api/project_badges/measure?project=Lambert-Scala&metric=vulnerabilities)

**Freely adapted from Yageek work at** : https://github.com/yageek/lambert-java 

The idea was to have :
* A version that would be compatible with Spark for geographic data manipulation
* A native compatibility with SBT

A simple scala 2.11 library to convert Lambert Coordinates to GPS WGS84 coordinates based on the [IGN alorithms and methods](http://geodesie.ign.fr/contenu/fichiers/documentation/algorithmes/notice/NTG_71.pdf)

# Install
## From source with sbt
* Install `sbt`
* Compile with `sbt clean test package publishLocal`
* You can now add the sbt dependency `libraryDependencies +="net.yageek" %% "lambert-scala"%"1.0"` to your Scala projects

# Usage
The usage is mostly the same as the initial java version :

```scala

import net.yageek.lambert.ZoneCode._

 val pt:LambertPoint = Lambert.convertToWGS84Deg(994272.661, 113467.422, new LambertZone(LambertI))
 println("Point latitude:" + pt.getY() + " longitude:" + pt.getX())
```
