#!/bin/bash

apex_docker_compose_file=docker-compose/docker-compose_stress_apex.yml

# TODO, cant delete docker created files and folders...
zip_and_clean(){
	# zip results
	here={1} # /dir1/dir2/.../spring-core-performance
	workspace=${2} # /dir1/dir2/.../spring-core-performance/outputs/20000101000000/app_tag_cpus_clients
	cd ${workspace}
	zip -r $(basename $(pwd)).zip .
	if [ $? -ne 0 ]; then
		exit 1
	fi

	mv *.zip ..

	cd ${here}

	# copy important contents to new folder to lose permissions
	mkdir -p ${workspace}_tmp/report
	cp ${workspace}/JMeter_test_plan.log ${workspace}_tmp/
	cp ${workspace}/report/statistics.json ${workspace}_tmp/report/

	rm -rf ${workspace}
	mv ${workspace}_tmp ${workspace}
}

escape() {
	cd ${here}
	docker compose -f ${apex_docker_compose_file} down
	exit ${1}
}

docker compose -f ${apex_docker_compose_file} up --wait -d
if [ $? -ne 0 ]; then
	exit 1
fi

here=$(dirname $(readlink -f "$0"))
start_datetime=`date -u '+%Y%m%d%H%M%S'`

export delay=5 # to let prometheus start scraping
export ramp_up=15
export duration=120
export report_granurality=200

cpuss=(1 2)
clientss=(50 100 200)
apps=()

cd ${here}/apps/spring-parent/
for i in `ls -d w*`; do
	echo "- ${i}"
	apps+=(${i})
done

cd ${here}

echo
echo

mkdir -p outputs/${start_datetime}
chmod 777 outputs/${start_datetime}
touch outputs/${start_datetime}/all.log

global_count=0
per_tag_count=$(( ${#cpuss[@]} * ${#clientss[@]} ))
for app_idx in ${!apps[@]}; do
	app=${apps[$app_idx]}
	for tag in `docker image ls ${app} --format "{{.Tag}}"`; do
		global_count=$((global_count + per_tag_count))
	done
done

global_idx=0
for cpus_idx in ${!cpuss[@]}; do
	export cpus=${cpuss[$cpus_idx]}
	echo "============================================================"
	echo " ${cpus} cpus ($((cpus_idx + 1))/${#cpuss[@]})"
	echo "============================================================"
	echo

	for clients_idx in ${!clientss[@]}; do
		export clients=${clientss[$clients_idx]}
		echo "============================================================"
		echo " ${cpus} cpus ($((cpus_idx + 1))/${#cpuss[@]}) ${clients} clients ($((clients_idx + 1))/${#clientss[@]})"
		echo "============================================================"
		echo

		for app_idx in ${!apps[@]}; do
			app=${apps[$app_idx]}

			tags=()
			for tag in `docker image ls ${app} --format "{{.Tag}}"`; do
				tags+=(${tag})
			done

			for tag_idx in ${!tags[@]}; do
				tag=${tags[$tag_idx]}
				global_idx=$((global_idx + 1))

				title="[ ${global_idx} / ${global_count} ] ::"
				title="${title} ${cpus} cpus ($((cpus_idx + 1))/${#cpuss[@]}) "
				title="${title} ${clients} clients ($((clients_idx + 1))/${#clientss[@]})"
				title="${title}  -"
				title="${title} ${app} ($((app_idx + 1))/${#apps[@]})"
				title="${title} :"
				title="${title} ${tag} ($((tag_idx + 1))/${#tags[@]})"

				echo "============================================================"
				echo ${title} | tee -a outputs/${start_datetime}/all.log
				echo "============================================================"
				echo | tee -a outputs/${start_datetime}/all.log

				export appntag=${app}:${tag}
				app_start_datetime=`date -u '+%Y%m%d%H%M%S'`
				export spring_app_name=${app}_${tag}_${cpus}_${clients}_${app_start_datetime}
				export workspace=outputs/${start_datetime}/${spring_app_name}
				mkdir -p ${workspace}
				chmod -R 777 ${workspace}

				dc_file=${here}/docker-compose/docker-compose_stress.yml

				# docker compose -f ${dc_file} rm -fsv && docker compose -f ${dc_file} --compatibility up --abort-on-container-exit --remove-orphans | tee -a outputs/${start_datetime}/all.log
				docker compose -f ${dc_file} --compatibility up --abort-on-container-exit | tee -a outputs/${start_datetime}/all.log
				ret=${PIPESTATUS[0]}
				if [ $ret -ne 0 ]; then
					escape $ret
				fi

				# zip_and_clean ${here} ${here}/${workspace}

				echo | tee -a outputs/${start_datetime}/all.log
			done
		done
	done
done

docker volume prune -f

escape 0