# spring-core-performance
Comparison between 
- blocking tomcat
- reactive netty

with and without
- virtual threads (Project Loom)
- GraalVM native compilation
- profile guided optimizations

with and without integrations 
- postgresql
- mongodb
- redis
- http server

## Requirements to build test apps
- docker

## HW & SW specs
Results provided here were executed in this machine:
```
CPU: Intel(R) Core(TM) i7-7700K CPU @ 4.20GHz
RAM: 16GB DDR4 @ 3200MHz
```
> uname -a 
```
Linux 6.2.0-35-generic #35~22.04.1-Ubuntu SMP PREEMPT_DYNAMIC Fri Oct  6 10:23:26 UTC 2 x86_64 x86_64 x86_64 GNU/Linux
```
> docker info
<details>
<summary>Click to expand</summary>

```
Client: Docker Engine - Community
 Version:    24.0.7
 Context:    rootless
 Debug Mode: false
 Plugins:
  buildx: Docker Buildx (Docker Inc.)
    Version:  v0.11.2
    Path:     /usr/libexec/docker/cli-plugins/docker-buildx
  compose: Docker Compose (Docker Inc.)
    Version:  v2.21.0
    Path:     /usr/libexec/docker/cli-plugins/docker-compose

Server:
 Containers: 5
  Running: 0
  Paused: 0
  Stopped: 5
 Images: 42
 Server Version: 24.0.7
 Storage Driver: overlay2
  Backing Filesystem: extfs
  Supports d_type: true
  Using metacopy: false
  Native Overlay Diff: false
  userxattr: true
 Logging Driver: json-file
 Cgroup Driver: systemd
 Cgroup Version: 2
 Plugins:
  Volume: local
  Network: bridge host ipvlan macvlan null overlay
  Log: awslogs fluentd gcplogs gelf journald json-file local logentries splunk syslog
 Swarm: inactive
 Runtimes: io.containerd.runc.v2 runc
 Default Runtime: runc
 Init Binary: docker-init
 containerd version: 61f9fd88f79f081d64d6fa3bb1a0dc71ec870523
 runc version: v1.1.9-0-gccaecfc
 init version: de40ad0
 Security Options:
  seccomp
   Profile: builtin
  rootless
  cgroupns
 Kernel Version: 6.2.0-35-generic
 Operating System: Ubuntu 22.04.3 LTS
 OSType: linux
 Architecture: x86_64
 CPUs: 8
 Total Memory: 15.54GiB
 Name: kl-ubuntu
 ID: 2eb785d8-b803-44c3-89c3-7f36fbd931cb
 Docker Root Dir: /home/fradantim/.local/share/docker
 Debug Mode: false
 Experimental: false
 Insecure Registries:
  127.0.0.0/8
 Live Restore Enabled: false

WARNING: bridge-nf-call-iptables is disabled
WARNING: bridge-nf-call-ip6tables is disabled
```
</details>

## Building test apps

``` bash
bash docker_build_all.sh
```

Something went wrong? you may need to:
``` bash
sudo chmod -R 777 outputs/
```

After completition you should be able to see test apps docker images

``` bash
docker image ls --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
```
```
REPOSITORY              TAG            SIZE
wfx-redis               jvm            235MB
wfx-redis               gvm-pgo        112MB
wfx-redis               gvm            140MB
wfx-r2dbc               jvm            232MB
wfx-r2dbc               gvm-pgo        105MB
wfx-r2dbc               gvm            124MB
wfx-mongo               jvm            235MB
wfx-mongo               gvm-pgo        108MB
wfx-mongo               gvm            128MB
wfx-http                jvm            228MB
wfx-http                gvm-pgo        96MB
wfx-http                gvm            115MB
wfx                     jvm            228MB
wfx                     gvm-pgo        92.1MB
wfx                     gvm            112MB
web-redis               jvm            236MB
web-redis               gvm-pgo        116MB
web-redis               gvm            146MB
web-mongo               jvm            232MB
web-mongo               gvm-pgo        105MB
web-mongo               gvm            126MB
web-jdbc                jvm            230MB
web-jdbc                gvm-pgo        102MB
web-jdbc                gvm            122MB
web-http                jvm            226MB
web-http                gvm-pgo        91.4MB
web-http                gvm            109MB
web                     jvm            226MB
web                     gvm-pgo        91.6MB
web                     gvm            110MB
```

> took like 2 hours to complete... real	113m4,676s user	0m22,057s sys 0m15,584s

