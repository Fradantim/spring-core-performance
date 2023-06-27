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

## Stressing test apps

``` bash
bash stress_all.sh
```
Run it as many times you want to get more data as next step will load the best result for each case.

## Loading results
``` bash
cd apps/util_result_collector
sh mvnw spring-boot:run
```

At the end it will print the test results:


### Simple
| app & type \ #clients | 25 | 50 | 100 | 200 |
| - | - | - | - | - |
| web:graalvm | 348422 | 353575 | 356917 | 358932 |
| web:jvm | 472979 | 436710 | 546042 | 300674 |
| webflux:graalvm | 439096 | 339827 | 299876 | 301809 |
| webflux:jvm | 266608 | 301927 | 190958 | 204455 |

### PostgreSQL
| app & type \ #clients | 25 | 50 | 100 | 200 |
| - | - | - | - | - |
| web_jdbc:graalvm | 156308 | 158161 | 158653 | 157980 |
| web_jdbc:jvm | 151012 | 124581 | 88593 | 90813 |
| webflux_r2dbc:graalvm | 129334 | 128231 | 125018 | 128274 |
| webflux_r2dbc:jvm | 91540 | 81399 | 101886 | 95656 |

### MongoDB
| app & type \ #clients | 25 | 50 | 100 | 200 |
| - | - | - | - | - |
| web_mongo:graalvm | 144244 | 145313 | 211276 | 145868 |
| web_mongo:jvm | 82800 | 86634 | 77629 | 128768 |
| webflux_r_mongo:graalvm | 116291 | 117792 | 119688 | 119226 |
| webflux_r_mongo:jvm | 92960 | 58724 | 50748 | 49676 |