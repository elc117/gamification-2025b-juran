SHELL := /bin/bash
PORT ?= 3000
# BASE_URL = https://$${CODESPACE_NAME}-$${PORT}.$${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}
BASE_URL = $(shell echo "https://${CODESPACE_NAME}-${PORT}.${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}")

.PHONY: gamification stop clean db ls test-gamification


gamification:
	PORT=$(PORT) gradle -q runGamification

stop:
	-pkill -f demo.Gamification || true

clean:
	gradle clean

db:
	ls -l treinos.db || true


# ------------------------------------------------------------
# Tests using curl against Codespaces forwarded URL
# ------------------------------------------------------------


test-gamification:
	@echo -e "Testing gamification..."
	@echo -e "\n--> Health check"
	curl -s $(BASE_URL)/healthz && echo

	@echo -e "\n--> Listar treinos"
	curl -s $(BASE_URL)/treinos | jq .

	@echo -e "\n--> Criar treino (arremesso)"
	curl -s -X POST $(BASE_URL)/treinos \
		-H 'content-type: application/json' \
		-d '{"tipo":"arremesso","quantidade":10,"data":"2023-10-01"}' | jq .

	@echo -e "\n--> Buscar treino por ID (1)"
	curl -s $(BASE_URL)/treinos/1 | jq . 

	@echo -e "\n--> Atualizar treino (ID 1)"
	curl -s -X PUT $(BASE_URL)/treinos/1 \
		-H 'content-type: application/json' \
		-d '{"tipo":"arremesso","quantidade":15,"data":"2023-10-01"}' | jq .

	@echo -e "\n--> Deletar treino (ID 1)"
	curl -s -X DELETE $(BASE_URL)/treinos/1

    @echo -e "\n--> Status do usuário (gamificação)"
	curl -s $(BASE_URL)/usuario | jq .

# Print info 
ls:
	@echo "Codespaces base URL: $(BASE_URL)"

