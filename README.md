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
wfx-redis               gvm-pgo_VT     113MB
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
wfx-http                gvm-pgo_VT     97.3MB
wfx-http                gvm-pgo_RT     97.4MB
wfx-http                gvm_VT         116MB
wfx-http                gvm_RT         116MB
wfx                     jvm_VT         229MB
wfx                     jvm_RT         229MB
wfx                     gvm-pgo_VT     93.7MB
wfx                     gvm-pgo_RT     93.9MB
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
web-jdbc                gvm-pgo_VT     98.7MB
web-jdbc                gvm-pgo_RT     98.4MB
web-jdbc                gvm_VT         117MB
web-jdbc                gvm_RT         117MB
web-http                jvm_VT         226MB
web-http                jvm_RT         226MB
web-http                gvm-pgo_VT     95.4MB
web-http                gvm-pgo_RT     95.2MB
web-http                gvm_VT         113MB
web-http                gvm_RT         113MB
web                     jvm_VT         226MB
web                     jvm_RT         226MB
web                     gvm-pgo_VT     92.6MB
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
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>6639</th><th>13395</th><th>6751</th><th>13629</th><th>6772</th><th>13582</th></tr>
<tr><td>VT</td><th>6435</th><th>12694</th><th>6747</th><th>13285</th><th>6965</th><th>13850</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>9208</th><th>18392</th><th>9331</th><th>18472</th><th>9419</th><th>18070</th></tr>
<tr><td>VT</td><th>8760</th><th>17946</th><th>9177</th><th>17967</th><th>9571</th><th>17492</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>7838</th><th>16741</th><th>6656</th><th>14959</th><th>4141</th><th>11320</th></tr>
<tr><td>VT</td><th>11285</th><th>20151</th><th>10629</th><th>20228</th><th>9308</th><th>20374</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>6981</th><th>14093</th><th>7007</th><th>13988</th><th>6858</th><th>13988</th></tr>
<tr><td>VT</td><th>6909</th><th>14019</th><th>6924</th><th>13879</th><th>6860</th><th>13793</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>11107</th><th>22627</th><th>11048</th><th>21917</th><th>10708</th><th>21588</th></tr>
<tr><td>VT</td><th>11048</th><th>22041</th><th>10808</th><th>21651</th><th>10609</th><th>21437</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>6757</th><th>16064</th><th>6567</th><th>15474</th><th>6340</th><th>15473</th></tr>
<tr><td>VT</td><th>6701</th><th>15586</th><th>6445</th><th>15603</th><th>6017</th><th>15688</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>796881</th><th>1606796</th><th>810291</th><th>1635473</th><th>812707</th><th>1630209<br>e: 18</th></tr>
<tr><td>VT</td><th>772084</th><th>1522787</th><th>809387</th><th>1594187</th><th>835979</th><th>1662322</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>1105000</th><th>2206146</th><th>1119518</th><th>2215804</th><th>1130699</th><th>2168412</th></tr>
<tr><td>VT</td><th>1050908</th><th>2153527</th><th>1101336</th><th>2155409</th><th>1148791</th><th>2098397</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>940272</th><th>2008563</th><th>798579</th><th>1794271</th><th>497104</th><th>1358096</th></tr>
<tr><td>VT</td><th>1353490</th><th>2417154</th><th>1275483</th><th>2426838</th><th>1116852</th><th>2444311</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>837439</th><th>1690647</th><th>840829</th><th>1678006</th><th>823151</th><th>1678524</th></tr>
<tr><td>VT</td><th>829070</th><th>1682356</th><th>830833</th><th>1665416</th><th>823469</th><th>1654588</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>1332654</th><th>2714693</th><th>1325405</th><th>2629598</th><th>1285317</th><th>2589782</th></tr>
<tr><td>VT</td><th>1325851</th><th>2643997</th><th>1296826</th><th>2597151</th><th>1273459</th><th>2572749</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>810495</th><th>1926947</th><th>787852</th><th>1856113</th><th>760660</th><th>1856410</th></tr>
<tr><td>VT</td><th>804149</th><th>1869524</th><th>773196</th><th>1872337</th><th>722026</th><th>1881942</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>0.093</th><th>0.069</th><th>0.049</th><th>0.088</th><th>0.057</th><th>0.058</th></tr>
<tr><td>VT</td><th>0.094</th><th>0.069</th><th>0.048</th><th>0.089</th><th>0.059</th><th>0.061</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.061</th><th>0.051</th><th>0.037</th><th>0.065</th><th>0.043</th><th>0.038</th></tr>
<tr><td>VT</td><th>0.061</th><th>0.048</th><th>0.037</th><th>0.059</th><th>0.044</th><th>0.04</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.117</th><th>2.159</th><th>4.112</th><th>2.083</th><th>3.897</th><th>2.016</th></tr>
<tr><td>VT</td><th>4.023</th><th>2.054</th><th>4.012</th><th>2.002</th><th>4.114</th><th>2.065</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>0.083</th><th>0.063</th><th>0.037</th><th>0.058</th><th>0.048</th><th>0.064</th></tr>
<tr><td>VT</td><th>0.084</th><th>0.064</th><th>0.037</th><th>0.058</th><th>0.051</th><th>0.064</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.059</th><th>0.047</th><th>0.028</th><th>0.058</th><th>0.048</th><th>0.04</th></tr>
<tr><td>VT</td><th>0.052</th><th>0.032</th><th>0.031</th><th>0.054</th><th>0.054</th><th>0.03</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>3.983</th><th>2.014</th><th>3.815</th><th>2.089</th><th>3.927</th><th>1.974</th></tr>
<tr><td>VT</td><th>4.083</th><th>2.028</th><th>3.692</th><th>2.032</th><th>3.914</th><th>2.06</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>43.50MB<br>62</th><th>100.00<br>53.50MB<br>62</th><th>100.00<br>79.50MB<br>111</th><th>100.00<br>81.50MB<br>117</th><th>100.00<br>146.50MB<br>208</th><th>100.00<br>167.00MB<br>208</th></tr>
<tr><td>VT</td><th>100.00<br>47.00MB<br>16</th><th>100.00<br>42.50MB<br>16</th><th>100.00<br>54.00MB<br>16</th><th>100.00<br>44.50MB<br>16</th><th>100.00<br>87.00MB<br>16</th><th>100.00<br>72.00MB<br>16</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>50.00MB<br>61</th><th>100.00<br>79.00MB<br>60</th><th>100.00<br>67.50MB<br>111</th><th>100.00<br>80.00MB<br>110</th><th>100.00<br>118.50MB<br>208</th><th>100.00<br>186.00MB<br>208</th></tr>
<tr><td>VT</td><th>100.00<br>45.50MB<br>16</th><th>100.00<br>44.00MB<br>16</th><th>100.00<br>55.50MB<br>16</th><th>100.00<br>60.50MB<br>16</th><th>100.00<br>104.00MB<br>16</th><th>100.00<br>84.50MB<br>16</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>116.85MB<br>66</th><th>100.00<br>173.31MB<br>66</th><th>100.00<br>130.91MB<br>117</th><th>100.00<br>166.88MB<br>118</th><th>100.00<br>159.72MB<br>212</th><th>100.00<br>196.31MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>104.40MB<br>14</th><th>100.00<br>158.19MB<br>14</th><th>100.00<br>107.04MB<br>14</th><th>100.00<br>201.19MB<br>14</th><th>100.00<br>111.67MB<br>14</th><th>100.00<br>268.19MB<br>14</th></tr>
<tr><td rowspan="6">wfx</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>42.50MB<br>10</th><th>100.00<br>33.50MB<br>10</th><th>100.00<br>55.00MB<br>10</th><th>100.00<br>39.00MB<br>10</th><th>100.00<br>39.50MB<br>10</th><th>100.00<br>56.50MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>57.50MB<br>10</th><th>100.00<br>43.50MB<br>10</th><th>100.00<br>63.00MB<br>10</th><th>100.00<br>34.50MB<br>10</th><th>100.00<br>38.50MB<br>10</th><th>100.00<br>43.00MB<br>10</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>40.00MB<br>10</th><th>100.00<br>46.00MB<br>10</th><th>100.00<br>54.00MB<br>10</th><th>100.00<br>62.50MB<br>10</th><th>100.00<br>60.50MB<br>10</th><th>100.00<br>41.50MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>49.00MB<br>10</th><th>100.00<br>68.00MB<br>10</th><th>100.00<br>54.00MB<br>10</th><th>100.00<br>43.50MB<br>10</th><th>100.00<br>52.50MB<br>10</th><th>100.00<br>80.50MB<br>10</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>101.78MB<br>14</th><th>100.00<br>213.50MB<br>14</th><th>100.00<br>101.90MB<br>14</th><th>100.00<br>205.94MB<br>14</th><th>100.00<br>102.24MB<br>14</th><th>100.00<br>222.25MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>101.82MB<br>14</th><th>100.00<br>220.94MB<br>14</th><th>100.00<br>102.64MB<br>14</th><th>100.00<br>220.25MB<br>14</th><th>100.00<br>102.86MB<br>14</th><th>100.00<br>232.75MB<br>14</th></tr>
</table>
</details>

