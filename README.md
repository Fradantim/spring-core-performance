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
wfx-redis               gvm-pgo        114MB
wfx-redis               gvm            141MB
wfx-r2dbc               jvm            234MB
wfx-r2dbc               gvm-pgo        104MB
wfx-r2dbc               gvm            124MB
wfx-mongo               jvm            236MB
wfx-mongo               gvm-pgo        107MB
wfx-mongo               gvm            127MB
wfx-http                jvm            229MB
wfx-http                gvm-pgo        97.1MB
wfx-http                gvm            116MB
wfx                     jvm            229MB
wfx                     gvm-pgo        93.7MB
wfx                     gvm            113MB
web-redis               jvm            237MB
web-redis               gvm-pgo        118MB
web-redis               gvm            146MB
web-mongo               jvm            233MB
web-mongo               gvm-pgo        105MB
web-mongo               gvm            125MB
web-jdbc                jvm            228MB
web-jdbc                gvm-pgo        98.6MB
web-jdbc                gvm            117MB
web-http                jvm            226MB
web-http                gvm-pgo        95.2MB
web-http                gvm            113MB
web                     jvm            226MB
web                     gvm-pgo        92.5MB
web                     gvm            111MB
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

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>6661</th><th>13435</th><th>6758</th><th>13606</th><th>6789</th><th>13615</th></tr>
<tr><td>VT</td><th>6682</th><th>13413</th><th>6782</th><th>13561</th><th>6794</th><th>13651</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>9200</th><th>18390</th><th>9333</th><th>18452</th><th>9422</th><th>18091</th></tr>
<tr><td>VT</td><th>9180</th><th>18363</th><th>9307</th><th>18551</th><th>9390</th><th>18125</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>7732</th><th>17270</th><th>6769</th><th>16772</th><th>5262</th><th>15444</th></tr>
<tr><td>VT</td><th>11016</th><th>20828</th><th>10666</th><th>20574</th><th>9495</th><th>20441</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>6979</th><th>13966</th><th>6937</th><th>13978</th><th>6859</th><th>13848</th></tr>
<tr><td>VT</td><th>6994</th><th>13985</th><th>6949</th><th>13888</th><th>6862</th><th>13817</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>11014</th><th>22001</th><th>10809</th><th>21726</th><th>10583</th><th>21464</th></tr>
<tr><td>VT</td><th>10994</th><th>22082</th><th>10876</th><th>21666</th><th>10606</th><th>21442</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>7173</th><th>16465</th><th>6657</th><th>16707</th><th>6720</th><th>15986</th></tr>
<tr><td>VT</td><th>6981</th><th>16643</th><th>6736</th><th>16390</th><th>7098</th><th>15964</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>799220</th><th>1611924</th><th>810970</th><th>1632314</th><th>814861</th><th>1633852</th></tr>
<tr><td>VT</td><th>801647</th><th>1609104</th><th>813650</th><th>1627265</th><th>815616</th><th>1637841</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>1103892</th><th>2205891</th><th>1119855</th><th>2213570</th><th>1131023</th><th>2170796</th></tr>
<tr><td>VT</td><th>1101666</th><th>2202725</th><th>1116771</th><th>2226210</th><th>1127003</th><th>2174657</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>928068</th><th>2071582</th><th>812055</th><th>2011918</th><th>631555</th><th>1853134</th></tr>
<tr><td>VT</td><th>1321188</th><th>2498296</th><th>1279552</th><th>2468071</th><th>1139242</th><th>2452485</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>837743</th><th>1675253</th><th>832702</th><th>1676790</th><th>823085</th><th>1661504</th></tr>
<tr><td>VT</td><th>839481</th><th>1677716</th><th>833714</th><th>1666595</th><th>823318</th><th>1658108</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>1321831</th><th>2640160</th><th>1296587</th><th>2606704</th><th>1270142</th><th>2574974</th></tr>
<tr><td>VT</td><th>1319521</th><th>2649868</th><th>1304735</th><th>2598945</th><th>1273115</th><th>2572778</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>860585</th><th>1975280</th><th>798786</th><th>2004159</th><th>806678</th><th>1917872</th></tr>
<tr><td>VT</td><th>837948</th><th>1996851</th><th>808201</th><th>1966201</th><th>851704</th><th>1915448</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>0.088</th><th>0.055</th><th>0.049</th><th>0.065</th><th>0.05</th><th>0.076</th></tr>
<tr><td>VT</td><th>0.048</th><th>0.05</th><th>0.05</th><th>0.049</th><th>0.049</th><th>0.048</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.041</th><th>0.039</th><th>0.039</th><th>0.062</th><th>0.038</th><th>0.06</th></tr>
<tr><td>VT</td><th>0.037</th><th>0.039</th><th>0.038</th><th>0.038</th><th>0.04</th><th>0.038</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>3.932</th><th>1.998</th><th>3.967</th><th>1.988</th><th>4.091</th><th>2.088</th></tr>
<tr><td>VT</td><th>3.885</th><th>1.947</th><th>3.899</th><th>1.929</th><th>4.187</th><th>2.069</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>0.073</th><th>0.048</th><th>0.036</th><th>0.045</th><th>0.036</th><th>0.056</th></tr>
<tr><td>VT</td><th>0.037</th><th>0.041</th><th>0.036</th><th>0.105</th><th>0.039</th><th>0.04</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.05</th><th>0.048</th><th>0.029</th><th>0.053</th><th>0.03</th><th>0.042</th></tr>
<tr><td>VT</td><th>0.028</th><th>0.03</th><th>0.03</th><th>0.035</th><th>0.03</th><th>0.029</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>3.937</th><th>1.886</th><th>3.821</th><th>2.094</th><th>3.83</th><th>1.927</th></tr>
<tr><td>VT</td><th>4.032</th><th>1.965</th><th>3.71</th><th>1.909</th><th>4.036</th><th>1.877</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>61</th><th>100.00<br>546.00 MB<br>62</th><th>100.00<br>546.00 MB<br>112</th><th>100.00<br>546.00 MB<br>112</th><th>100.00<br>546.00 MB<br>208</th><th>100.00<br>546.00 MB<br>208</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>62</th><th>100.00<br>546.00 MB<br>65</th><th>100.00<br>546.00 MB<br>112</th><th>100.00<br>546.00 MB<br>113</th><th>100.00<br>546.00 MB<br>208</th><th>100.00<br>546.00 MB<br>208</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>546.00 MB<br>61</th><th>100.00<br>546.00 MB<br>60</th><th>100.00<br>546.00 MB<br>112</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>208</th><th>100.00<br>546.00 MB<br>208</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>61</th><th>100.00<br>546.00 MB<br>62</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>112</th><th>100.00<br>546.00 MB<br>208</th><th>100.00<br>546.00 MB<br>208</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>66</th><th>100.00<br>1024.00 MB<br>65</th><th>100.00<br>1024.00 MB<br>117</th><th>100.00<br>1024.00 MB<br>117</th><th>100.00<br>1024.00 MB<br>212</th><th>100.00<br>1024.00 MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>15</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th></tr>
</table>
</details>

