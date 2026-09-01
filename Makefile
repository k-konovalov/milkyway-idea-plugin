.PHONY: publish-core publish-gradle-plugin run-ide dev

publish-core:
	./gradlew :core:publishToMavenLocal --no-configuration-cache --rerun-tasks

publish-gradle-plugin:
	./gradlew :plugins:gradle-plugin:publishToMavenLocal --no-configuration-cache --rerun-tasks

validate-gradle-plugin-globally:
	./gradlew :plugins:gradle-plugin:publishPlugins --no-configuration-cache --rerun-tasks --validate-only

publish-gradle-plugin-globally:
	./gradlew :plugins:gradle-plugin:publishPlugins --no-configuration-cache --rerun-tasks

run-ide:
	./gradlew :plugins:idea-plugin:runIde --no-configuration-cache

dev: publish-core publish-gradle-plugin run-ide