## Stressing test apps

``` bash
bash stress_all.sh
```
> took like 6 hours to complete... real 397m12,422s user 0m57,416s sys 0m17,045s

Run it as many times you want to get more data as next step will load the best result for each case.
``` bash
while true; do bash stress_all.sh; done
```
**IMPORTANT** to reduce disk usage at the end will execute a 
```
docker volume prune -f
```

## Loading results
``` bash
cd apps/util_result_collector
sh mvnw spring-boot:run -Dspring-boot.run.arguments=--path-2-look=$(pwd)/../../outputs
```

At the end it will print the test results in a html syntax:

### Simple web-app

Duration: 60s, ramp up: 15s

#### Requests processed per second (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>6212</th><th>6269</th></tr>
<tr><td>VT</td><th>6191</th><th>6269</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>8363</th><th>8545</th></tr>
<tr><td>VT</td><th>8418</th><th>8474</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>3098</th><th>2906</th></tr>
<tr><td>VT</td><th>3949</th><th>2812</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>6583</th><th>N/A</th></tr>
<tr><td>VT</td><th>6622</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>10355</th><th>N/A</th></tr>
<tr><td>VT</td><th>10317</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>3878</th><th>N/A</th></tr>
<tr><td>VT</td><th>3463</th><th>N/A</th></tr>
</table>
</details>

#### Amount of requests processed (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>372799</th><th>376414</th></tr>
<tr><td>VT</td><th>371499</th><th>376182</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>501446</th><th>513039</th></tr>
<tr><td>VT</td><th>504770</th><th>508576</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>185805</th><th>174573</th></tr>
<tr><td>VT</td><th>236914</th><th>169160</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>394900</th><th>N/A</th></tr>
<tr><td>VT</td><th>397168</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>620784</th><th>N/A</th></tr>
<tr><td>VT</td><th>618611</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>232543</th><th>N/A</th></tr>
<tr><td>VT</td><th>207895</th><th>N/A</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>0.053</th><th>0.064</th></tr>
<tr><td>VT</td><th>0.049</th><th>0.044</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.036</th><th>0.044</th></tr>
<tr><td>VT</td><th>0.035</th><th>0.037</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.305</th><th>4.717</th></tr>
<tr><td>VT</td><th>4.49</th><th>4.015</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>0.051</th><th>N/A</th></tr>
<tr><td>VT</td><th>0.038</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.035</th><th>N/A</th></tr>
<tr><td>VT</td><th>0.027</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.387</th><th>N/A</th></tr>
<tr><td>VT</td><th>4.176</th><th>N/A</th></tr>
</table>
</details>

#### CPU usage % + peak threads (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 114</th><th>100.00 / 208</th></tr>
<tr><td>VT</td><th>100.00 / 111</th><th>100.00 / 208</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 111</th><th>100.00 / 208</th></tr>
<tr><td>VT</td><th>100.00 / 118</th><th>100.00 / 208</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 116</th><th>100.00 / 211</th></tr>
<tr><td>VT</td><th>100.00 / 114</th><th>100.00 / 211</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 10</th><th>N/A</th></tr>
<tr><td>VT</td><th>100.00 / 10</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 10</th><th>N/A</th></tr>
<tr><td>VT</td><th>100.00 / 10</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 13</th><th>N/A</th></tr>
<tr><td>VT</td><th>100.00 / 13</th><th>N/A</th></tr>
</table>
</details>

### PostgreSQL integrated web-app

Duration: 60s, ramp up: 15s

#### Requests processed per second (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>2525</th><th>2587</th></tr>
<tr><td>VT</td><th>2524</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>3299</th><th>3376</th></tr>
<tr><td>VT</td><th>3281</th><th>3406</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>1173</th><th>1181</th></tr>
<tr><td>VT</td><th>1377</th><th>1195</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>2623</th><th>N/A</th></tr>
<tr><td>VT</td><th>2582</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>3749</th><th>N/A</th></tr>
<tr><td>VT</td><th>3771</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>708</th><th>N/A</th></tr>
<tr><td>VT</td><th>707</th><th>N/A</th></tr>
</table>
</details>