### PostgreSQL integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>4095</th><th>8031</th><th>4303</th><th>8732</th><th>4329</th><th>8814</th></tr>
<tr><td>VT</td><th>4089</th><th>8067</th><th>4294</th><th>8700</th><th>4327</th><th>8822</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>5117</th><th>10653</th><th>5543</th><th>10863</th><th>5577</th><th>11379</th></tr>
<tr><td>VT</td><th>5184</th><th>10622</th><th>5534</th><th>10740</th><th>5581</th><th>11356</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4186</th><th>9845</th><th>4095</th><th>9487</th><th>4030</th><th>9865</th></tr>
<tr><td>VT</td><th>6308</th><th>11777</th><th>6086</th><th>11620</th><th>6026</th><th>11624</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>3505</th><th>7107</th><th>3516</th><th>7122</th><th>3519</th><th>7110</th></tr>
<tr><td>VT</td><th>3523</th><th>7092</th><th>3513</th><th>7153</th><th>3510</th><th>7105</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>5264</th><th>10572</th><th>5264</th><th>10699</th><th>5286</th><th>10749</th></tr>
<tr><td>VT</td><th>5274</th><th>10596</th><th>5297</th><th>10719</th><th>5276</th><th>10741</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2122</th><th>6740</th><th>2158</th><th>6433</th><th>1868</th><th>6235</th></tr>
<tr><td>VT</td><th>2204</th><th>6850</th><th>2065</th><th>6656</th><th>1907</th><th>6057</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>491599</th><th>963435</th><th>516363</th><th>1047588</th><th>519706</th><th>1057882</th></tr>
<tr><td>VT</td><th>490806</th><th>968019</th><th>515440</th><th>1043705</th><th>519483</th><th>1058890</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>613897</th><th>1278005</th><th>664954</th><th>1303210</th><th>669153</th><th>1365598</th></tr>
<tr><td>VT</td><th>622256</th><th>1274209</th><th>664319</th><th>1288404</th><th>670027</th><th>1362511</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>502246</th><th>1181093</th><th>491908</th><th>1138000</th><th>483800</th><th>1183624</th></tr>
<tr><td>VT</td><th>756982</th><th>1412738</th><th>730302</th><th>1394014</th><th>723270</th><th>1394730</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>420477</th><th>852500</th><th>422016</th><th>854345</th><th>422540</th><th>853113</th></tr>
<tr><td>VT</td><th>422666</th><th>850865</th><th>421686</th><th>858076</th><th>421390</th><th>852417</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>631589</th><th>1268618</th><th>631660</th><th>1283952</th><th>634581</th><th>1289652</th></tr>
<tr><td>VT</td><th>633004</th><th>1271003</th><th>635519</th><th>1286128</th><th>633169</th><th>1288650</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>254695</th><th>808872</th><th>258919</th><th>771733</th><th>224237</th><th>748301</th></tr>
<tr><td>VT</td><th>264512</th><th>821804</th><th>247708</th><th>798699</th><th>228889</th><th>726938</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>0.123</th><th>0.082</th><th>0.075</th><th>0.097</th><th>0.07</th><th>0.094</th></tr>
<tr><td>VT</td><th>0.075</th><th>0.072</th><th>0.071</th><th>0.072</th><th>0.07</th><th>0.072</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.069</th><th>0.066</th><th>0.057</th><th>0.069</th><th>0.06</th><th>0.081</th></tr>
<tr><td>VT</td><th>0.06</th><th>0.06</th><th>0.06</th><th>0.059</th><th>0.059</th><th>0.058</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.598</th><th>2.136</th><th>4.285</th><th>2.215</th><th>4.506</th><th>2.236</th></tr>
<tr><td>VT</td><th>4.507</th><th>2.15</th><th>4.315</th><th>2.346</th><th>4.476</th><th>2.264</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>0.108</th><th>0.068</th><th>0.054</th><th>0.078</th><th>0.081</th><th>0.072</th></tr>
<tr><td>VT</td><th>0.056</th><th>0.054</th><th>0.053</th><th>0.054</th><th>0.052</th><th>0.055</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.058</th><th>0.054</th><th>0.069</th><th>0.071</th><th>0.047</th><th>0.06</th></tr>
<tr><td>VT</td><th>0.045</th><th>0.045</th><th>0.044</th><th>0.043</th><th>0.044</th><th>0.044</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.517</th><th>2.423</th><th>4.396</th><th>2.435</th><th>4.903</th><th>2.291</th></tr>
<tr><td>VT</td><th>4.52</th><th>2.301</th><th>4.793</th><th>2.315</th><th>4.585</th><th>2.278</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>64</th><th>100.00<br>546.00 MB<br>64</th><th>100.00<br>546.00 MB<br>113</th><th>100.00<br>546.00 MB<br>113</th><th>100.00<br>546.00 MB<br>211</th><th>100.00<br>546.00 MB<br>211</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>64</th><th>100.00<br>546.00 MB<br>64</th><th>100.00<br>546.00 MB<br>113</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>211</th><th>100.00<br>546.00 MB<br>211</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>546.00 MB<br>64</th><th>100.00<br>546.00 MB<br>65</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>211</th><th>100.00<br>546.00 MB<br>211</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>63</th><th>100.00<br>546.00 MB<br>66</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>211</th><th>100.00<br>546.00 MB<br>211</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>68</th><th>100.00<br>1024.00 MB<br>68</th><th>100.00<br>1024.00 MB<br>118</th><th>100.00<br>1024.00 MB<br>119</th><th>100.00<br>1024.00 MB<br>215</th><th>100.00<br>1024.00 MB<br>217</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>23</th><th>100.00<br>1024.00 MB<br>22</th><th>100.00<br>1024.00 MB<br>22</th><th>100.00<br>1024.00 MB<br>22</th><th>100.00<br>1024.00 MB<br>23</th><th>100.00<br>1024.00 MB<br>22</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>16</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>20</th></tr>
</table>
</details>

