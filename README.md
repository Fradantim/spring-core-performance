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

After completition you should be able to see test apps docker images

``` bash
docker image ls
```
```
REPOSITORY               TAG       IMAGE ID       CREATED             SIZE
web_mongo                graalvm   (...)          (...)               114MB
web_mongo                jvm       (...)          (...)               193MB
web_jdbc                 graalvm   (...)          (...)               111MB
web_jdbc                 jvm       (...)          (...)               191MB
webflux_r_mongo          graalvm   (...)          (...)               114MB
webflux_r_mongo          jvm       (...)          (...)               196MB
webflux_r2dbc            graalvm   (...)          (...)               112MB
webflux_r2dbc            jvm       (...)          (...)               193MB
webflux                  graalvm   (...)          (...)               100MB
webflux                  jvm       (...)          (...)               189MB
web                      graalvm   (...)          (...)               100MB
web                      jvm       (...)          (...)               186MB
```