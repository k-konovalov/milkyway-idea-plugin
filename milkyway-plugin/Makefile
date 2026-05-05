.PHONY: publish-core publish-gradle-plugin run-ide dev

publish-core:
	./gradlew :core:publishToMavenLocal --no-configuration-cache --rerun-tasks

publish-gradle-plugin:
	./gradlew :gradle-plugin:publishToMavenLocal --no-configuration-cache --rerun-tasks

run-ide:
	./gradlew :idea-plugin:runIde --no-configuration-cache

dev: publish-core publish-gradle-plugin run-ide