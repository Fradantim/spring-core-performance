#!/bin/bash

build_docker_compose_file=docker-compose/docker-compose_build.yml
monitoring_docker_compose_file=docker-compose/docker-compose_monitoring.yml

escape() {
	cd ${here}
	docker compose -f ${build_docker_compose_file} down
	docker compose -f ${monitoring_docker_compose_file} down
	exit ${1}
}

here=$(dirname $(readlink -f "$0"))

docker compose -f ${build_docker_compose_file} up nexus --wait -d
if [ $? -ne 0 ]; then
	exit 1
fi

# build app
cd apps/util_result_collector
docker build -f ../../Dockerfile/Dockerfile-util_jvm -t util_result_collector:jvm --add-host=host.docker.internal:host-gateway --network host .
if [ $? -ne 0 ]; then
	escape 1
fi

cd ${here}
docker compose -f ${monitoring_docker_compose_file} up pmt --wait -d
if [ $? -ne 0 ]; then
	escape 1
fi

# execute app
docker run --add-host=host.docker.internal:host-gateway --network host -v ./outputs:/outputs util_result_collector:jvm --path-2-look=outputs --prometheus.url=http://host.docker.internal:9090/api/v1/query_range
if [ $? -ne 0 ]; then
	escape 1
fi

escape 0