### MongoDB integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>2936</th><th>5938</th><th>2956</th><th>5996</th><th>2952</th><th>5991</th></tr>
<tr><td>VT</td><th>2941</th><th>5926</th><th>2955</th><th>5987</th><th>2944</th><th>5984</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>4005</th><th>8086</th><th>4067</th><th>8214</th><th>4055</th><th>8187</th></tr>
<tr><td>VT</td><th>3992</th><th>8090</th><th>4063</th><th>8205</th><th>4046</th><th>8159</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2670</th><th>7381</th><th>2639</th><th>7014</th><th>2252</th><th>7064</th></tr>
<tr><td>VT</td><th>4293</th><th>8791</th><th>3904</th><th>8681</th><th>3499</th><th>8195</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>2597</th><th>5236</th><th>2657</th><th>5321</th><th>2605</th><th>5383</th></tr>
<tr><td>VT</td><th>2599</th><th>5243</th><th>2633</th><th>5312</th><th>2605</th><th>5373</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>3905</th><th>7805</th><th>3971</th><th>8057</th><th>4011</th><th>8214</th></tr>
<tr><td>VT</td><th>3905</th><th>7822</th><th>3986</th><th>8062</th><th>3991</th><th>8217</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>1684</th><th>5544</th><th>1553</th><th>5303</th><th>1320</th><th>4931</th></tr>
<tr><td>VT</td><th>1694</th><th>5482</th><th>1698</th><th>5484</th><th>1332</th><th>4978</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>352342</th><th>712264</th><th>354765</th><th>719393</th><th>354225</th><th>719005</th></tr>
<tr><td>VT</td><th>352990</th><th>711049</th><th>354807</th><th>718264</th><th>353445</th><th>718000</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>480412</th><th>970341</th><th>487946</th><th>985319</th><th>486757</th><th>982460</th></tr>
<tr><td>VT</td><th>478955</th><th>970496</th><th>487760</th><th>984434</th><th>485507</th><th>979002</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>320546</th><th>885578</th><th>316651</th><th>841640</th><th>270375</th><th>847719</th></tr>
<tr><td>VT</td><th>515123</th><th>1054517</th><th>468374</th><th>1041510</th><th>419711</th><th>983289</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>311669</th><th>628304</th><th>318767</th><th>638367</th><th>312734</th><th>645687</th></tr>
<tr><td>VT</td><th>311928</th><th>628942</th><th>316015</th><th>637415</th><th>312572</th><th>644663</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>468642</th><th>936208</th><th>476484</th><th>966591</th><th>481225</th><th>985482</th></tr>
<tr><td>VT</td><th>468717</th><th>938637</th><th>478278</th><th>967115</th><th>479029</th><th>985779</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>202048</th><th>665077</th><th>186463</th><th>636262</th><th>158491</th><th>591726</th></tr>
<tr><td>VT</td><th>203355</th><th>657794</th><th>203713</th><th>657981</th><th>159912</th><th>597248</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>0.074</th><th>0.073</th><th>0.058</th><th>0.11</th><th>0.078</th><th>0.068</th></tr>
<tr><td>VT</td><th>0.059</th><th>0.062</th><th>0.063</th><th>0.065</th><th>0.062</th><th>0.063</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.077</th><th>0.06</th><th>0.046</th><th>0.063</th><th>0.067</th><th>0.066</th></tr>
<tr><td>VT</td><th>0.048</th><th>0.049</th><th>0.047</th><th>0.047</th><th>0.048</th><th>0.047</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.089</th><th>2.584</th><th>4.987</th><th>2.543</th><th>4.931</th><th>2.467</th></tr>
<tr><td>VT</td><th>5.018</th><th>2.409</th><th>4.881</th><th>2.611</th><th>4.919</th><th>2.452</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>0.102</th><th>0.062</th><th>0.05</th><th>0.092</th><th>0.081</th><th>0.069</th></tr>
<tr><td>VT</td><th>0.046</th><th>0.05</th><th>0.048</th><th>0.046</th><th>0.049</th><th>0.052</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.062</th><th>0.053</th><th>0.037</th><th>0.064</th><th>0.062</th><th>0.058</th></tr>
<tr><td>VT</td><th>0.037</th><th>0.038</th><th>0.038</th><th>0.038</th><th>0.039</th><th>0.037</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.822</th><th>2.451</th><th>4.893</th><th>2.433</th><th>4.992</th><th>2.408</th></tr>
<tr><td>VT</td><th>4.825</th><th>2.423</th><th>4.973</th><th>2.384</th><th>4.697</th><th>2.401</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>65</th><th>100.00<br>546.00 MB<br>65</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>116</th><th>100.00<br>546.00 MB<br>212</th><th>100.00<br>546.00 MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>66</th><th>100.00<br>546.00 MB<br>65</th><th>100.00<br>546.00 MB<br>115</th><th>100.00<br>546.00 MB<br>115</th><th>100.00<br>546.00 MB<br>212</th><th>100.00<br>546.00 MB<br>212</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>546.00 MB<br>65</th><th>100.00<br>546.00 MB<br>66</th><th>100.00<br>546.00 MB<br>116</th><th>100.00<br>546.00 MB<br>115</th><th>100.00<br>546.00 MB<br>212</th><th>100.00<br>546.00 MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>65</th><th>100.00<br>546.00 MB<br>65</th><th>100.00<br>546.00 MB<br>115</th><th>100.00<br>546.00 MB<br>116</th><th>100.00<br>546.00 MB<br>212</th><th>100.00<br>546.00 MB<br>212</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>69</th><th>100.00<br>1024.00 MB<br>69</th><th>100.00<br>1024.00 MB<br>119</th><th>100.00<br>1024.00 MB<br>120</th><th>100.00<br>1024.00 MB<br>216</th><th>100.00<br>1024.00 MB<br>216</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>23</th><th>100.00<br>1024.00 MB<br>23</th><th>100.00<br>1024.00 MB<br>23</th><th>100.00<br>1024.00 MB<br>23</th><th>100.00<br>1024.00 MB<br>23</th><th>100.00<br>1024.00 MB<br>23</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th><th>100.00<br>546.00 MB<br>16</th><th>100.00<br>546.00 MB<br>18</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>22</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>22</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>22</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>22</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>22</th><th>100.00<br>1024.00 MB<br>20</th><th>100.00<br>1024.00 MB<br>22</th></tr>
</table>
</details>