#### Amount of requests processed (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>151643</th><th>155436</th></tr>
<tr><td>VT</td><th>151445</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>197892</th><th>202851</th></tr>
<tr><td>VT</td><th>196858</th><th>204492</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>70438</th><th>71103<br>e: 1</th></tr>
<tr><td>VT</td><th>82728</th><th>71817<br>e: 1</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>157460</th><th>N/A</th></tr>
<tr><td>VT</td><th>155059</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>225051</th><th>N/A</th></tr>
<tr><td>VT</td><th>226181</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>42537</th><th>N/A</th></tr>
<tr><td>VT</td><th>42463</th><th>N/A</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>0.079</th><th>0.09</th></tr>
<tr><td>VT</td><th>0.083</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.066</th><th>0.071</th></tr>
<tr><td>VT</td><th>0.064</th><th>0.061</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>6.099</th><th>6.005</th></tr>
<tr><td>VT</td><th>5.979</th><th>6.011</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>0.067</th><th>N/A</th></tr>
<tr><td>VT</td><th>0.051</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.052</th><th>N/A</th></tr>
<tr><td>VT</td><th>0.04</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.475</th><th>N/A</th></tr>
<tr><td>VT</td><th>5.293</th><th>N/A</th></tr>
</table>
</details>

#### CPU usage % + peak threads (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 113</th><th>100.00 / 211</th></tr>
<tr><td>VT</td><th>100.00 / 113</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 113</th><th>100.00 / 211</th></tr>
<tr><td>VT</td><th>100.00 / 113</th><th>100.00 / 211</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 116</th><th>100.00 / 214</th></tr>
<tr><td>VT</td><th>100.00 / 117</th><th>100.00 / 214</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 16</th><th>N/A</th></tr>
<tr><td>VT</td><th>100.00 / 16</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 16</th><th>N/A</th></tr>
<tr><td>VT</td><th>100.00 / 16</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 19</th><th>N/A</th></tr>
<tr><td>VT</td><th>100.00 / 19</th><th>N/A</th></tr>
</table>
</details>

### MongoDB integrated web-app

Duration: 60s, ramp up: 15s

#### Requests processed per second (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="1">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>2764</th></tr>
<tr><td>VT</td><th>2718</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>3738</th></tr>
<tr><td>VT</td><th>3783</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>1003</th></tr>
<tr><td>VT</td><th>872</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>2485</th></tr>
<tr><td>VT</td><th>2482</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>3763</th></tr>
<tr><td>VT</td><th>3757</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>757</th></tr>
<tr><td>VT</td><th>651</th></tr>
</table>
</details>

#### Amount of requests processed (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="1">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>165767</th></tr>
<tr><td>VT</td><th>163214</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>224235</th></tr>
<tr><td>VT</td><th>226860</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>60246</th></tr>
<tr><td>VT</td><th>52379</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>149044</th></tr>
<tr><td>VT</td><th>149008</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>225669</th></tr>
<tr><td>VT</td><th>225563</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>45407</th></tr>
<tr><td>VT</td><th>39071</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="1">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>0.065</th></tr>
<tr><td>VT</td><th>0.061</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.05</th></tr>
<tr><td>VT</td><th>0.047</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.715</th></tr>
<tr><td>VT</td><th>5.52</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>0.065</th></tr>
<tr><td>VT</td><th>0.047</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.049</th></tr>
<tr><td>VT</td><th>0.037</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.68</th></tr>
<tr><td>VT</td><th>5.781</th></tr>
</table>
</details>

#### CPU usage % + peak threads (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="1">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 114</th></tr>
<tr><td>VT</td><th>100.00 / 114</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 114</th></tr>
<tr><td>VT</td><th>100.00 / 114</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 117</th></tr>
<tr><td>VT</td><th>100.00 / 118</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 16</th></tr>
<tr><td>VT</td><th>100.00 / 16</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 16</th></tr>
<tr><td>VT</td><th>100.00 / 16</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 19</th></tr>
<tr><td>VT</td><th>100.00 / 19</th></tr>
</table>
</details>

### Redis integrated web-app

Duration: 60s, ramp up: 15s

#### Requests processed per second (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="1">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>4233</th></tr>
<tr><td>VT</td><th>4234</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>5680</th></tr>
<tr><td>VT</td><th>5636</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2105</th></tr>
<tr><td>VT</td><th>2328</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>4578</th></tr>
<tr><td>VT</td><th>4532</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>7353</th></tr>
<tr><td>VT</td><th>7331</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2131</th></tr>
<tr><td>VT</td><th>2319</th></tr>
</table>
</details>

