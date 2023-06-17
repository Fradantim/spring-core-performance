# spring-core-performance
Comparison between 
- blocking tomcat
- reactive netty
- project loom

with and without integrations 
- postgresql
- mongodb
- kafka

with and without GraalVM native compilation

## Requirements to build test apps
- docker
- xmllint (soon to be made optional)
- GraalVM for Java 17 (SDKMAN 22.3.2.r17-grl works great)

## Building test apps

``` bash
bash docker_build_all.sh
```