### Redis integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>4383</th><th>9014</th><th>4541</th><th>9299</th><th>4634</th><th>9442</th></tr>
<tr><td>VT</td><th>4424</th><th>9017</th><th>4524</th><th>9335</th><th>4637</th><th>9452</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>6022</th><th>12176</th><th>6161</th><th>12441</th><th>6242</th><th>12849</th></tr>
<tr><td>VT</td><th>6015</th><th>12193</th><th>6168</th><th>12476</th><th>6253</th><th>12785</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4672</th><th>11038</th><th>4315</th><th>10781</th><th>3869</th><th>10292</th></tr>
<tr><td>VT</td><th>6564</th><th>13462</th><th>6445</th><th>13129</th><th>5904</th><th>12863</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>4607</th><th>9259</th><th>4683</th><th>9403</th><th>4662</th><th>9393</th></tr>
<tr><td>VT</td><th>4605</th><th>9252</th><th>4690</th><th>9343</th><th>4681</th><th>9420</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>7314</th><th>14734</th><th>7591</th><th>15131</th><th>7639</th><th>15327</th></tr>
<tr><td>VT</td><th>7333</th><th>14789</th><th>7477</th><th>15083</th><th>7672</th><th>15432</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4307</th><th>10785</th><th>4356</th><th>10722</th><th>3826</th><th>9341</th></tr>
<tr><td>VT</td><th>4286</th><th>10753</th><th>4393</th><th>10842</th><th>3871</th><th>9315</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>525989</th><th>1081688</th><th>545050</th><th>1115550</th><th>556043</th><th>1132737</th></tr>
<tr><td>VT</td><th>531036</th><th>1081986</th><th>542924</th><th>1119913</th><th>556413</th><th>1133990</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>722304</th><th>1460478</th><th>739382</th><th>1492292</th><th>748954</th><th>1541816</th></tr>
<tr><td>VT</td><th>721775</th><th>1462545</th><th>739956</th><th>1496700</th><th>750256</th><th>1534085</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>560644</th><th>1324427</th><th>517735</th><th>1293607</th><th>464441</th><th>1234848</th></tr>
<tr><td>VT</td><th>787389</th><th>1614879</th><th>773322</th><th>1574829</th><th>708439</th><th>1543482</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>552627</th><th>1110694</th><th>561997</th><th>1127961</th><th>559680</th><th>1126854</th></tr>
<tr><td>VT</td><th>552504</th><th>1109943</th><th>562637</th><th>1120774</th><th>561642</th><th>1129950</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>877663</th><th>1767508</th><th>910716</th><th>1815054</th><th>916570</th><th>1839120</th></tr>
<tr><td>VT</td><th>879866</th><th>1774677</th><th>897197</th><th>1809458</th><th>920452</th><th>1851447</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>516907</th><th>1293711</th><th>522841</th><th>1286308</th><th>459235</th><th>1120623</th></tr>
<tr><td>VT</td><th>514439</th><th>1289933</th><th>527076</th><th>1300653</th><th>464625</th><th>1117620</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>0.128</th><th>0.089</th><th>0.059</th><th>0.106</th><th>0.059</th><th>0.077</th></tr>
<tr><td>VT</td><th>0.062</th><th>0.06</th><th>0.063</th><th>0.06</th><th>0.061</th><th>0.061</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.084</th><th>0.065</th><th>0.059</th><th>0.06</th><th>0.052</th><th>0.071</th></tr>
<tr><td>VT</td><th>0.048</th><th>0.047</th><th>0.051</th><th>0.049</th><th>0.047</th><th>0.047</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.912</th><th>2.599</th><th>4.929</th><th>2.386</th><th>5.207</th><th>2.497</th></tr>
<tr><td>VT</td><th>4.734</th><th>2.432</th><th>4.818</th><th>2.499</th><th>4.783</th><th>2.401</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>0.048</th><th>0.062</th><th>0.057</th><th>0.047</th><th>0.048</th><th>0.06</th></tr>
<tr><td>VT</td><th>0.049</th><th>0.045</th><th>0.046</th><th>0.048</th><th>0.048</th><th>0.048</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.052</th><th>0.038</th><th>0.05</th><th>0.051</th><th>0.05</th><th>0.051</th></tr>
<tr><td>VT</td><th>0.035</th><th>0.036</th><th>0.037</th><th>0.036</th><th>0.035</th><th>0.037</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.366</th><th>2.296</th><th>4.641</th><th>2.381</th><th>4.604</th><th>2.273</th></tr>
<tr><td>VT</td><th>4.404</th><th>2.155</th><th>4.507</th><th>2.424</th><th>4.506</th><th>2.329</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>64</th><th>100.00<br>546.00 MB<br>66</th><th>100.00<br>546.00 MB<br>115</th><th>100.00<br>546.00 MB<br>116</th><th>100.00<br>546.00 MB<br>212</th><th>100.00<br>546.00 MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>64</th><th>100.00<br>546.00 MB<br>66</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>115</th><th>100.00<br>546.00 MB<br>212</th><th>100.00<br>546.00 MB<br>212</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>546.00 MB<br>64</th><th>100.00<br>546.00 MB<br>67</th><th>100.00<br>546.00 MB<br>116</th><th>100.00<br>546.00 MB<br>115</th><th>100.00<br>546.00 MB<br>212</th><th>100.00<br>546.00 MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>66</th><th>100.00<br>546.00 MB<br>66</th><th>100.00<br>546.00 MB<br>114</th><th>100.00<br>546.00 MB<br>115</th><th>100.00<br>546.00 MB<br>212</th><th>100.00<br>546.00 MB<br>212</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>69</th><th>100.00<br>1024.00 MB<br>69</th><th>100.00<br>1024.00 MB<br>118</th><th>100.00<br>1024.00 MB<br>119</th><th>100.00<br>1024.00 MB<br>216</th><th>100.00<br>1024.00 MB<br>216</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>19</th><th>100.00<br>1024.00 MB<br>19</th><th>100.00<br>1024.00 MB<br>19</th><th>100.00<br>1024.00 MB<br>19</th><th>100.00<br>1024.00 MB<br>19</th><th>100.00<br>1024.00 MB<br>19</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th><th>100.00<br>546.00 MB<br>14</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th><th>100.00<br>1024.00 MB<br>18</th></tr>
</table>
</details>