#### Amount of requests processed (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="1">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>254032</th></tr>
<tr><td>VT</td><th>253901</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>340692</th></tr>
<tr><td>VT</td><th>338131</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>126262</th></tr>
<tr><td>VT</td><th>139653</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>274708</th></tr>
<tr><td>VT</td><th>272033</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>441273</th></tr>
<tr><td>VT</td><th>439579</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>127827</th></tr>
<tr><td>VT</td><th>139084</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="1">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>0.064</th></tr>
<tr><td>VT</td><th>0.059</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.051</th></tr>
<tr><td>VT</td><th>0.046</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.769</th></tr>
<tr><td>VT</td><th>5.386</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>0.061</th></tr>
<tr><td>VT</td><th>0.041</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.049</th></tr>
<tr><td>VT</td><th>0.034</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.303</th></tr>
<tr><td>VT</td><th>5.071</th></tr>
</table>
</details>

#### CPU usage % + peak threads (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="1">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 114</th></tr>
<tr><td>VT</td><th>100.00 / 114</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 114</th></tr>
<tr><td>VT</td><th>100.00 / 113</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 117</th></tr>
<tr><td>VT</td><th>100.00 / 117</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 14</th></tr>
<tr><td>VT</td><th>100.00 / 14</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 14</th></tr>
<tr><td>VT</td><th>100.00 / 14</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 17</th></tr>
<tr><td>VT</td><th>100.00 / 17</th></tr>
</table>
</details>

### Http integrated web-app

Duration: 60s, ramp up: 15s

#### Requests processed per second (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>2037</th><th>517</th></tr>
<tr><td>VT</td><th>2008</th><th>496</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>2718</th><th>625</th></tr>
<tr><td>VT</td><th>2691</th><th>573</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>920</th><th>153</th></tr>
<tr><td>VT</td><th>821</th><th>152</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>2737</th><th>N/A</th></tr>
<tr><td>VT</td><th>2737</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>4009</th><th>N/A</th></tr>
<tr><td>VT</td><th>4114</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>1096</th><th>N/A</th></tr>
<tr><td>VT</td><th>1016</th><th>N/A</th></tr>
</table>
</details>

#### Amount of requests processed (JMeter)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>122332</th><th>33104<br>e: 3608</th></tr>
<tr><td>VT</td><th>120562</th><th>31266<br>e: 3600</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>163072</th><th>39354<br>e: 3600</th></tr>
<tr><td>VT</td><th>161383</th><th>36061<br>e: 3631</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>55250</th><th>9627<br>e: 3600</th></tr>
<tr><td>VT</td><th>49276</th><th>9666<br>e: 3600</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>164347</th><th>N/A</th></tr>
<tr><td>VT</td><th>164342</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>240476</th><th>N/A</th></tr>
<tr><td>VT</td><th>246788</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>65763</th><th>N/A</th></tr>
<tr><td>VT</td><th>61047</th><th>N/A</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>0.063</th><th>0.052</th></tr>
<tr><td>VT</td><th>0.045</th><th>0.046</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.037</th><th>0.044</th></tr>
<tr><td>VT</td><th>0.035</th><th>0.037</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.274</th><th>4.496</th></tr>
<tr><td>VT</td><th>4.2</th><th>4.585</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>0.05</th><th>N/A</th></tr>
<tr><td>VT</td><th>0.037</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.035</th><th>N/A</th></tr>
<tr><td>VT</td><th>0.027</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.34</th><th>N/A</th></tr>
<tr><td>VT</td><th>4.289</th><th>N/A</th></tr>
</table>
</details>

#### CPU usage % + peak threads (Prometheus)
<details><summary>Click to expand</summary><table>
<tr><th></th><th></th><th></th><th colspan="2">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="1">100</th><th colspan="1">300</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>1</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 208</th><th>100.00 / 209</th></tr>
<tr><td>VT</td><th>100.00 / 206</th><th>100.00 / 209</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 202</th><th>100.00 / 209</th></tr>
<tr><td>VT</td><th>100.00 / 203</th><th>100.00 / 209</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 210</th><th>100.00 / 212</th></tr>
<tr><td>VT</td><th>100.00 / 210</th><th>100.00 / 212</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>100.00 / 10</th><th>N/A</th></tr>
<tr><td>VT</td><th>100.00 / 10</th><th>N/A</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00 / 10</th><th>N/A</th></tr>
<tr><td>VT</td><th>100.00 / 10</th><th>N/A</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00 / 13</th><th>N/A</th></tr>
<tr><td>VT</td><th>100.00 / 13</th><th>N/A</th></tr>
</table>
</details>