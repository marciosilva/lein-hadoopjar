(ns leiningen.hadoopjar
  "Create a jar for submission as a hadoop job."
  (:require [leiningen.compile :as compile]
            [leiningen.core.classpath :as classpath]
            [cemerick.pomegranate.aether :as aether])
  (:use [leiningen.pom :only [make-pom make-pom-properties]]
        [leiningen.jar :only [write-jar]]
        [clojure.java.io :only [file]]))

(defn deps-spec [project]
  "Constructs a filespec containing all dependencies prefixed by lib/"
  (let [deps (classpath/resolve-dependencies :dependencies project)
        filespecs (map (fn [d] {:type :bytes :path (str "lib/" (.getName d)) :bytes (slurp d)})
                       deps)]
       filespecs))

(defn hadoopjar
  "I don't do a lot."
  [project & args]
  (let [jar-file (str (:root project) "/" (str (:name project) "-hadoop.jar"))]
    (println "Building " jar-file)
    (write-jar project jar-file (deps-spec project))))