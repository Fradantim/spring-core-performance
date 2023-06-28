#!/bin/bash

here=$(dirname $(readlink -f "$0"))
start_datetime=`date '+%Y%m%d%H%M%S'`

export delay=0
export ramp_up=15
export duration=120

threads=(50 100 250 500)
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
touch outputs/${start_datetime}/all.log;

for threads_idx in ${!threads[@]}; do
	export thread_amount=${threads[$threads_idx]}
	echo "============================================================"
	echo " ${thread_amount} threads ($((threads_idx + 1))/${#threads[@]})"
	echo "============================================================"
	echo

	for app_idx in ${!apps[@]}; do
		app=${apps[$app_idx]}
		for tag_idx in ${!tags[@]}; do
			tag=${tags[$tag_idx]}
			echo "============================================================"
			echo " ${thread_amount} threads ($((threads_idx + 1))/${#threads[@]}) - ${app} ($((app_idx + 1))/${#apps[@]}) : ${tag} ($((tag_idx + 1))/${#tags[@]}) summary" | tee -a outputs/${start_datetime}/all.log
			echo "============================================================"
			echo | tee -a outputs/${start_datetime}/all.log

			export appntag=${app}:${tag}
			export workspace=outputs/${start_datetime}/${app}_${tag}_${thread_amount}
			mkdir -p ${workspace}
			chmod 777 ${workspace}

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

exit 0