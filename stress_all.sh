#!/bin/bash

# TODO, cant delete docker created files and folders...
clean_workspace() {
	workspace=${1} # /dir1/dir2/.../spring-core-performance/outputs/20000101000000/app_tag_cpus_clients
	chmod -R 777 ${workspace}
	rm -f ${workspace}/JMeter_test_plan.jtl
	rm -f ${workspace}/report/index.html
	rm -rf ${workspace}/report/content
	rm -rf ${workspace}/report/sb* #sbadmin2-1.0.7
}

here=$(dirname $(readlink -f "$0"))
start_datetime=`date '+%Y%m%d%H%M%S'`

export delay=0
export ramp_up=15
export duration=120

cpuss=(1 2 4)
clientss=(50 100 250 500)
tags=("jvm" "graalvm")
apps=()

cd ${here}/apps/
for i in `ls -d web*/`; do
	# remove last "/"
	i=${i::-1}
	echo "- ${i}"
	apps+=(${i})
done

cd ${here}

echo
echo

mkdir -p outputs/${start_datetime}
chmod 777 outputs/${start_datetime}
touch outputs/${start_datetime}/all.log

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
			for tag_idx in ${!tags[@]}; do
				tag=${tags[$tag_idx]}
				echo "============================================================"
				echo " ${cpus} cpus ($((cpus_idx + 1))/${#cpuss[@]}) ${clients} clients ($((clients_idx + 1))/${#clientss[@]}) - ${app} ($((app_idx + 1))/${#apps[@]}) : ${tag} ($((tag_idx + 1))/${#tags[@]}) summary" | tee -a outputs/${start_datetime}/all.log
				echo "============================================================"
				echo | tee -a outputs/${start_datetime}/all.log

				export appntag=${app}:${tag}
				export workspace=outputs/${start_datetime}/${app}_${tag}_${cpus}_${clients}
				mkdir -p ${workspace}
				chmod -R 777 ${workspace}

				dc_file=docker-compose.yml
				case $app in
					*dbc*)
						dc_file=docker-compose_w_postgres.yml
					;;
					*mongo*)
						dc_file=docker-compose_w_mongo.yml
					;;
				esac

				docker compose -f ${dc_file} rm -fsv && docker compose -f ${dc_file} up --abort-on-container-exit --remove-orphans | tee -a outputs/${start_datetime}/all.log
				ret=${PIPESTATUS[0]}
				if [ $ret -ne 0 ]; then
					exit $ret
				fi

				echo | tee -a outputs/${start_datetime}/all.log
			done
		done
	done
done

docker volume prune -f

exit 0