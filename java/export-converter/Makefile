
.PHONY: build

build:
	mvn clean package -Dmaven.test.skip=true

docker:
	mvn clean package -Dmaven.test.skip=true
	docker build -t scenedoc/exporter .