### PostgreSQL integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>4112</th><th>8111</th><th>4346</th><th>8751</th><th>4334</th><th>8869</th></tr>
<tr><td>VT</td><th>4180</th><th>8262</th><th>4162</th><th>8583</th><th>4117</th><th>8538</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>5154</th><th>10702</th><th>5600</th><th>10817</th><th>5626</th><th>11513</th></tr>
<tr><td>VT</td><th>5420</th><th>10915</th><th>5395</th><th>11029</th><th>5351</th><th>11042</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4207</th><th>9364</th><th>4383</th><th>9099</th><th>3811</th><th>8815</th></tr>
<tr><td>VT</td><th>6245</th><th>11713</th><th>6139</th><th>11504</th><th>5971</th><th>11468</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>3504</th><th>7070</th><th>3541</th><th>7143</th><th>3512</th><th>7095</th></tr>
<tr><td>VT</td><th>3517</th><th>7077</th><th>3518</th><th>7119</th><th>3511</th><th>7119</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>5264</th><th>10481</th><th>5303</th><th>10668</th><th>5213</th><th>10626</th></tr>
<tr><td>VT</td><th>5230</th><th>10460</th><th>5272</th><th>10669</th><th>5222</th><th>10686</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2212</th><th>5958</th><th>1723</th><th>6295</th><th>2141</th><th>5707</th></tr>
<tr><td>VT</td><th>2401</th><th>5754</th><th>1715</th><th>5918</th><th>1944</th><th>5597</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>493651</th><th>973001</th><th>521459</th><th>1050272</th><th>520270</th><th>1064439</th></tr>
<tr><td>VT</td><th>501444</th><th>991180</th><th>499661</th><th>1030103</th><th>493988</th><th>1024766</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>618465</th><th>1283952</th><th>672062</th><th>1297923</th><th>675317</th><th>1381549</th></tr>
<tr><td>VT</td><th>650177</th><th>1309234</th><th>647237</th><th>1323315</th><th>642328</th><th>1325060</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>504778</th><th>1123312</th><th>526051</th><th>1091581</th><th>457575</th><th>1057751</th></tr>
<tr><td>VT</td><th>749337</th><th>1405016</th><th>736620</th><th>1380041</th><th>716570</th><th>1376101</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>420590</th><th>848375</th><th>424840</th><th>857233</th><th>421546</th><th>851271</th></tr>
<tr><td>VT</td><th>421941</th><th>849020</th><th>422185</th><th>854054</th><th>421464</th><th>854149</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>631689</th><th>1257384</th><th>636367</th><th>1279763</th><th>625852</th><th>1274986</th></tr>
<tr><td>VT</td><th>627705</th><th>1255067</th><th>632435</th><th>1279887</th><th>626957</th><th>1282229</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>265564</th><th>714794</th><th>206779</th><th>755130</th><th>256956</th><th>684917</th></tr>
<tr><td>VT</td><th>288121</th><th>690332</th><th>205830</th><th>710309</th><th>233337</th><th>671604</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>0.117</th><th>0.095</th><th>0.074</th><th>0.122</th><th>0.082</th><th>0.08</th></tr>
<tr><td>VT</td><th>0.113</th><th>0.099</th><th>0.069</th><th>0.116</th><th>0.09</th><th>0.082</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.082</th><th>0.069</th><th>0.057</th><th>0.081</th><th>0.066</th><th>0.064</th></tr>
<tr><td>VT</td><th>0.079</th><th>0.068</th><th>0.057</th><th>0.08</th><th>0.064</th><th>0.061</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.576</th><th>2.302</th><th>4.194</th><th>2.318</th><th>4.222</th><th>2.305</th></tr>
<tr><td>VT</td><th>4.218</th><th>2.212</th><th>4.17</th><th>2.38</th><th>4.567</th><th>2.28</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>0.109</th><th>0.083</th><th>0.053</th><th>0.078</th><th>0.065</th><th>0.085</th></tr>
<tr><td>VT</td><th>0.108</th><th>0.087</th><th>0.054</th><th>0.077</th><th>0.073</th><th>0.09</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.074</th><th>0.074</th><th>0.043</th><th>0.043</th><th>0.053</th><th>0.077</th></tr>
<tr><td>VT</td><th>0.076</th><th>0.075</th><th>0.043</th><th>0.044</th><th>0.057</th><th>0.076</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.807</th><th>2.435</th><th>4.709</th><th>2.477</th><th>4.57</th><th>2.489</th></tr>
<tr><td>VT</td><th>4.617</th><th>2.521</th><th>4.91</th><th>2.327</th><th>4.52</th><th>2.393</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-jdbc</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>60.50MB<br>64</th><th>100.00<br>53.50MB<br>65</th><th>100.00<br>96.50MB<br>114</th><th>100.00<br>95.00MB<br>114</th><th>100.00<br>159.00MB<br>211</th><th>100.00<br>207.50MB<br>211</th></tr>
<tr><td>VT</td><th>100.00<br>54.00MB<br>24</th><th>100.00<br>67.00MB<br>24</th><th>100.00<br>67.00MB<br>24</th><th>100.00<br>78.50MB<br>24</th><th>100.00<br>101.50MB<br>24</th><th>100.00<br>137.50MB<br>24</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>51.50MB<br>64</th><th>100.00<br>66.50MB<br>64</th><th>100.00<br>88.50MB<br>113</th><th>100.00<br>87.00MB<br>115</th><th>100.00<br>203.50MB<br>211</th><th>100.00<br>177.50MB<br>211</th></tr>
<tr><td>VT</td><th>100.00<br>64.50MB<br>24</th><th>100.00<br>68.50MB<br>24</th><th>100.00<br>80.50MB<br>24</th><th>100.00<br>106.00MB<br>24</th><th>100.00<br>148.00MB<br>24</th><th>100.00<br>129.00MB<br>24</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>132.98MB<br>67</th><th>100.00<br>197.00MB<br>69</th><th>100.00<br>148.87MB<br>118</th><th>100.00<br>171.75MB<br>118</th><th>100.00<br>181.11MB<br>215</th><th>100.00<br>223.13MB<br>215</th></tr>
<tr><td>VT</td><th>100.00<br>130.73MB<br>22</th><th>100.00<br>212.13MB<br>22</th><th>100.00<br>145.41MB<br>23</th><th>100.00<br>212.94MB<br>22</th><th>100.00<br>173.47MB<br>22</th><th>100.00<br>236.25MB<br>22</th></tr>
<tr><td rowspan="6">wfx-r2dbc</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>49.00MB<br>16</th><th>100.00<br>48.00MB<br>16</th><th>100.00<br>72.50MB<br>16</th><th>100.00<br>75.50MB<br>16</th><th>100.00<br>117.50MB<br>16</th><th>100.00<br>94.00MB<br>16</th></tr>
<tr><td>VT</td><th>100.00<br>46.50MB<br>16</th><th>100.00<br>48.50MB<br>16</th><th>100.00<br>63.50MB<br>16</th><th>100.00<br>70.50MB<br>16</th><th>100.00<br>113.50MB<br>16</th><th>100.00<br>108.50MB<br>16</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>51.50MB<br>16</th><th>100.00<br>50.50MB<br>16</th><th>100.00<br>68.00MB<br>16</th><th>100.00<br>74.50MB<br>16</th><th>100.00<br>74.50MB<br>16</th><th>100.00<br>116.50MB<br>16</th></tr>
<tr><td>VT</td><th>100.00<br>41.00MB<br>16</th><th>100.00<br>59.00MB<br>16</th><th>100.00<br>64.00MB<br>16</th><th>100.00<br>77.50MB<br>16</th><th>100.00<br>90.50MB<br>16</th><th>100.00<br>104.00MB<br>16</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>120.14MB<br>20</th><th>100.00<br>246.63MB<br>20</th><th>100.00<br>134.60MB<br>20</th><th>100.00<br>218.06MB<br>20</th><th>100.00<br>139.27MB<br>20</th><th>100.00<br>220.69MB<br>20</th></tr>
<tr><td>VT</td><th>100.00<br>120.23MB<br>20</th><th>100.00<br>225.38MB<br>20</th><th>100.00<br>133.50MB<br>20</th><th>100.00<br>262.81MB<br>20</th><th>100.00<br>138.63MB<br>20</th><th>100.00<br>261.88MB<br>20</th></tr>
</table>
</details>

