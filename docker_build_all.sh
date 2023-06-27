#!/bin/bash

# DEPRECATED, wont use spring build packs
spring_boot_build_image_and_retag() {
	new_tag=${1} # jvm / graalvm
	profiles=${2} # "" / "-Pnative"
	
	# app_version=`sh mvnw help:evaluate -Dexpression=project.version -q -DforceStdout 2>nul` # too slow
	app_version=`xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" pom.xml`
	# app_name=`sh mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout 2>nul` # too slow
	app_name=`xmllint --xpath "//*[local-name()='project']/*[local-name()='artifactId']/text()" pom.xml`
	
	sh mvnw ${profiles} clean spring-boot:build-image
	#	-Dspring-boot.build-image.imageName=${app_name}
	if [ $? -ne 0 ]; then
		exit 1
	fi
	
	echo retagging....
	#docker tag ${app_name}:latest ${app_name}:${new_tag}
	docker tag ${app_name}:${app_version} ${app_name}:${new_tag}
	docker rmi ${app_name}:${app_version}
	echo
	echo
}

here=$(dirname $(readlink -f "$0"))

apps=()

cd ${here}/apps/
for i in `ls -d web*/`; do
	# remove last "/"
	i=${i::-1}
	echo "- ${i}"
	apps+=(${i})
done

echo
echo

index=0
for app in "${apps[@]}"; do
	index=$((index + 1))
	
	cd ${here}/apps/${app}
	
	echo "============================================================"
	echo " (${index}/${#apps[@]})   ${app}"
	echo "============================================================"
	echo
	
	sh mvnw clean package -Pnative
	if [ $? -ne 0 ]; then
		exit 1
	fi

	echo
	echo "docker build jvm ..."
	echo

	docker build -f ../../Dockerfile_jvm -t ${app}:jvm .
	if [ $? -ne 0 ]; then
		exit 1
	fi

	echo
	echo "docker build graalvm ..."
	echo

	export APP_NAME=$(basename $(pwd))
	cat ../../Dockerfile_graalvm | envsubst > /tmp/Dockerfile_graalvm_${app}

	docker build -f /tmp/Dockerfile_graalvm_${app} -t ${app}:graalvm .
	if [ $? -ne 0 ]; then
		exit 1
	fi

	# clean proyect directory
	sh mvnw clean
	if [ $? -ne 0 ]; then
		exit 1
	fi

	cd ${here}
done

exit 0