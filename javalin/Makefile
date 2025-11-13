SHELL := /bin/bash
PORT ?= 3000
# BASE_URL = https://$${CODESPACE_NAME}-$${PORT}.$${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}
BASE_URL = $(shell echo "https://${CODESPACE_NAME}-${PORT}.${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}")

.PHONY: hello advice-text advice-json poi sqlite stop clean db ls test-hello

hello:
	PORT=$(PORT) gradle -q runHello

advice-text:
	PORT=$(PORT) gradle -q runAdviceText

advice-json:
	PORT=$(PORT) gradle -q runAdviceJson

poi:
	PORT=$(PORT) gradle -q runPoi

sqlite:
	PORT=$(PORT) gradle -q runSqlite

stop:
	-pkill -f demo.HelloJavalin || true
	-pkill -f demo.RandomAdviceService || true
	-pkill -f demo.RandomAdviceServiceJson || true
	-pkill -f demo.PoiService || true
	-pkill -f demo.SqliteService || true

clean:
	gradle clean

db:
	ls -l users.db || true


# ------------------------------------------------------------
# Tests using curl against Codespaces forwarded URL
# ------------------------------------------------------------

test-hello:
	@echo "Testing HelloJavalin"
	curl -s $(BASE_URL)/

test-advice-text:
	@echo "Testing RandomAdviceService"
	curl -s $(BASE_URL)/advice

test-advice-json:
	@echo "Testing RandomAdviceServiceJson"
	curl -s $(BASE_URL)/advice | jq .

test-poi:
	@echo "Testing PoiService"
	curl -s $(BASE_URL)/poi | jq .
	curl -s $(BASE_URL)/poilist | jq .
	curl -s $(BASE_URL)/near/-29.71689/-53.72968 | jq .

test-sqlite:
	@echo -e "Testing SqliteService..."
	@echo -e "\n--> SqliteService: health"
	curl -s $(BASE_URL)/healthz && echo

	@echo -e "\n--> SqliteService: list users"
	curl -s $(BASE_URL)/users | jq .

	@echo -e "\n--> SqliteService: create user"
	curl -s -X POST ${BASE_URL}/users \
		-H 'content-type: application/json' \
		-d '{"name":"Ada","email":"ada@example.org"}' 

	@echo -e "\n\n--> SqliteService: get by id"
	curl -s ${BASE_URL}/users/1 

	@echo -e "\n\n--> SqliteService: update user"
	curl -s -X PUT ${BASE_URL}/users/1 \
		-H 'content-type: application/json' \
		-d '{"name":"Ada Lovelace","email":"ada@math.org"}' 

	@echo -e "\n\n--> SqliteService: delete user"
	curl -s -X DELETE ${BASE_URL}/users/1 

# Print info 
ls:
	@echo "Codespaces base URL: $(BASE_URL)"