### MongoDB integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>2940</th><th>5942</th><th>2953</th><th>5990</th><th>2947</th><th>6023</th></tr>
<tr><td>VT</td><th>2969</th><th>5932</th><th>3032</th><th>6109</th><th>2985</th><th>6068</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>3998</th><th>8101</th><th>4069</th><th>8213</th><th>4061</th><th>8214</th></tr>
<tr><td>VT</td><th>3969</th><th>7783</th><th>4063</th><th>8095</th><th>4011</th><th>8116</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2705</th><th>6483</th><th>2610</th><th>6369</th><th>2198</th><th>6105</th></tr>
<tr><td>VT</td><th>4362</th><th>8694</th><th>3784</th><th>8566</th><th>3411</th><th>7928</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>2586</th><th>5232</th><th>2626</th><th>5350</th><th>2597</th><th>5397</th></tr>
<tr><td>VT</td><th>2597</th><th>5246</th><th>2610</th><th>5356</th><th>2564</th><th>5384</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>3959</th><th>7936</th><th>4045</th><th>8196</th><th>4040</th><th>8427</th></tr>
<tr><td>VT</td><th>3941</th><th>7882</th><th>3985</th><th>8108</th><th>3986</th><th>8321</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>1636</th><th>5030</th><th>1586</th><th>4747</th><th>1418</th><th>4843</th></tr>
<tr><td>VT</td><th>1716</th><th>4981</th><th>1509</th><th>4867</th><th>1240</th><th>4609</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>352749</th><th>713100</th><th>354439</th><th>718969</th><th>353765</th><th>722898</th></tr>
<tr><td>VT</td><th>356426</th><th>711683</th><th>363774</th><th>732912</th><th>358265</th><th>728094</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>479622</th><th>972009</th><th>488479</th><th>985318</th><th>487380</th><th>985749</th></tr>
<tr><td>VT</td><th>476342</th><th>933696</th><th>487492</th><th>971319</th><th>481615</th><th>974081</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>324621</th><th>777634</th><th>313161</th><th>763989</th><th>263799</th><th>732803</th></tr>
<tr><td>VT</td><th>523178</th><th>1042824</th><th>453970</th><th>1027579</th><th>409258</th><th>951073</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>310312</th><th>627664</th><th>315121</th><th>641882</th><th>311781</th><th>647526</th></tr>
<tr><td>VT</td><th>311574</th><th>629364</th><th>313177</th><th>642876</th><th>307730</th><th>646042</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>474998</th><th>951927</th><th>485499</th><th>983409</th><th>484873</th><th>1011077</th></tr>
<tr><td>VT</td><th>472859</th><th>945682</th><th>478206</th><th>972884</th><th>478469</th><th>998640</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>196312</th><th>603403</th><th>190399</th><th>569528</th><th>170202</th><th>581195</th></tr>
<tr><td>VT</td><th>206050</th><th>597467</th><th>181152</th><th>583933</th><th>148907</th><th>553011</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>0.117</th><th>0.084</th><th>0.066</th><th>0.113</th><th>0.068</th><th>0.075</th></tr>
<tr><td>VT</td><th>0.117</th><th>0.086</th><th>0.066</th><th>0.115</th><th>0.072</th><th>0.067</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.084</th><th>0.062</th><th>0.049</th><th>0.08</th><th>0.054</th><th>0.048</th></tr>
<tr><td>VT</td><th>0.078</th><th>0.063</th><th>0.048</th><th>0.063</th><th>0.053</th><th>0.065</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>5.097</th><th>2.619</th><th>4.979</th><th>2.57</th><th>5.076</th><th>2.518</th></tr>
<tr><td>VT</td><th>4.881</th><th>2.59</th><th>5.108</th><th>2.641</th><th>5.312</th><th>2.56</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>0.105</th><th>0.083</th><th>0.052</th><th>0.073</th><th>0.066</th><th>0.067</th></tr>
<tr><td>VT</td><th>0.101</th><th>0.079</th><th>0.052</th><th>0.077</th><th>0.063</th><th>0.068</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.07</th><th>0.071</th><th>0.038</th><th>0.038</th><th>0.046</th><th>0.072</th></tr>
<tr><td>VT</td><th>0.07</th><th>0.068</th><th>0.038</th><th>0.039</th><th>0.05</th><th>0.072</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.805</th><th>2.47</th><th>4.991</th><th>2.593</th><th>5.074</th><th>2.517</th></tr>
<tr><td>VT</td><th>4.893</th><th>2.612</th><th>4.983</th><th>2.596</th><th>5.12</th><th>2.652</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>54.50MB<br>65</th><th>100.00<br>93.00MB<br>65</th><th>100.00<br>82.50MB<br>116</th><th>100.00<br>93.50MB<br>115</th><th>100.00<br>135.50MB<br>212</th><th>100.00<br>164.00MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>64.00MB<br>25</th><th>100.00<br>66.50MB<br>25</th><th>100.00<br>93.00MB<br>25</th><th>100.00<br>139.00MB<br>25</th><th>100.00<br>149.00MB<br>25</th><th>100.00<br>253.50MB<br>25</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>57.50MB<br>66</th><th>100.00<br>60.00MB<br>65</th><th>100.00<br>74.50MB<br>115</th><th>100.00<br>91.50MB<br>119</th><th>100.00<br>125.50MB<br>212</th><th>100.00<br>193.50MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>68.50MB<br>25</th><th>100.00<br>72.50MB<br>24</th><th>100.00<br>98.00MB<br>25</th><th>100.00<br>122.50MB<br>25</th><th>100.00<br>155.50MB<br>25</th><th>100.00<br>308.50MB<br>25</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>144.26MB<br>70</th><th>100.00<br>192.94MB<br>69</th><th>100.00<br>161.63MB<br>119</th><th>100.00<br>260.19MB<br>120</th><th>100.00<br>189.99MB<br>216</th><th>100.00<br>247.94MB<br>216</th></tr>
<tr><td>VT</td><th>100.00<br>141.79MB<br>23</th><th>100.00<br>237.13MB<br>22</th><th>100.00<br>157.13MB<br>23</th><th>100.00<br>222.56MB<br>23</th><th>100.00<br>190.93MB<br>23</th><th>100.00<br>261.13MB<br>23</th></tr>
<tr><td rowspan="6">wfx-mongo</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>69.00MB<br>16</th><th>100.00<br>63.00MB<br>18</th><th>100.00<br>106.00MB<br>16</th><th>100.00<br>86.00MB<br>18</th><th>100.00<br>116.00MB<br>16</th><th>100.00<br>160.50MB<br>18</th></tr>
<tr><td>VT</td><th>100.00<br>76.50MB<br>16</th><th>100.00<br>53.00MB<br>18</th><th>100.00<br>73.00MB<br>16</th><th>100.00<br>79.00MB<br>18</th><th>100.00<br>154.50MB<br>16</th><th>100.00<br>131.00MB<br>18</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>50.00MB<br>16</th><th>100.00<br>57.00MB<br>18</th><th>100.00<br>77.50MB<br>16</th><th>100.00<br>90.00MB<br>18</th><th>100.00<br>150.50MB<br>16</th><th>100.00<br>160.50MB<br>18</th></tr>
<tr><td>VT</td><th>100.00<br>65.00MB<br>16</th><th>100.00<br>91.00MB<br>18</th><th>100.00<br>84.00MB<br>16</th><th>100.00<br>103.00MB<br>18</th><th>100.00<br>139.00MB<br>16</th><th>100.00<br>143.50MB<br>18</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>125.47MB<br>20</th><th>100.00<br>240.00MB<br>22</th><th>100.00<br>143.89MB<br>20</th><th>100.00<br>212.63MB<br>22</th><th>100.00<br>146.39MB<br>20</th><th>100.00<br>251.06MB<br>22</th></tr>
<tr><td>VT</td><th>100.00<br>125.21MB<br>20</th><th>100.00<br>200.63MB<br>22</th><th>100.00<br>144.76MB<br>20</th><th>100.00<br>256.31MB<br>22</th><th>100.00<br>146.52MB<br>20</th><th>100.00<br>293.94MB<br>22</th></tr>
</table>
</details>

