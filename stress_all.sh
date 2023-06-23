#!/bin/bash

here=$(dirname $(readlink -f "$0"))
start_datetime=`date '+%Y%m%d%H%M%S'`

export delay=15
export ramp_up=5
export duration=15

threads=(50 100 200 250)
tags=("jvm" "graalvm")
apps=()

cd ${here}/apps/
for i in `ls -d */`; do
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
			echo " ${thread_amount} threads ($((threads_idx + 1))/${#threads[@]}) - ${app} ($((app_idx + 1))/${#apps[@]}) : ${tag} ($((tag_idx + 1))/${#tags[@]})"
			echo "============================================================"
			echo

			export appntag=${app}:${tag}
			export workspace=outputs/${start_datetime}/${app}_${tag}_${thread_amount}
			mkdir -p ${workspace}
			chmod 777 ${workspace}
			docker compose rm -fsv && docker compose up --abort-on-container-exit --remove-orphans | tee -a outputs/${start_datetime}/all.log
			ret=${PIPESTATUS[0]}
			if [ $ret -ne 0 ]; then
				exit $ret
			fi
		done
	done
done

exit 0