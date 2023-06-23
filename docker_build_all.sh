#!/bin/bash

spring_boot_build_image_and_retag() {
	new_tag=${1}
	profiles=${2}
	
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
for i in `ls -d */`; do
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
	echo " (${index}/${#apps[@]})   ${app} JVM (1/2)"
	echo "============================================================"
	echo
	
	spring_boot_build_image_and_retag jvm
	
	echo "============================================================"
	echo " (${index}/${#apps[@]})   ${app} GraalVM (2/2)"
	echo "============================================================"
	echo
	
	spring_boot_build_image_and_retag graalvm -Pnative
	
	cd ${here}
done

exit 0