### Redis integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>4413</th><th>9036</th><th>4553</th><th>9361</th><th>4651</th><th>9539</th></tr>
<tr><td>VT</td><th>4425</th><th>8987</th><th>4503</th><th>9090</th><th>4634</th><th>9254</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>6005</th><th>12048</th><th>6185</th><th>12274</th><th>6235</th><th>12692</th></tr>
<tr><td>VT</td><th>6060</th><th>12479</th><th>6149</th><th>12571</th><th>6342</th><th>12611</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4674</th><th>10430</th><th>3973</th><th>10106</th><th>3571</th><th>9765</th></tr>
<tr><td>VT</td><th>6599</th><th>12866</th><th>6542</th><th>12654</th><th>5811</th><th>12382</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>4587</th><th>9204</th><th>4629</th><th>9349</th><th>4663</th><th>9350</th></tr>
<tr><td>VT</td><th>4604</th><th>9211</th><th>4677</th><th>9331</th><th>4664</th><th>9372</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>7219</th><th>14460</th><th>7418</th><th>14647</th><th>7533</th><th>14853</th></tr>
<tr><td>VT</td><th>7224</th><th>14560</th><th>7397</th><th>14646</th><th>7532</th><th>14976</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4258</th><th>10325</th><th>4193</th><th>10574</th><th>3720</th><th>10522</th></tr>
<tr><td>VT</td><th>4307</th><th>10200</th><th>4329</th><th>10524</th><th>3657</th><th>10260</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>529588</th><th>1083986</th><th>546373</th><th>1122995</th><th>558089</th><th>1144803</th></tr>
<tr><td>VT</td><th>530892</th><th>1078226</th><th>540401</th><th>1090768</th><th>556289</th><th>1110117</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>720542</th><th>1445367</th><th>742230</th><th>1472398</th><th>748327</th><th>1522917</th></tr>
<tr><td>VT</td><th>727301</th><th>1497256</th><th>737641</th><th>1508435</th><th>761333</th><th>1513244</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>560693</th><th>1251249</th><th>476628</th><th>1212786</th><th>428576</th><th>1171941</th></tr>
<tr><td>VT</td><th>791541</th><th>1543301</th><th>784903</th><th>1517901</th><th>697186</th><th>1485458</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>550371</th><th>1104145</th><th>555408</th><th>1121418</th><th>559746</th><th>1122189</th></tr>
<tr><td>VT</td><th>552589</th><th>1104942</th><th>560992</th><th>1119387</th><th>559740</th><th>1124643</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>866138</th><th>1734637</th><th>890280</th><th>1757412</th><th>904252</th><th>1782044</th></tr>
<tr><td>VT</td><th>867030</th><th>1746603</th><th>887480</th><th>1756927</th><th>903631</th><th>1796801</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>511082</th><th>1239005</th><th>503118</th><th>1268562</th><th>446276</th><th>1262747</th></tr>
<tr><td>VT</td><th>516741</th><th>1223430</th><th>519637</th><th>1262423</th><th>438778</th><th>1230954</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>0.129</th><th>0.084</th><th>0.064</th><th>0.111</th><th>0.075</th><th>0.088</th></tr>
<tr><td>VT</td><th>0.127</th><th>0.098</th><th>0.077</th><th>0.104</th><th>0.076</th><th>0.099</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.082</th><th>0.059</th><th>0.068</th><th>0.083</th><th>0.067</th><th>0.063</th></tr>
<tr><td>VT</td><th>0.086</th><th>0.058</th><th>0.047</th><th>0.085</th><th>0.056</th><th>0.047</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.905</th><th>2.634</th><th>5.101</th><th>2.585</th><th>5.061</th><th>2.491</th></tr>
<tr><td>VT</td><th>4.994</th><th>2.535</th><th>5.389</th><th>2.526</th><th>4.779</th><th>2.54</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>0.101</th><th>0.077</th><th>0.047</th><th>0.075</th><th>0.061</th><th>0.074</th></tr>
<tr><td>VT</td><th>0.102</th><th>0.075</th><th>0.046</th><th>0.077</th><th>0.064</th><th>0.077</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.073</th><th>0.063</th><th>0.036</th><th>0.052</th><th>0.039</th><th>0.061</th></tr>
<tr><td>VT</td><th>0.035</th><th>0.038</th><th>0.037</th><th>0.057</th><th>0.039</th><th>0.057</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.322</th><th>2.331</th><th>4.45</th><th>2.271</th><th>4.316</th><th>2.427</th></tr>
<tr><td>VT</td><th>4.506</th><th>2.423</th><th>4.495</th><th>2.332</th><th>4.802</th><th>2.294</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-redis</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>58.50MB<br>64</th><th>100.00<br>71.50MB<br>65</th><th>100.00<br>112.50MB<br>115</th><th>100.00<br>117.00MB<br>115</th><th>100.00<br>193.50MB<br>212</th><th>100.00<br>207.00MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>57.00MB<br>21</th><th>100.00<br>106.00MB<br>21</th><th>100.00<br>80.50MB<br>21</th><th>100.00<br>129.00MB<br>21</th><th>100.00<br>193.50MB<br>21</th><th>100.00<br>219.50MB<br>21</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>59.50MB<br>65</th><th>100.00<br>64.50MB<br>65</th><th>100.00<br>103.50MB<br>116</th><th>100.00<br>100.50MB<br>114</th><th>100.00<br>263.50MB<br>212</th><th>100.00<br>211.50MB<br>212</th></tr>
<tr><td>VT</td><th>100.00<br>60.50MB<br>21</th><th>100.00<br>84.00MB<br>21</th><th>100.00<br>108.50MB<br>21</th><th>100.00<br>200.00MB<br>21</th><th>100.00<br>223.50MB<br>21</th><th>100.00<br>194.50MB<br>21</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>148.63MB<br>69</th><th>100.00<br>167.69MB<br>70</th><th>100.00<br>171.56MB<br>120</th><th>100.00<br>189.19MB<br>118</th><th>100.00<br>205.24MB<br>216</th><th>100.00<br>238.88MB<br>216</th></tr>
<tr><td>VT</td><th>100.00<br>150.30MB<br>19</th><th>100.00<br>221.88MB<br>19</th><th>100.00<br>164.53MB<br>19</th><th>100.00<br>241.38MB<br>19</th><th>100.00<br>195.27MB<br>19</th><th>100.00<br>294.25MB<br>19</th></tr>
<tr><td rowspan="6">wfx-redis</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>97.50MB<br>14</th><th>100.00<br>54.50MB<br>14</th><th>100.00<br>82.00MB<br>14</th><th>100.00<br>107.00MB<br>14</th><th>100.00<br>111.00MB<br>14</th><th>100.00<br>185.50MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>112.50MB<br>14</th><th>100.00<br>53.50MB<br>14</th><th>100.00<br>112.50MB<br>14</th><th>100.00<br>100.50MB<br>14</th><th>100.00<br>115.00MB<br>14</th><th>100.00<br>133.50MB<br>14</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>71.50MB<br>14</th><th>100.00<br>55.00MB<br>14</th><th>100.00<br>110.50MB<br>14</th><th>100.00<br>85.50MB<br>14</th><th>100.00<br>125.50MB<br>14</th><th>100.00<br>139.50MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>62.00MB<br>14</th><th>100.00<br>44.00MB<br>14</th><th>100.00<br>90.50MB<br>14</th><th>100.00<br>112.00MB<br>14</th><th>100.00<br>110.00MB<br>14</th><th>100.00<br>136.50MB<br>14</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>118.16MB<br>18</th><th>100.00<br>209.94MB<br>18</th><th>100.00<br>129.18MB<br>18</th><th>100.00<br>229.38MB<br>18</th><th>100.00<br>137.55MB<br>18</th><th>100.00<br>242.13MB<br>18</th></tr>
<tr><td>VT</td><th>100.00<br>118.19MB<br>18</th><th>100.00<br>219.25MB<br>18</th><th>100.00<br>129.92MB<br>18</th><th>100.00<br>228.63MB<br>18</th><th>100.00<br>137.59MB<br>18</th><th>100.00<br>237.88MB<br>18</th></tr>
</table>
</details>

