#!/bin/bash

build_docker_compose_file=docker-compose/docker-compose_build.yml

escape() {
	cd ${here}
	docker compose -f ${build_docker_compose_file} down
	exit ${1}
}

docker compose -f ${build_docker_compose_file} up --wait -d
if [ $? -ne 0 ]; then
	exit 1
fi

echo
echo

export here=$(dirname $(readlink -f "$0"))

docker_files=()

for i in `ls -f Dockerfile_*`; do
	echo "- ${i}"
	docker_files+=(${i})
done

echo
echo

apps=()

cd ${here}/apps/spring-parent/
for i in `ls -d w*`; do
	echo "- ${i}"
	apps+=(${i})
done

echo
echo

app_index=0
for app in "${apps[@]}"; do
	app_index=$((app_index + 1))

	docker_file_index=0
	for docker_file in "${docker_files[@]}"; do
		docker_file_index=$((docker_file_index + 1))

		global_stat="[ $(((app_index - 1)*${#docker_files[@]} + docker_file_index)) / $((${#apps[@]}*${#docker_files[@]})) ]"
		app_stat="(${app_index}/${#apps[@]})"
		dfile_stat="(${docker_file_index}/${#docker_files[@]})"
		echo "============================================================"
		echo "${global_stat}  ::  ${app} ${app_stat} ${docker_file} ${dfile_stat}"
		echo "============================================================"
		echo
		
		export APP_NAME=${app}

		cat ${here}/${docker_file} | envsubst > /tmp/${docker_file}
		tag=$(echo ${docker_file} | cut -d '_' -f 2)

		docker build -f /tmp/${docker_file} -t ${app}:${tag} --add-host=host.docker.internal:host-gateway --network host .
		if [ $? -ne 0 ]; then
			escape 1
		fi
	done
done

escape 0