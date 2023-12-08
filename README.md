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
wfx-redis               jvm_VT         235MB
wfx-redis               jvm_RT         235MB
wfx-redis               gvm-pgo_VT     114MB
wfx-redis               gvm-pgo_RT     114MB
wfx-redis               gvm_VT         141MB
wfx-redis               gvm_RT         141MB
wfx-r2dbc               jvm_VT         234MB
wfx-r2dbc               jvm_RT         234MB
wfx-r2dbc               gvm-pgo_VT     104MB
wfx-r2dbc               gvm-pgo_RT     104MB
wfx-r2dbc               gvm_VT         124MB
wfx-r2dbc               gvm_RT         124MB
wfx-mongo               jvm_VT         236MB
wfx-mongo               jvm_RT         236MB
wfx-mongo               gvm-pgo_VT     107MB
wfx-mongo               gvm-pgo_RT     107MB
wfx-mongo               gvm_VT         127MB
wfx-mongo               gvm_RT         127MB
wfx-http                jvm_VT         229MB
wfx-http                jvm_RT         229MB
wfx-http                gvm-pgo_VT     96.6MB
wfx-http                gvm-pgo_RT     96.6MB
wfx-http                gvm_VT         116MB
wfx-http                gvm_RT         116MB
wfx                     jvm_VT         229MB
wfx                     jvm_RT         229MB
wfx                     gvm-pgo_VT     93.8MB
wfx                     gvm-pgo_RT     93.7MB
wfx                     gvm_VT         113MB
wfx                     gvm_RT         113MB
web-redis               jvm_VT         237MB
web-redis               jvm_RT         237MB
web-redis               gvm-pgo_VT     118MB
web-redis               gvm-pgo_RT     118MB
web-redis               gvm_VT         146MB
web-redis               gvm_RT         146MB
web-mongo               jvm_VT         233MB
web-mongo               jvm_RT         233MB
web-mongo               gvm-pgo_VT     105MB
web-mongo               gvm-pgo_RT     105MB
web-mongo               gvm_VT         125MB
web-mongo               gvm_RT         125MB
web-jdbc                jvm_VT         228MB
web-jdbc                jvm_RT         228MB
web-jdbc                gvm-pgo_VT     98.5MB
web-jdbc                gvm-pgo_RT     98.5MB
web-jdbc                gvm_VT         117MB
web-jdbc                gvm_RT         117MB
web-http                jvm_VT         226MB
web-http                jvm_RT         226MB
web-http                gvm-pgo_VT     94.9MB
web-http                gvm-pgo_RT     94.7MB
web-http                gvm_VT         113MB
web-http                gvm_RT         113MB
web                     jvm_VT         226MB
web                     jvm_RT         226MB
web                     gvm-pgo_VT     92.7MB
web                     gvm-pgo_RT     92.7MB
web                     gvm_VT         111MB
web                     gvm_RT         111MB
```
> [GraalVM Builds require setting thread-type at compilation time](https://stackoverflow.com/questions/77550123/spring-native-build-not-using-virtual-threads) so the same image has a real-thread and virtual-thread variant (the jvm ones do not requiere it).

> took like 4 hours to complete... real	217m15,199s user	0m38,620s sys	0m30,329s 

## Stressing test apps

``` bash
bash stress_all.sh
```
> took like 15 hours to complete... real 896m16,947s user 0m59,534s sys 0m20,663s

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
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>6676</th><th>13401</th><th>6723</th><th>13521</th><th>6782</th><th>13525</th></tr>
<tr><td>VT</td><th>6410</th><th>12676</th><th>6730</th><th>13200</th><th>6963</th><th>13686</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>9245</th><th>18401</th><th>9319</th><th>18443</th><th>9403</th><th>18169</th></tr>
<tr><td>VT</td><th>8813</th><th>18053</th><th>9245</th><th>18137</th><th>9621</th><th>17643</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>7710</th><th>17387</th><th>6942</th><th>17231</th><th>5158</th><th>15874</th></tr>
<tr><td>VT</td><th>10996</th><th>20304</th><th>10376</th><th>20398</th><th>9412</th><th>19986</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>6927</th><th>13871</th><th>6882</th><th>13732</th><th>6803</th><th>13620</th></tr>
<tr><td>VT</td><th>6928</th><th>13836</th><th>6899</th><th>13789</th><th>6793</th><th>13694</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>10881</th><th>21844</th><th>10729</th><th>21559</th><th>10494</th><th>21221</th></tr>
<tr><td>VT</td><th>10678</th><th>21503</th><th>10567</th><th>21343</th><th>10379</th><th>20919</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>6699</th><th>16192</th><th>6532</th><th>16208</th><th>6704</th><th>15593</th></tr>
<tr><td>VT</td><th>6699</th><th>16204</th><th>6680</th><th>16098</th><th>6531</th><th>15841</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>801263</th><th>1607807</th><th>806466</th><th>1621717</th><th>813802</th><th>1622654</th></tr>
<tr><td>VT</td><th>769180</th><th>1520544</th><th>807350</th><th>1584136</th><th>835788</th><th>1642063</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>1109136</th><th>2207153</th><th>1118507</th><th>2212913</th><th>1128176</th><th>2179795</th></tr>
<tr><td>VT</td><th>1057176</th><th>2165670</th><th>1109510</th><th>2175763</th><th>1154514</th><th>2116906</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>924897</th><th>2085646</th><th>832799</th><th>2067218</th><th>618824</th><th>1904129</th></tr>
<tr><td>VT</td><th>1319714</th><th>2435335</th><th>1244775</th><th>2446357</th><th>1129212</th><th>2397663</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>831395</th><th>1663991</th><th>825746</th><th>1647189</th><th>816393</th><th>1633774</th></tr>
<tr><td>VT</td><th>831470</th><th>1659624</th><th>827995</th><th>1654034</th><th>815513</th><th>1643034</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>1305511</th><th>2620291</th><th>1287864</th><th>2586574</th><th>1259655</th><th>2545868</th></tr>
<tr><td>VT</td><th>1280636</th><th>2579359</th><th>1268014</th><th>2560006</th><th>1245148</th><th>2510839</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>803824</th><th>1942313</th><th>783902</th><th>1944345</th><th>804555</th><th>1870138</th></tr>
<tr><td>VT</td><th>803652</th><th>1943742</th><th>801274</th><th>1931481</th><th>783822</th><th>1900350</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>0.099</th><th>0.072</th><th>0.047</th><th>0.142</th><th>0.072</th><th>0.072</th></tr>
<tr><td>VT</td><th>0.08</th><th>0.051</th><th>0.05</th><th>0.078</th><th>0.138</th><th>0.132</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.122</th><th>0.039</th><th>0.038</th><th>0.062</th><th>0.044</th><th>0.039</th></tr>
<tr><td>VT</td><th>0.066</th><th>0.053</th><th>0.04</th><th>0.109</th><th>0.05</th><th>0.045</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.113</th><th>2.121</th><th>4.268</th><th>2.018</th><th>4.132</th><th>2.104</th></tr>
<tr><td>VT</td><th>4.196</th><th>2.113</th><th>4.097</th><th>2.094</th><th>4.283</th><th>2.191</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>0.117</th><th>0.061</th><th>0.049</th><th>0.057</th><th>0.084</th><th>0.062</th></tr>
<tr><td>VT</td><th>0.063</th><th>0.068</th><th>0.113</th><th>0.051</th><th>0.041</th><th>0.044</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.057</th><th>0.05</th><th>0.11</th><th>0.043</th><th>0.04</th><th>0.06</th></tr>
<tr><td>VT</td><th>0.056</th><th>0.065</th><th>0.038</th><th>0.044</th><th>0.032</th><th>0.038</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>3.947</th><th>2.064</th><th>4.078</th><th>2.022</th><th>4.287</th><th>2.207</th></tr>
<tr><td>VT</td><th>4.175</th><th>2.112</th><th>4.03</th><th>2.011</th><th>4.088</th><th>2.023</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>57.50MB<br>61</th><th>100.00<br>66.50MB<br>61</th><th>100.00<br>76.50MB<br>112</th><th>100.00<br>84.50MB<br>113</th><th>100.00<br>156.50MB<br>208</th><th>100.00<br>133.50MB<br>208</th></tr>
<tr><td>VT</td><th>100.00<br>51.50MB<br>16</th><th>100.00<br>46.50MB<br>16</th><th>100.00<br>51.00MB<br>16</th><th>100.00<br>49.50MB<br>16</th><th>100.00<br>81.50MB<br>16</th><th>100.00<br>124.50MB<br>16</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>49.00MB<br>61</th><th>100.00<br>48.00MB<br>67</th><th>100.00<br>89.00MB<br>113</th><th>100.00<br>79.00MB<br>110</th><th>100.00<br>150.50MB<br>208</th><th>100.00<br>166.50MB<br>208</th></tr>
<tr><td>VT</td><th>100.00<br>72.50MB<br>16</th><th>100.00<br>68.00MB<br>16</th><th>100.00<br>59.50MB<br>16</th><th>100.00<br>51.00MB<br>16</th><th>100.00<br>141.50MB<br>16</th><th>100.00<br>76.50MB<br>16</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>117.14MB<br>67</th><th>100.00<br>118.88MB<br>67</th><th>100.00<br>133.49MB<br>117</th><th>100.00<br>133.48MB<br>116</th><th>100.00<br>158.61MB<br>212</th><th>100.00<br>165.33MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>105.82MB<br>15</th><th>100.00<br>105.32MB<br>14</th><th>100.00<br>106.62MB<br>14</th><th>100.00<br>108.06MB<br>14</th><th>100.00<br>111.67MB<br>14</th><th>100.00<br>112.90MB<br>14</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>60.00MB<br>10</th><th>100.00<br>30.00MB<br>10</th><th>100.00<br>47.00MB<br>10</th><th>100.00<br>41.00MB<br>10</th><th>100.00<br>64.50MB<br>10</th><th>100.00<br>45.50MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>36.50MB<br>10</th><th>100.00<br>58.00MB<br>10</th><th>100.00<br>49.50MB<br>10</th><th>100.00<br>36.00MB<br>10</th><th>100.00<br>73.50MB<br>10</th><th>100.00<br>42.50MB<br>10</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>75.00MB<br>10</th><th>100.00<br>46.00MB<br>10</th><th>100.00<br>116.00MB<br>10</th><th>100.00<br>41.00MB<br>10</th><th>100.00<br>70.00MB<br>10</th><th>100.00<br>62.50MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>47.50MB<br>10</th><th>100.00<br>44.00MB<br>10</th><th>100.00<br>47.50MB<br>10</th><th>100.00<br>51.50MB<br>10</th><th>100.00<br>74.00MB<br>10</th><th>100.00<br>53.00MB<br>10</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>101.14MB<br>14</th><th>100.00<br>101.14MB<br>14</th><th>100.00<br>101.43MB<br>14</th><th>100.00<br>101.06MB<br>14</th><th>100.00<br>101.83MB<br>14</th><th>100.00<br>102.39MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>101.14MB<br>14</th><th>100.00<br>101.39MB<br>14</th><th>100.00<br>101.78MB<br>14</th><th>100.00<br>102.17MB<br>14</th><th>100.00<br>101.73MB<br>14</th><th>100.00<br>102.39MB<br>14</th></tr>
</table>
</details>

### PostgreSQL integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>4082</th><th>8045</th><th>4317</th><th>8636</th><th>4334</th><th>8817</th></tr>
<tr><td>VT</td><th>4159</th><th>8239</th><th>4140</th><th>8532</th><th>4151</th><th>8510</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>5085</th><th>10634</th><th>5509</th><th>10404</th><th>5605</th><th>11336</th></tr>
<tr><td>VT</td><th>5319</th><th>10878</th><th>5387</th><th>10883</th><th>5300</th><th>10944</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4098</th><th>9833</th><th>4329</th><th>9334</th><th>3982</th><th>9833</th></tr>
<tr><td>VT</td><th>6134</th><th>11706</th><th>6028</th><th>11523</th><th>5988</th><th>11320</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>3496</th><th>7067</th><th>3500</th><th>7081</th><th>3480</th><th>7061</th></tr>
<tr><td>VT</td><th>3469</th><th>6994</th><th>3493</th><th>7040</th><th>3473</th><th>7031</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>5186</th><th>10384</th><th>5211</th><th>10548</th><th>5237</th><th>10566</th></tr>
<tr><td>VT</td><th>5207</th><th>10442</th><th>5251</th><th>10570</th><th>5230</th><th>10600</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2233</th><th>6829</th><th>2346</th><th>6846</th><th>2220</th><th>6367</th></tr>
<tr><td>VT</td><th>2248</th><th>6783</th><th>2119</th><th>6795</th><th>2086</th><th>6247</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>489836</th><th>965039</th><th>518165</th><th>1035968</th><th>520139</th><th>1058297</th></tr>
<tr><td>VT</td><th>498956</th><th>988483</th><th>496618</th><th>1024008</th><th>498270</th><th>1020993</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>610323</th><th>1275625</th><th>661130</th><th>1248100</th><th>672809</th><th>1360090</th></tr>
<tr><td>VT</td><th>638096</th><th>1305098</th><th>646544</th><th>1306003</th><th>636200</th><th>1313121</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>491722</th><th>1179301</th><th>519354</th><th>1120040</th><th>478008</th><th>1179944</th></tr>
<tr><td>VT</td><th>735834</th><th>1404190</th><th>723507</th><th>1382354</th><th>718704</th><th>1358248</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>419584</th><th>848458</th><th>419994</th><th>849643</th><th>417700</th><th>847227</th></tr>
<tr><td>VT</td><th>416191</th><th>838915</th><th>419025</th><th>844533</th><th>416800</th><th>843351</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>622122</th><th>1245579</th><th>625252</th><th>1265479</th><th>628535</th><th>1268032</th></tr>
<tr><td>VT</td><th>625059</th><th>1252587</th><th>629958</th><th>1268158</th><th>627480</th><th>1271668</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>267967</th><th>819316</th><th>281606</th><th>821618</th><th>266318</th><th>763982</th></tr>
<tr><td>VT</td><th>269786</th><th>813748</th><th>254311</th><th>815131</th><th>250493</th><th>749536</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>0.086</th><th>0.08</th><th>0.069</th><th>0.083</th><th>0.071</th><th>0.091</th></tr>
<tr><td>VT</td><th>0.118</th><th>0.102</th><th>0.07</th><th>0.089</th><th>0.083</th><th>0.108</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.082</th><th>0.06</th><th>0.059</th><th>0.084</th><th>0.058</th><th>0.062</th></tr>
<tr><td>VT</td><th>0.087</th><th>0.057</th><th>0.058</th><th>0.085</th><th>0.064</th><th>0.084</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.519</th><th>2.286</th><th>4.799</th><th>2.252</th><th>4.596</th><th>2.364</th></tr>
<tr><td>VT</td><th>4.5</th><th>2.238</th><th>4.528</th><th>2.28</th><th>4.222</th><th>2.303</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>0.109</th><th>0.085</th><th>0.057</th><th>0.084</th><th>0.136</th><th>0.082</th></tr>
<tr><td>VT</td><th>0.156</th><th>0.083</th><th>0.054</th><th>0.083</th><th>0.087</th><th>0.121</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.055</th><th>0.071</th><th>0.057</th><th>0.066</th><th>0.056</th><th>0.058</th></tr>
<tr><td>VT</td><th>0.06</th><th>0.057</th><th>0.043</th><th>0.06</th><th>0.101</th><th>0.064</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.595</th><th>2.381</th><th>4.686</th><th>2.394</th><th>4.834</th><th>2.451</th></tr>
<tr><td>VT</td><th>4.69</th><th>2.37</th><th>4.803</th><th>2.335</th><th>4.726</th><th>2.422</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>59.50MB<br>64</th><th>100.00<br>54.00MB<br>65</th><th>100.00<br>91.50MB<br>113</th><th>100.00<br>118.50MB<br>114</th><th>100.00<br>171.50MB<br>211</th><th>100.00<br>188.00MB<br>211</th></tr>
<tr><td>VT</td><th>100.00<br>61.50MB<br>24</th><th>100.00<br>75.50MB<br>24</th><th>100.00<br>74.00MB<br>24</th><th>100.00<br>116.00MB<br>24</th><th>100.00<br>195.50MB<br>24</th><th>100.00<br>138.00MB<br>24</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>57.00MB<br>63</th><th>100.00<br>53.50MB<br>64</th><th>100.00<br>95.00MB<br>114</th><th>100.00<br>96.00MB<br>114</th><th>100.00<br>192.50MB<br>211</th><th>100.00<br>166.50MB<br>211</th></tr>
<tr><td>VT</td><th>100.00<br>52.50MB<br>24</th><th>100.00<br>69.50MB<br>24</th><th>100.00<br>77.50MB<br>24</th><th>100.00<br>110.50MB<br>24</th><th>100.00<br>113.50MB<br>24</th><th>100.00<br>180.50MB<br>24</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>133.10MB<br>69</th><th>100.00<br>134.61MB<br>69</th><th>100.00<br>149.26MB<br>118</th><th>100.00<br>149.88MB<br>117</th><th>100.00<br>179.96MB<br>215</th><th>100.00<br>182.12MB<br>215</th></tr>
<tr><td>VT</td><th>100.00<br>130.54MB<br>23</th><th>100.00<br>131.13MB<br>22</th><th>100.00<br>145.54MB<br>22</th><th>100.00<br>148.14MB<br>22</th><th>100.00<br>173.52MB<br>23</th><th>100.00<br>175.29MB<br>22</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>53.00MB<br>16</th><th>100.00<br>114.00MB<br>16</th><th>100.00<br>68.50MB<br>16</th><th>100.00<br>74.00MB<br>16</th><th>100.00<br>82.50MB<br>16</th><th>100.00<br>94.50MB<br>16</th></tr>
<tr><td>VT</td><th>100.00<br>65.00MB<br>16</th><th>100.00<br>46.50MB<br>16</th><th>100.00<br>70.00MB<br>16</th><th>100.00<br>90.50MB<br>16</th><th>100.00<br>91.00MB<br>16</th><th>100.00<br>89.00MB<br>16</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>75.00MB<br>16</th><th>100.00<br>57.50MB<br>16</th><th>100.00<br>54.00MB<br>16</th><th>100.00<br>85.00MB<br>16</th><th>100.00<br>121.50MB<br>16</th><th>100.00<br>118.50MB<br>16</th></tr>
<tr><td>VT</td><th>100.00<br>73.00MB<br>16</th><th>100.00<br>46.00MB<br>16</th><th>100.00<br>87.00MB<br>16</th><th>100.00<br>109.00MB<br>16</th><th>100.00<br>96.50MB<br>16</th><th>100.00<br>110.50MB<br>16</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>118.79MB<br>20</th><th>100.00<br>120.51MB<br>20</th><th>100.00<br>133.06MB<br>20</th><th>100.00<br>132.88MB<br>20</th><th>100.00<br>137.96MB<br>20</th><th>100.00<br>140.07MB<br>20</th></tr>
<tr><td>VT</td><th>100.00<br>119.25MB<br>20</th><th>100.00<br>121.33MB<br>20</th><th>100.00<br>133.27MB<br>20</th><th>100.00<br>134.45MB<br>20</th><th>100.00<br>138.36MB<br>20</th><th>100.00<br>140.09MB<br>20</th></tr>
</table>
</details>

### MongoDB integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>2952</th><th>5954</th><th>2953</th><th>6028</th><th>2963</th><th>6026</th></tr>
<tr><td>VT</td><th>2936</th><th>5882</th><th>3043</th><th>6092</th><th>2974</th><th>6056</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>4017</th><th>8083</th><th>4072</th><th>8235</th><th>4049</th><th>8161</th></tr>
<tr><td>VT</td><th>3955</th><th>7717</th><th>4070</th><th>8107</th><th>4020</th><th>8077</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2669</th><th>7197</th><th>2695</th><th>7215</th><th>2383</th><th>6834</th></tr>
<tr><td>VT</td><th>4305</th><th>8801</th><th>3834</th><th>8660</th><th>3489</th><th>8254</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>2613</th><th>5258</th><th>2648</th><th>5352</th><th>2612</th><th>5401</th></tr>
<tr><td>VT</td><th>2627</th><th>5258</th><th>2647</th><th>5365</th><th>2612</th><th>5404</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>3994</th><th>7943</th><th>4095</th><th>8209</th><th>4093</th><th>8423</th></tr>
<tr><td>VT</td><th>3925</th><th>7797</th><th>4005</th><th>8046</th><th>3992</th><th>8241</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>1795</th><th>5611</th><th>1784</th><th>5567</th><th>1404</th><th>5162</th></tr>
<tr><td>VT</td><th>1805</th><th>5485</th><th>1742</th><th>5523</th><th>1421</th><th>5064</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>354187</th><th>714297</th><th>354472</th><th>723090</th><th>355645</th><th>723154</th></tr>
<tr><td>VT</td><th>352289</th><th>705903</th><th>365154</th><th>731124</th><th>357021</th><th>726535</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>482157</th><th>969873</th><th>488554</th><th>987909</th><th>486003</th><th>979400</th></tr>
<tr><td>VT</td><th>474576</th><th>925726</th><th>488609</th><th>972622</th><th>482499</th><th>969100</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>320353</th><th>863511</th><th>323394</th><th>865570</th><th>286044</th><th>819928</th></tr>
<tr><td>VT</td><th>516529</th><th>1055700</th><th>459918</th><th>1038928</th><th>418790</th><th>990332</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>313667</th><th>630890</th><th>317675</th><th>642219</th><th>313567</th><th>648314</th></tr>
<tr><td>VT</td><th>315238</th><th>630991</th><th>317748</th><th>643892</th><th>313562</th><th>648607</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>479167</th><th>952827</th><th>491375</th><th>985145</th><th>491392</th><th>1010431</th></tr>
<tr><td>VT</td><th>471158</th><th>935452</th><th>480452</th><th>965143</th><th>479217</th><th>988763</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>215387</th><th>673158</th><th>214144</th><th>667888</th><th>168488</th><th>619414</th></tr>
<tr><td>VT</td><th>216633</th><th>658029</th><th>209121</th><th>662504</th><th>170547</th><th>607879</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>0.122</th><th>0.1</th><th>0.079</th><th>0.07</th><th>0.086</th><th>0.108</th></tr>
<tr><td>VT</td><th>0.073</th><th>0.09</th><th>0.06</th><th>0.067</th><th>0.061</th><th>0.081</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.088</th><th>0.056</th><th>0.047</th><th>0.084</th><th>0.056</th><th>0.062</th></tr>
<tr><td>VT</td><th>0.074</th><th>0.068</th><th>0.068</th><th>0.07</th><th>0.074</th><th>0.076</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.017</th><th>2.509</th><th>4.904</th><th>2.532</th><th>5.014</th><th>2.554</th></tr>
<tr><td>VT</td><th>5.189</th><th>2.57</th><th>5.019</th><th>2.707</th><th>4.804</th><th>2.479</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>0.096</th><th>0.068</th><th>0.046</th><th>0.087</th><th>0.061</th><th>0.064</th></tr>
<tr><td>VT</td><th>0.1</th><th>0.067</th><th>0.054</th><th>0.074</th><th>0.08</th><th>0.062</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.06</th><th>0.08</th><th>0.059</th><th>0.056</th><th>0.059</th><th>0.06</th></tr>
<tr><td>VT</td><th>0.055</th><th>0.057</th><th>0.058</th><th>0.041</th><th>0.053</th><th>0.06</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.102</th><th>2.629</th><th>5.014</th><th>2.56</th><th>5.031</th><th>2.442</th></tr>
<tr><td>VT</td><th>5.109</th><th>2.418</th><th>4.913</th><th>2.415</th><th>4.92</th><th>2.477</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>61.50MB<br>65</th><th>100.00<br>54.50MB<br>65</th><th>100.00<br>108.00MB<br>115</th><th>100.00<br>117.00MB<br>115</th><th>100.00<br>162.50MB<br>212</th><th>100.00<br>171.50MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>58.50MB<br>25</th><th>100.00<br>68.50MB<br>25</th><th>100.00<br>102.50MB<br>25</th><th>100.00<br>126.00MB<br>25</th><th>100.00<br>195.50MB<br>25</th><th>100.00<br>216.00MB<br>25</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>57.50MB<br>66</th><th>100.00<br>82.00MB<br>65</th><th>100.00<br>91.50MB<br>117</th><th>100.00<br>138.50MB<br>115</th><th>100.00<br>156.00MB<br>212</th><th>100.00<br>184.50MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>53.50MB<br>25</th><th>100.00<br>79.00MB<br>24</th><th>100.00<br>103.00MB<br>25</th><th>100.00<br>124.00MB<br>25</th><th>100.00<br>229.50MB<br>25</th><th>100.00<br>294.00MB<br>25</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>145.05MB<br>70</th><th>100.00<br>145.21MB<br>71</th><th>100.00<br>160.93MB<br>118</th><th>100.00<br>161.15MB<br>118</th><th>100.00<br>190.20MB<br>216</th><th>100.00<br>192.26MB<br>216</th></tr>
<tr><td>VT</td><th>100.00<br>143.36MB<br>23</th><th>100.00<br>142.52MB<br>22</th><th>100.00<br>157.83MB<br>23</th><th>100.00<br>157.10MB<br>23</th><th>100.00<br>187.34MB<br>23</th><th>100.00<br>191.54MB<br>23</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>93.00MB<br>16</th><th>100.00<br>63.00MB<br>18</th><th>100.00<br>103.50MB<br>16</th><th>100.00<br>86.00MB<br>18</th><th>100.00<br>149.00MB<br>16</th><th>100.00<br>173.50MB<br>18</th></tr>
<tr><td>VT</td><th>100.00<br>119.50MB<br>16</th><th>100.00<br>62.00MB<br>18</th><th>100.00<br>128.00MB<br>16</th><th>100.00<br>95.00MB<br>18</th><th>100.00<br>113.00MB<br>16</th><th>100.00<br>106.50MB<br>18</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>73.50MB<br>16</th><th>100.00<br>63.00MB<br>18</th><th>100.00<br>172.00MB<br>16</th><th>100.00<br>97.00MB<br>18</th><th>100.00<br>220.50MB<br>16</th><th>100.00<br>165.50MB<br>18</th></tr>
<tr><td>VT</td><th>100.00<br>78.00MB<br>16</th><th>100.00<br>66.50MB<br>18</th><th>100.00<br>110.00MB<br>16</th><th>100.00<br>91.00MB<br>18</th><th>100.00<br>121.50MB<br>16</th><th>100.00<br>224.00MB<br>18</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>124.45MB<br>20</th><th>100.00<br>125.44MB<br>22</th><th>100.00<br>143.85MB<br>20</th><th>100.00<br>146.20MB<br>22</th><th>100.00<br>146.38MB<br>20</th><th>100.00<br>150.13MB<br>22</th></tr>
<tr><td>VT</td><th>100.00<br>124.50MB<br>20</th><th>100.00<br>125.36MB<br>22</th><th>100.00<br>144.78MB<br>20</th><th>100.00<br>146.52MB<br>22</th><th>100.00<br>146.82MB<br>20</th><th>100.00<br>150.73MB<br>22</th></tr>
</table>
</details>

### Redis integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>4414</th><th>8936</th><th>4537</th><th>9301</th><th>4631</th><th>9435</th></tr>
<tr><td>VT</td><th>4429</th><th>9023</th><th>4513</th><th>9070</th><th>4645</th><th>9269</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>5970</th><th>12133</th><th>6170</th><th>12334</th><th>6214</th><th>12749</th></tr>
<tr><td>VT</td><th>6089</th><th>12576</th><th>6148</th><th>12600</th><th>6339</th><th>12716</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4619</th><th>11014</th><th>4415</th><th>10724</th><th>3987</th><th>10377</th></tr>
<tr><td>VT</td><th>6395</th><th>13034</th><th>6385</th><th>12835</th><th>5508</th><th>12669</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>4561</th><th>9106</th><th>4628</th><th>9229</th><th>4647</th><th>9341</th></tr>
<tr><td>VT</td><th>4571</th><th>9151</th><th>4637</th><th>9252</th><th>4669</th><th>9329</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>7236</th><th>14586</th><th>7452</th><th>14850</th><th>7538</th><th>15096</th></tr>
<tr><td>VT</td><th>7197</th><th>14683</th><th>7387</th><th>14917</th><th>7535</th><th>15146</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4316</th><th>10873</th><th>4466</th><th>10933</th><th>3855</th><th>9437</th></tr>
<tr><td>VT</td><th>4352</th><th>10703</th><th>4441</th><th>10943</th><th>4025</th><th>9396</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>529495</th><th>1072393</th><th>544265</th><th>1115718</th><th>556004</th><th>1132304</th></tr>
<tr><td>VT</td><th>531341</th><th>1082799</th><th>541634</th><th>1088174</th><th>557390</th><th>1112380</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>716201</th><th>1455858</th><th>740516</th><th>1479676</th><th>746188</th><th>1529991</th></tr>
<tr><td>VT</td><th>730412</th><th>1508612</th><th>737853</th><th>1511495</th><th>760926</th><th>1525893</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>554144</th><th>1321314</th><th>529786</th><th>1286501</th><th>478332</th><th>1244919</th></tr>
<tr><td>VT</td><th>767208</th><th>1563459</th><th>766086</th><th>1540290</th><th>660828</th><th>1520049</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>547267</th><th>1092432</th><th>555386</th><th>1107123</th><th>557635</th><th>1120664</th></tr>
<tr><td>VT</td><th>548364</th><th>1097685</th><th>556591</th><th>1110276</th><th>560345</th><th>1119271</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>868474</th><th>1749612</th><th>894144</th><th>1781448</th><th>904888</th><th>1811088</th></tr>
<tr><td>VT</td><th>863510</th><th>1761511</th><th>886224</th><th>1788938</th><th>903976</th><th>1817192</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>517951</th><th>1304308</th><th>535905</th><th>1311552</th><th>462605</th><th>1132258</th></tr>
<tr><td>VT</td><th>522085</th><th>1283940</th><th>532773</th><th>1312745</th><th>483013</th><th>1127524</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>0.08</th><th>0.091</th><th>0.1</th><th>0.128</th><th>0.063</th><th>0.098</th></tr>
<tr><td>VT</td><th>0.101</th><th>0.068</th><th>0.086</th><th>0.088</th><th>0.097</th><th>0.085</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.058</th><th>0.054</th><th>0.057</th><th>0.127</th><th>0.063</th><th>0.07</th></tr>
<tr><td>VT</td><th>0.052</th><th>0.053</th><th>0.086</th><th>0.063</th><th>0.056</th><th>0.075</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.202</th><th>2.572</th><th>5.289</th><th>2.519</th><th>5.39</th><th>2.656</th></tr>
<tr><td>VT</td><th>5.301</th><th>2.461</th><th>5.183</th><th>2.67</th><th>5.135</th><th>2.571</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>0.1</th><th>0.081</th><th>0.065</th><th>0.108</th><th>0.052</th><th>0.082</th></tr>
<tr><td>VT</td><th>0.08</th><th>0.065</th><th>0.148</th><th>0.075</th><th>0.064</th><th>0.137</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.081</th><th>0.054</th><th>0.047</th><th>0.058</th><th>0.046</th><th>0.049</th></tr>
<tr><td>VT</td><th>0.066</th><th>0.054</th><th>0.047</th><th>0.058</th><th>0.051</th><th>0.067</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.697</th><th>2.335</th><th>4.633</th><th>2.317</th><th>4.623</th><th>2.349</th></tr>
<tr><td>VT</td><th>4.621</th><th>2.344</th><th>4.629</th><th>2.34</th><th>4.816</th><th>2.419</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>64.50MB<br>64</th><th>100.00<br>58.50MB<br>65</th><th>100.00<br>113.00MB<br>115</th><th>100.00<br>106.00MB<br>115</th><th>100.00<br>178.00MB<br>212</th><th>100.00<br>219.50MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>73.00MB<br>21</th><th>100.00<br>69.50MB<br>21</th><th>100.00<br>89.00MB<br>21</th><th>100.00<br>125.50MB<br>21</th><th>100.00<br>191.00MB<br>21</th><th>100.00<br>203.00MB<br>21</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>66.50MB<br>65</th><th>100.00<br>63.50MB<br>66</th><th>100.00<br>99.50MB<br>115</th><th>100.00<br>112.00MB<br>115</th><th>100.00<br>202.50MB<br>212</th><th>100.00<br>188.50MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>73.50MB<br>21</th><th>100.00<br>92.50MB<br>21</th><th>100.00<br>100.50MB<br>21</th><th>100.00<br>137.50MB<br>21</th><th>100.00<br>196.00MB<br>21</th><th>100.00<br>238.50MB<br>21</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>148.84MB<br>69</th><th>100.00<br>154.72MB<br>70</th><th>100.00<br>170.78MB<br>119</th><th>100.00<br>171.32MB<br>118</th><th>100.00<br>204.02MB<br>216</th><th>100.00<br>206.09MB<br>216</th></tr>
<tr><td>VT</td><th>100.00<br>149.46MB<br>20</th><th>100.00<br>151.46MB<br>19</th><th>100.00<br>163.55MB<br>20</th><th>100.00<br>166.86MB<br>19</th><th>100.00<br>197.75MB<br>19</th><th>100.00<br>193.23MB<br>19</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>52.50MB<br>14</th><th>100.00<br>44.00MB<br>14</th><th>100.00<br>95.00MB<br>14</th><th>100.00<br>93.00MB<br>14</th><th>100.00<br>116.50MB<br>14</th><th>100.00<br>164.00MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>60.00MB<br>14</th><th>100.00<br>62.00MB<br>14</th><th>100.00<br>117.00MB<br>14</th><th>100.00<br>79.50MB<br>14</th><th>100.00<br>219.00MB<br>14</th><th>100.00<br>169.00MB<br>14</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>68.50MB<br>14</th><th>100.00<br>57.00MB<br>14</th><th>100.00<br>129.00MB<br>14</th><th>100.00<br>112.50MB<br>14</th><th>100.00<br>134.00MB<br>14</th><th>100.00<br>169.00MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>70.00MB<br>14</th><th>100.00<br>69.50MB<br>14</th><th>100.00<br>84.50MB<br>14</th><th>100.00<br>119.00MB<br>14</th><th>100.00<br>193.50MB<br>14</th><th>100.00<br>200.00MB<br>14</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>117.00MB<br>18</th><th>100.00<br>117.63MB<br>18</th><th>100.00<br>129.26MB<br>18</th><th>100.00<br>133.00MB<br>18</th><th>100.00<br>136.76MB<br>18</th><th>100.00<br>137.80MB<br>18</th></tr>
<tr><td>VT</td><th>100.00<br>117.23MB<br>18</th><th>100.00<br>118.13MB<br>18</th><th>100.00<br>130.44MB<br>18</th><th>100.00<br>133.43MB<br>18</th><th>100.00<br>136.80MB<br>18</th><th>100.00<br>138.82MB<br>18</th></tr>
</table>
</details>

### Http integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>1891</th><th>3863</th><th>1904</th><th>3897</th><th>1895</th><th>3899</th></tr>
<tr><td>VT</td><th>1913</th><th>3900</th><th>1936</th><th>3923</th><th>1914</th><th>3863</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>2465</th><th>5034</th><th>2490</th><th>5082</th><th>2483</th><th>5102</th></tr>
<tr><td>VT</td><th>2528</th><th>5111</th><th>2538</th><th>5192</th><th>2515</th><th>5096</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>1508</th><th>3875</th><th>1313</th><th>3718</th><th>1182</th><th>3469</th></tr>
<tr><td>VT</td><th>2232</th><th>4485</th><th>2150</th><th>4457</th><th>1947</th><th>4464</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>2711</th><th>5569</th><th>2746</th><th>5589</th><th>2724</th><th>5562</th></tr>
<tr><td>VT</td><th>2694</th><th>5538</th><th>2711</th><th>5549</th><th>2688</th><th>5497</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>4016</th><th>8117</th><th>4061</th><th>8243</th><th>3996</th><th>8265</th></tr>
<tr><td>VT</td><th>4103</th><th>8159</th><th>4083</th><th>8295</th><th>4037</th><th>8302</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2350</th><th>6152</th><th>2150</th><th>5902</th><th>1840</th><th>4851</th></tr>
<tr><td>VT</td><th>2309</th><th>6185</th><th>2218</th><th>5848</th><th>1854</th><th>4827</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>227086</th><th>463497</th><th>228501</th><th>467597</th><th>227431</th><th>468002</th></tr>
<tr><td>VT</td><th>229598<br>e: 3</th><th>467932<br>e: 3</th><th>232371<br>e: 2</th><th>470830<br>e: 2</th><th>229902<br>e: 2</th><th>463711<br>e: 5</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>295908</th><th>604190</th><th>298910</th><th>609811</th><th>297993</th><th>612595</th></tr>
<tr><td>VT</td><th>303309<br>e: 4</th><th>613210<br>e: 2</th><th>304568<br>e: 5</th><th>623153<br>e: 5</th><th>301885<br>e: 2</th><th>611484<br>e: 4</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>180948</th><th>465116</th><th>157647</th><th>446210</th><th>141986</th><th>416301</th></tr>
<tr><td>VT</td><th>267869</th><th>538110</th><th>257989</th><th>534761</th><th>233763</th><th>535605</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>325417</th><th>668292</th><th>329513</th><th>670637</th><th>326895</th><th>667517</th></tr>
<tr><td>VT</td><th>323244</th><th>664415</th><th>325417</th><th>665698</th><th>322642</th><th>659671</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>481993</th><th>973728</th><th>487549</th><th>988829</th><th>479449</th><th>991535</th></tr>
<tr><td>VT</td><th>492365</th><th>978863</th><th>489959</th><th>995118</th><th>484405</th><th>996311</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>281907</th><th>738028</th><th>257931</th><th>707946</th><th>220975</th><th>582125</th></tr>
<tr><td>VT</td><th>277128</th><th>742094</th><th>266125</th><th>701582</th><th>222510</th><th>579251</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>0.099</th><th>0.137</th><th>0.054</th><th>0.063</th><th>0.057</th><th>0.071</th></tr>
<tr><td>VT</td><th>0.099</th><th>0.049</th><th>0.074</th><th>0.074</th><th>0.048</th><th>0.071</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.063</th><th>0.091</th><th>0.065</th><th>0.062</th><th>0.042</th><th>0.064</th></tr>
<tr><td>VT</td><th>0.063</th><th>0.064</th><th>0.064</th><th>0.044</th><th>0.049</th><th>0.063</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.402</th><th>2.206</th><th>4.548</th><th>2.329</th><th>4.321</th><th>2.242</th></tr>
<tr><td>VT</td><th>4.515</th><th>2.188</th><th>4.488</th><th>2.199</th><th>4.689</th><th>2.317</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>0.066</th><th>0.082</th><th>0.066</th><th>0.046</th><th>0.124</th><th>0.062</th></tr>
<tr><td>VT</td><th>0.079</th><th>0.07</th><th>0.067</th><th>0.044</th><th>0.043</th><th>0.066</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.062</th><th>0.06</th><th>0.033</th><th>0.061</th><th>0.061</th><th>0.033</th></tr>
<tr><td>VT</td><th>0.059</th><th>0.034</th><th>0.032</th><th>0.058</th><th>0.058</th><th>0.034</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.105</th><th>2.084</th><th>4.101</th><th>2.076</th><th>3.905</th><th>2.054</th></tr>
<tr><td>VT</td><th>4.322</th><th>2.157</th><th>4.004</th><th>2.036</th><th>4.1</th><th>2.013</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>190.50MB<br>219</th><th>100.00<br>194.50MB<br>202</th><th>100.00<br>274.50MB<br>418</th><th>100.00<br>286.50MB<br>415</th><th>100.00<br>316.00MB<br>602</th><th>100.00<br>318.50MB<br>677</th></tr>
<tr><td>VT</td><th>100.00<br>179.50MB<br>157</th><th>100.00<br>NaNB<br>135</th><th>100.00<br>295.00MB<br>280</th><th>100.00<br>316.50MB<br>302</th><th>100.00<br>313.00MB<br>582</th><th>100.00<br>327.50MB<br>614</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>159.50MB<br>216</th><th>100.00<br>263.50MB<br>215</th><th>100.00<br>284.50MB<br>414</th><th>100.00<br>293.00MB<br>425</th><th>100.00<br>NaNB<br>682</th><th>100.00<br>304.00MB<br>653</th></tr>
<tr><td>VT</td><th>100.00<br>190.00MB<br>172</th><th>100.00<br>215.50MB<br>132</th><th>100.00<br>277.50MB<br>291</th><th>100.00<br>321.50MB<br>289</th><th>100.00<br>342.50MB<br>573</th><th>100.00<br>310.50MB<br>609</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>153.91MB<br>206</th><th>100.00<br>153.72MB<br>205</th><th>100.00<br>180.70MB<br>384</th><th>100.00<br>183.92MB<br>381</th><th>100.00<br>214.98MB<br>536</th><th>100.00<br>217.80MB<br>540</th></tr>
<tr><td>VT</td><th>100.00<br>142.83MB<br>45</th><th>100.00<br>144.25MB<br>51</th><th>100.00<br>166.40MB<br>58</th><th>100.00<br>167.54MB<br>66</th><th>100.00<br>209.90MB<br>117</th><th>100.00<br>210.69MB<br>126</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>96.00MB<br>10</th><th>100.00<br>66.00MB<br>10</th><th>100.00<br>105.00MB<br>10</th><th>100.00<br>91.00MB<br>10</th><th>100.00<br>190.50MB<br>10</th><th>100.00<br>165.00MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>63.00MB<br>10</th><th>100.00<br>94.00MB<br>10</th><th>100.00<br>101.00MB<br>10</th><th>100.00<br>100.50MB<br>10</th><th>100.00<br>136.50MB<br>10</th><th>100.00<br>124.00MB<br>10</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>92.00MB<br>10</th><th>100.00<br>73.00MB<br>10</th><th>100.00<br>136.50MB<br>10</th><th>100.00<br>123.50MB<br>10</th><th>100.00<br>157.50MB<br>10</th><th>100.00<br>212.00MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>109.50MB<br>10</th><th>100.00<br>83.50MB<br>10</th><th>100.00<br>110.50MB<br>10</th><th>100.00<br>120.50MB<br>10</th><th>100.00<br>196.50MB<br>10</th><th>100.00<br>253.50MB<br>10</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>110.64MB<br>14</th><th>100.00<br>110.50MB<br>14</th><th>100.00<br>119.42MB<br>14</th><th>100.00<br>121.04MB<br>14</th><th>100.00<br>125.12MB<br>14</th><th>100.00<br>127.24MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>109.92MB<br>14</th><th>100.00<br>110.29MB<br>14</th><th>100.00<br>119.41MB<br>14</th><th>100.00<br>120.95MB<br>14</th><th>100.00<br>124.52MB<br>14</th><th>100.00<br>127.34MB<br>14</th></tr>
</table>
</details>