### Http integrated web-app

Duration: 120s, ramp up: 15s

#### Requests processed per second (JMeter)
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>1894</th><th>3866</th><th>1913</th><th>3899</th><th>1915</th><th>3925</th></tr>
<tr><td>VT</td><th>1924</th><th>3914</th><th>1943</th><th>3968</th><th>1946</th><th>3943</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>2470</th><th>5065</th><th>2489</th><th>5108</th><th>2494</th><th>5152</th></tr>
<tr><td>VT</td><th>2529</th><th>5179</th><th>2568</th><th>5262</th><th>2573</th><th>5235</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>1485</th><th>3632</th><th>1254</th><th>3412</th><th>1120</th><th>3144</th></tr>
<tr><td>VT</td><th>2330</th><th>4495</th><th>2160</th><th>4516</th><th>2088</th><th>4678</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>2778</th><th>5717</th><th>2766</th><th>5734</th><th>2758</th><th>5690</th></tr>
<tr><td>VT</td><th>2758</th><th>5621</th><th>2758</th><th>5779</th><th>2767</th><th>5713</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>4076</th><th>8402</th><th>4136</th><th>8526</th><th>4079</th><th>8428</th></tr>
<tr><td>VT</td><th>4099</th><th>8211</th><th>4159</th><th>8503</th><th>4123</th><th>8450</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>2324</th><th>6051</th><th>2225</th><th>6120</th><th>1766</th><th>6173</th></tr>
<tr><td>VT</td><th>2252</th><th>6214</th><th>2195</th><th>6066</th><th>1926</th><th>5871</th></tr>
</table>


