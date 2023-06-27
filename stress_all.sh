#!/bin/bash

here=$(dirname $(readlink -f "$0"))
start_datetime=`date '+%Y%m%d%H%M%S'`

export delay=30
export ramp_up=30
export duration=120

threads=(25 50 100 200)
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
			docker compose rm -fsv && docker compose up --abort-on-container-exit --remove-orphans | tee -a outputs/${start_datetime}/all.log
			ret=${PIPESTATUS[0]}
			if [ $ret -ne 0 ]; then
				exit $ret
			fi

			echo | tee -a outputs/${start_datetime}/all.log
		done
	done
done

exit 0