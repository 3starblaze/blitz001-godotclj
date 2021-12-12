PROJECT_DIR=$(PWD)
GODOT_HEADERS=$(PROJECT_DIR)/godot-headers
BUILD=$(PROJECT_DIR)/build
BIN=$(PROJECT_DIR)/bin
CLASSES=$(PWD)/classes
CLASSPATH=$(shell clj -Spath | clj -M -e "(require 'godotclj.paths)")

CLJ=clj -Scp $(CLASSPATH)

ifeq ($(RUNTIME),graalvm)
JAVA_HOME=$(GRAALVM_HOME)
else
JAVA_HOME=$(shell clj -M -e "(println (System/getProperty \"java.home\"))")
endif

JAVA_PATH=$(JAVA_HOME)/bin:$(PATH)

export PROJECT_DIR GODOT_HEADERS BUILD BIN CLASSES CLASSPATH

all: $(BIN)/libgodotclj_gdnative.so godotclj/src/clojure/godotclj/api/gdscript.clj

clean:
	rm -fr .cpcache
	$(MAKE) -C godotclj clean

godotclj/src/clojure/godotclj/api/gdscript.clj:
	$(MAKE) -C godotclj src/clojure/godotclj/api/gdscript.clj

aot: godotclj/src/clojure/godotclj/api/gdscript.clj
	mkdir -p $(CLASSES)
	PATH=$(JAVA_PATH) \
	$(CLJ) -J-Dtech.v3.datatype.graal-native=true \
		-J-Dclojure.compiler.direct-linking=true \
		-J-Dclojure.spec.skip-macros=true \
		-M -e "(set! *warn-on-reflection* true) (with-bindings {#'*compile-path* (System/getenv \"CLASSES\")} (compile 'thecreeps.main))"

$(BIN)/%.so: godotclj/src/clojure/godotclj/api/gdscript.clj
	$(MAKE) -C godotclj $@