#### Amount of requests processed (JMeter)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>227324</th><th>463859</th><th>229532</th><th>468007</th><th>229836</th><th>470946</th></tr>
<tr><td>VT</td><th>230851<br>e: 8</th><th>469767<br>e: 3</th><th>233242<br>e: 5</th><th>476278<br>e: 16</th><th>233648<br>e: 4</th><th>473387<br>e: 2</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>296398</th><th>607789</th><th>298632</th><th>612790</th><th>299343</th><th>618072</th></tr>
<tr><td>VT</td><th>303636<br>e: 3</th><th>621596<br>e: 4</th><th>308124<br>e: 3</th><th>631261<br>e: 1</th><th>308932<br>e: 5</th><th>628146<br>e: 1</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>178249</th><th>435693</th><th>150451</th><th>409414</th><th>134469</th><th>377384</th></tr>
<tr><td>VT</td><th>279592</th><th>539393</th><th>259240</th><th>541813</th><th>250594</th><th>561320</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>333446</th><th>685892</th><th>331840</th><th>687819</th><th>331031</th><th>682976</th></tr>
<tr><td>VT</td><th>330981</th><th>674355</th><th>330890</th><th>693296</th><th>332160</th><th>685413</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>489261</th><th>1008207</th><th>496177</th><th>1022828</th><th>489681</th><th>1011225</th></tr>
<tr><td>VT</td><th>491871</th><th>984931</th><th>498954</th><th>1020233</th><th>494670</th><th>1013730</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>278880</th><th>725900</th><th>266973</th><th>734270</th><th>212087</th><th>740842</th></tr>
<tr><td>VT</td><th>270234</th><th>745484</th><th>263385</th><th>727739</th><th>231183</th><th>704706</th></tr>
</table>
</details>

