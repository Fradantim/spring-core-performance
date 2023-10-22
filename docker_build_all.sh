#!/bin/bash

escape() {
	cd ${here}
	docker compose -f docker-compose_all.yml down
	exit ${1}
}

docker compose -f docker-compose_all.yml up --wait -d
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

cd ${here}/apps/
for i in `ls -d web*/`; do
	i=${i::-1} # remove last "/"
	echo "- ${i}"
	apps+=(${i})
done

echo
echo

app_index=0
for app in "${apps[@]}"; do
	app_index=$((app_index + 1))
	
	cd ${here}/apps/${app}

	docker_file_index=0
	for docker_file in "${docker_files[@]}"; do
		docker_file_index=$((docker_file_index + 1))

		echo "============================================================"
		echo "[ $(((app_index - 1)*${#docker_files[@]} + docker_file_index)) / $((${#apps[@]}*${#docker_files[@]})) ]  ::  ${app} (${app_index}/${#apps[@]}) ${docker_file} (${docker_file_index}/${#docker_files[@]})"
		echo "============================================================"
		echo
		
		export APP_NAME=$(basename $(pwd))

		cat ../../${docker_file} | envsubst > /tmp/${docker_file}
		tag=$(echo ${docker_file} | cut -d '_' -f 2)

		docker build -f /tmp/${docker_file} -t ${app}:${tag} --add-host=host.docker.internal:host-gateway --network host .
		if [ $? -ne 0 ]; then
			escape 1
		fi
	done
done

escape 0