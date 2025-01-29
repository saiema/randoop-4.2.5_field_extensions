AGENT_DIR			=   agent
BUILD_LIBS			=   build/libs
VERSION				=   4.2.5
ZIP_FOLDER			=   zip_folder

.PHONY: clean
.PHONY: zip
.PHONY: compile
.PHONY: copy_jars

clean:
	./gradlew clean
	rm -f "$(ZIP_FOLDER)/randoop-$(VERSION).zip"
	rm -f "$(ZIP_FOLDER)/randoop-$(VERSION)/randoop-$(VERSION).jar"
	rm -f "$(ZIP_FOLDER)/randoop-$(VERSION)/randoop-all-$(VERSION).jar"
	rm -f "$(ZIP_FOLDER)/randoop-$(VERSION)/replacecall-$(VERSION).jar"
	rm -f "$(ZIP_FOLDER)/randoop-$(VERSION)/covered-class-$(VERSION).jar"
	rm -f "$(ZIP_FOLDER)/randoop-$(VERSION)/field_coverage_metrics.env"

compile:
	./gradlew shadowJar

copy_jars: compile
	cp "$(AGENT_DIR)/replacecall/$(BUILD_LIBS)/replacecall-$(VERSION).jar" "$(ZIP_FOLDER)/randoop-$(VERSION)/"
	cp "$(AGENT_DIR)/covered-class/$(BUILD_LIBS)/covered-class-$(VERSION).jar" "$(ZIP_FOLDER)/randoop-$(VERSION)/"
	cp "$(BUILD_LIBS)/randoop-$(VERSION).jar" "$(ZIP_FOLDER)/randoop-$(VERSION)/"
	cp "$(BUILD_LIBS)/randoop-all-$(VERSION).jar" "$(ZIP_FOLDER)/randoop-$(VERSION)/"
	cp "field_coverage_metrics.env" "$(ZIP_FOLDER)/randoop-$(VERSION)/"

zip: copy_jars
	mkdir -p "$(ZIP_FOLDER)"
	cd "$(ZIP_FOLDER)" && \
	zip -r "randoop-$(VERSION).zip" "randoop-$(VERSION)/"