#### Start up in seconds (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>0.101</th><th>0.074</th><th>0.053</th><th>0.091</th><th>0.06</th><th>0.06</th></tr>
<tr><td>VT</td><th>0.1</th><th>0.074</th><th>0.049</th><th>0.098</th><th>0.059</th><th>0.056</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.066</th><th>0.05</th><th>0.039</th><th>0.063</th><th>0.044</th><th>0.038</th></tr>
<tr><td>VT</td><th>0.064</th><th>0.049</th><th>0.038</th><th>0.064</th><th>0.045</th><th>0.042</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.197</th><th>2.163</th><th>4.288</th><th>2.165</th><th>4.608</th><th>2.28</th></tr>
<tr><td>VT</td><th>4.404</th><th>2.195</th><th>4.613</th><th>2.175</th><th>4.922</th><th>2.217</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>0.085</th><th>0.083</th><th>0.042</th><th>0.046</th><th>0.055</th><th>0.085</th></tr>
<tr><td>VT</td><th>0.084</th><th>0.08</th><th>0.041</th><th>0.049</th><th>0.056</th><th>0.081</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>0.062</th><th>0.06</th><th>0.034</th><th>0.03</th><th>0.053</th><th>0.061</th></tr>
<tr><td>VT</td><th>0.06</th><th>0.061</th><th>0.032</th><th>0.031</th><th>0.041</th><th>0.061</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>4.09</th><th>2.085</th><th>4.08</th><th>2.118</th><th>4.008</th><th>2.104</th></tr>
<tr><td>VT</td><th>3.997</th><th>2.086</th><th>3.821</th><th>2.105</th><th>3.881</th><th>2.037</th></tr>
</table>
</details>

