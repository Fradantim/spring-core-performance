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
REPOSITORY                 TAG        IMAGE ID       CREATED        SIZE
paketobuildpacks/run       base-cnb   1c8659977d9c   11 days ago    87.1MB
paketobuildpacks/run       tiny-cnb   cf6e761b103b   11 days ago    17.6MB
paketobuildpacks/builder   base       d37640437760   43 years ago   1.29GB
paketobuildpacks/builder   tiny       c9013cb8c33f   43 years ago   652MB
web                        jvm        74c98ecfd909   43 years ago   281MB
web                        graalvm    4a86fe62c7b7   43 years ago   112MB
web_jdbc                   jvm        cd906114b431   43 years ago   285MB
web_jdbc                   graalvm    b9182dfeda3b   43 years ago   124MB
web_mongo                  jvm        2cbec90ebe96   43 years ago   287MB
web_mongo                  graalvm    afbb8960e329   43 years ago   126MB
webflux                    jvm        d63afcffc301   43 years ago   284MB
webflux                    graalvm    625d2378db15   43 years ago   111MB
webflux_r_mongo            jvm        ddfcc5246125   43 years ago   290MB
webflux_r_mongo            graalvm    f31851f8cbfa   43 years ago   124MB
webflux_r2dbc              jvm        7504ea3d0be2   43 years ago   289MB
webflux_r2dbc              graalvm    3ff37ab39c2e   43 years ago   118MB
```