### Http integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>1896</th><th>3879</th><th>1913</th><th>3912</th><th>1896</th><th>3919</th></tr>
<tr><td>VT</td><th>1895</th><th>3876</th><th>1911</th><th>3915</th><th>1908</th><th>3903</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>2470</th><th>5050</th><th>2494</th><th>5070</th><th>2452</th><th>5092</th></tr>
<tr><td>VT</td><th>2459</th><th>5037</th><th>2481</th><th>5086</th><th>2479</th><th>5105</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>1515</th><th>3819</th><th>1294</th><th>3685</th><th>1060</th><th>3403</th></tr>
<tr><td>VT</td><th>2316</th><th>4526</th><th>2161</th><th>4550</th><th>1957</th><th>4567</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>2775</th><th>5775</th><th>2789</th><th>5749</th><th>2784</th><th>5737</th></tr>
<tr><td>VT</td><th>2768</th><th>5741</th><th>2805</th><th>5749</th><th>2784</th><th>5725</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>4111</th><th>8510</th><th>4130</th><th>8402</th><th>4120</th><th>8477</th></tr>
<tr><td>VT</td><th>4168</th><th>8494</th><th>4144</th><th>8523</th><th>4110</th><th>8469</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2371</th><th>6405</th><th>2230</th><th>5933</th><th>1831</th><th>5036</th></tr>
<tr><td>VT</td><th>2397</th><th>6231</th><th>2151</th><th>5995</th><th>1880</th><th>4976</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>227684</th><th>465535</th><th>229592</th><th>469282</th><th>227533</th><th>470455</th></tr>
<tr><td>VT</td><th>227444</th><th>464998</th><th>229439</th><th>469901</th><th>229058</th><th>468337</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>296295</th><th>606055</th><th>299310</th><th>608499</th><th>294326</th><th>611118</th></tr>
<tr><td>VT</td><th>295185</th><th>604409</th><th>297709</th><th>610068</th><th>297665</th><th>612691</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>181811</th><th>458219</th><th>155425</th><th>442140</th><th>127403</th><th>408422</th></tr>
<tr><td>VT</td><th>277946</th><th>542979</th><th>259262</th><th>545865</th><th>234928</th><th>547894</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>333077</th><th>692805</th><th>334661</th><th>690008</th><th>334150</th><th>688279</th></tr>
<tr><td>VT</td><th>332203</th><th>688772</th><th>336667</th><th>689674</th><th>334231</th><th>686945</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>493243</th><th>1021212</th><th>495576</th><th>1008325</th><th>494548</th><th>1017230</th></tr>
<tr><td>VT</td><th>499991</th><th>1018955</th><th>497129</th><th>1022592</th><th>493398</th><th>1016365</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>284552</th><th>768385</th><th>267715</th><th>711734</th><th>219798</th><th>604205</th></tr>
<tr><td>VT</td><th>287766</th><th>747727</th><th>258097</th><th>719204</th><th>225695</th><th>597080</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>0.1</th><th>0.074</th><th>0.064</th><th>0.072</th><th>0.051</th><th>0.064</th></tr>
<tr><td>VT</td><th>0.051</th><th>0.051</th><th>0.05</th><th>0.05</th><th>0.049</th><th>0.049</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.046</th><th>0.043</th><th>0.048</th><th>0.056</th><th>0.04</th><th>0.067</th></tr>
<tr><td>VT</td><th>0.04</th><th>0.039</th><th>0.04</th><th>0.041</th><th>0.038</th><th>0.039</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.228</th><th>2.102</th><th>4.47</th><th>2.06</th><th>4.3</th><th>2.184</th></tr>
<tr><td>VT</td><th>4.223</th><th>2.207</th><th>4.585</th><th>2.099</th><th>4.295</th><th>2.102</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>0.088</th><th>0.05</th><th>0.047</th><th>0.038</th><th>0.062</th><th>0.065</th></tr>
<tr><td>VT</td><th>0.038</th><th>0.04</th><th>0.038</th><th>0.038</th><th>0.042</th><th>0.039</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.059</th><th>0.04</th><th>0.047</th><th>0.038</th><th>0.047</th><th>0.047</th></tr>
<tr><td>VT</td><th>0.03</th><th>0.031</th><th>0.031</th><th>0.031</th><th>0.032</th><th>0.033</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.496</th><th>2.015</th><th>4.212</th><th>2.073</th><th>3.84</th><th>2.053</th></tr>
<tr><td>VT</td><th>4.007</th><th>1.99</th><th>3.984</th><th>1.959</th><th>3.885</th><th>2.059</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>213</th><th>99.70<br>546.00 MB<br>219</th><th>100.00<br>546.00 MB<br>407</th><th>100.00<br>546.00 MB<br>425</th><th>100.00<br>NaN B<br>701</th><th>100.00<br>546.00 MB<br>625</th></tr>
<tr><td>VT</td><th>99.86<br>546.00 MB<br>220</th><th>100.00<br>546.00 MB<br>214</th><th>100.00<br>546.00 MB<br>414</th><th>100.00<br>546.00 MB<br>418</th><th>100.00<br>546.00 MB<br>697</th><th>100.00<br>546.00 MB<br>633</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>99.02<br>546.00 MB<br>211</th><th>100.00<br>546.00 MB<br>200</th><th>100.00<br>NaN B<br>398</th><th>100.00<br>546.00 MB<br>410</th><th>100.00<br>546.00 MB<br>664</th><th>100.00<br>546.00 MB<br>639</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>217</th><th>100.00<br>546.00 MB<br>208</th><th>100.00<br>546.00 MB<br>404</th><th>100.00<br>546.00 MB<br>396</th><th>100.00<br>546.00 MB<br>608</th><th>100.00<br>546.00 MB<br>670</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>209</th><th>100.00<br>1024.00 MB<br>204</th><th>100.00<br>1024.00 MB<br>384</th><th>100.00<br>1024.00 MB<br>385</th><th>100.00<br>1024.00 MB<br>546</th><th>100.00<br>1024.00 MB<br>544</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>41</th><th>100.00<br>1024.00 MB<br>48</th><th>100.00<br>1024.00 MB<br>66</th><th>100.00<br>1024.00 MB<br>61</th><th>100.00<br>1024.00 MB<br>106</th><th>100.00<br>1024.00 MB<br>124</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th><th>100.00<br>546.00 MB<br>10</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th><th>100.00<br>1024.00 MB<br>14</th></tr>
</table>
</details>