#### CPU usage % + memory + peak threads (Prometheus)
<details>
<summary>Click to expand</summary>
<table>
<tr><th></th><th></th><th></th><th colspan="6">#clients & #cores</th></tr>
<tr><th></th><th></th><th></th><th colspan="2">50</th><th colspan="2">100</th><th colspan="2">200</th></tr>
<tr><th>app</th><th>tag</th><th>thread</th><th>1</th><th>2</th><th>1</th><th>2</th><th>1</th><th>2</th></tr>
<tr><td rowspan="6">web-http</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>156.50MB<br>218</th><th>99.84<br>215.00MB<br>206</th><th>100.00<br>329.50MB<br>421</th><th>100.00<br>462.00MB<br>478</th><th>100.00<br>437.50MB<br>656</th><th>100.00<br>547.50MB<br>664</th></tr>
<tr><td>VT</td><th>100.00<br>157.50MB<br>149</th><th>100.00<br>280.00MB<br>147</th><th>100.00<br>375.00MB<br>290</th><th>100.00<br>373.00MB<br>268</th><th>100.00<br>539.50MB<br>518</th><th>100.00<br>594.50MB<br>588</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>99.69<br>142.00MB<br>211</th><th>99.84<br>179.00MB<br>208</th><th>100.00<br>323.50MB<br>430</th><th>100.00<br>520.50MB<br>403</th><th>100.00<br>493.00MB<br>607</th><th>100.00<br>560.50MB<br>710</th></tr>
<tr><td>VT</td><th>100.00<br>169.00MB<br>143</th><th>100.00<br>326.50MB<br>125</th><th>100.00<br>320.50MB<br>291</th><th>100.00<br>519.50MB<br>276</th><th>100.00<br>594.50MB<br>619</th><th>100.00<br>582.50MB<br>634</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>152.07MB<br>209</th><th>100.00<br>241.00MB<br>204</th><th>100.00<br>178.87MB<br>383</th><th>100.00<br>310.13MB<br>390</th><th>100.00<br>213.93MB<br>562</th><th>100.00<br>369.50MB<br>560</th></tr>
<tr><td>VT</td><th>100.00<br>144.60MB<br>42</th><th>100.00<br>251.81MB<br>45</th><th>100.00<br>165.29MB<br>52</th><th>100.00<br>299.81MB<br>70</th><th>100.00<br>209.82MB<br>121</th><th>100.00<br>398.56MB<br>125</th></tr>
<tr><td rowspan="6">wfx-http</td><td rowspan="2">gvm</td><td>RT</td><th>100.00<br>80.00MB<br>10</th><th>100.00<br>74.00MB<br>10</th><th>100.00<br>111.00MB<br>10</th><th>100.00<br>103.00MB<br>10</th><th>100.00<br>117.00MB<br>10</th><th>100.00<br>175.50MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>51.00MB<br>10</th><th>100.00<br>88.50MB<br>10</th><th>100.00<br>115.00MB<br>10</th><th>100.00<br>105.50MB<br>10</th><th>100.00<br>138.50MB<br>10</th><th>100.00<br>149.50MB<br>10</th></tr>
<tr><td rowspan="2">gvm-pgo</td><td>RT</td><th>100.00<br>61.50MB<br>10</th><th>100.00<br>80.50MB<br>10</th><th>100.00<br>85.00MB<br>10</th><th>100.00<br>124.50MB<br>10</th><th>100.00<br>156.50MB<br>10</th><th>100.00<br>196.50MB<br>10</th></tr>
<tr><td>VT</td><th>100.00<br>85.00MB<br>10</th><th>100.00<br>107.50MB<br>10</th><th>100.00<br>101.00MB<br>10</th><th>100.00<br>105.50MB<br>10</th><th>100.00<br>171.00MB<br>10</th><th>100.00<br>181.50MB<br>10</th></tr>
<tr><td rowspan="2">jvm</td><td>RT</td><th>100.00<br>111.07MB<br>14</th><th>100.00<br>260.25MB<br>14</th><th>100.00<br>120.42MB<br>14</th><th>100.00<br>241.81MB<br>14</th><th>100.00<br>125.30MB<br>14</th><th>100.00<br>359.88MB<br>14</th></tr>
<tr><td>VT</td><th>100.00<br>111.29MB<br>14</th><th>100.00<br>238.19MB<br>14</th><th>100.00<br>119.43MB<br>14</th><th>100.00<br>264.44MB<br>14</th><th>100.00<br>124.46MB<br>14</th><th>100.00<br>274.06MB<br>14</th></tr>
</table>
</details>