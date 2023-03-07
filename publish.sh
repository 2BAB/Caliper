#!/usr/bin/env bash

./gradlew clean

# Keep the order as followed one may depend on previous one
MODULE_ARRAY1=('caliper-annotation' 'caliper-annotation-processor' 'code-analyzer' 'caliper-gradle-plugin')
for module in "${MODULE_ARRAY1[@]}"
do
./gradlew :"$module":publishAllPublicationsToSonatypeRepository
done

MODULE_ARRAY2=('caliper-runtime' 'caliper-runtime-battery-optim' 'caliper-runtime-privacy')
for module in "${MODULE_ARRAY2[@]}"
do
./gradlew :"$module":publishAllVariantsPublicationToSonatypeRepository
done

./gradlew aggregateJars releaseArtifactsToGithub