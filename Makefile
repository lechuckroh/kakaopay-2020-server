PHONY: clean image push

DOCKER_IMAGE_NAME=lechuckroh/kakaopay-2020-server:latest

clean:
	@./gradlew clean

image: clean
	@docker build -t $(DOCKER_IMAGE_NAME) .

push:
	@docker push $(DOCKER_IMAGE_NAME)
