all: app gateway

push: pushapp pushgateway

test: testapp testgateway

clean: cleanapp cleangateway cleandeploy

.PHONY: deploy
deploy:
	@echo Creating SSL assets
	mkdir -p deploy/artifacts/
	util/generate-self-signed-ssl-assets.sh echod deploy/artifacts/
	util/generate-self-signed-ssl-assets.sh echod-client deploy/artifacts/
	@echo Creating JWT signing assets
	util/generate-jwt-signing-keys.sh jwt-verification deploy/artifacts/
	@echo Deploying to Kubernetes cluster via Helm
	@cd deploy; \
	helm install echod \
		--set serverCert=$(shell cat deploy/artifacts/echod-cert.pem | base64),serverKey=$(shell cat deploy/artifacts/echod-key.pem | base64),serverCaCert=$(shell cat deploy/artifacts/echod-ca-cert.pem | base64),clientCert=$(shell cat deploy/artifacts/echod-client-cert.pem | base64),clientKey=$(shell cat deploy/artifacts/echod-client-key.pem | base64),clientCaCert=$(shell cat deploy/artifacts/echod-client-ca-cert.pem | base64),jwtVerificationKey=$(shell cat deploy/artifacts/jwt-verification-public.pem | base64)

cleandeploy:
	cd deploy; \
	rm -rf artifacts/

.PHONY: app
app:
	cd app; sbt docker:publishLocal

pushapp:
	cd app; sbt docker:publish

testapp:
	cd app; sbt clean coverage test

cleanapp:
	cd app; sbt clean

.PHONY: gateway
gateway:
	cd gateway; make docker

pushgateway:
	cd gateway; make push

testgateway:
	cd gateway;  # TODO: Run tests for gateway

cleangateway:
	cd gateway;  make clean

