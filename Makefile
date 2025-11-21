.PHONY: run build clean

run:
	chmod +x ./gradlew
	./gradlew runGamification

build:
	chmod +x ./gradlew
	./gradlew build

clean:
	chmod +x ./gradlew
	./gradlew clean

wrapper:
	chmod +x ./gradlew
	./gradlew wrapper