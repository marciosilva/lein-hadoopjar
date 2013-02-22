(ns leiningen.hadoopjar
  "Create a jar for submission as a hadoop job."
  (:require [leiningen.pom :as pom]
	    [leiningen.compile :as compile]
            [leiningen.core.classpath :as classpath]
            [cemerick.pomegranate.aether :as aether])
  (:use [leiningen.pom :only [make-pom make-pom-properties]]
        [leiningen.jar :only [write-jar]]
        [clojure.java.io :only [file]])
  (:import [java.io RandomAccessFile]))


(defn- get-bytes [^java.io.File file]
  (let [randomfile (RandomAccessFile. file "r")
        length     (.length randomfile)
	buff       (byte-array length)]
	(.readFully randomfile buff)
	buff))

(defn- base-spec [project]
  (concat [{:type :bytes
            :path (format "META-INF/maven/%s/%s/pom.xml"
                          (:group project) (:name project))
            :bytes (.getBytes (pom/make-pom project))}
           {:type :bytes
            :path (format "META-INF/maven/%s/%s/pom.properties"
                          (:group project) (:name project))
            :bytes (.getBytes (pom/make-pom-properties project))}
           {:type :bytes :path (format "META-INF/leiningen/%s/%s/project.clj"
                                       (:group project) (:name project))
            :bytes (.getBytes (slurp (str (:root project) "/project.clj")))}
           {:type :bytes :path "project.clj"
            :bytes (.getBytes (slurp (str (:root project) "/project.clj")))}]
          [{:type :path :path (:compile-path project)}
           {:type :paths :paths (:resource-paths project)}]
          (if-not (:omit-source project)
            [{:type :paths :paths (:source-paths project)}
             {:type :paths :paths (:java-source-paths project)}])
          (:filespecs project)))

(defn deps-spec [project]
  "Constructs a filespec containing all dependencies prefixed by lib/"
  (let [deps (classpath/resolve-dependencies :dependencies project)
        filespecs (map (fn [d] {:type :bytes :path (str "lib/" (.getName d)) :bytes (get-bytes d)})
                       deps)]
       filespecs))

(defn hadoopjar
  "Builds a hadoop job jar by packaging all the dependencies into a lib/ folder"
  [project & args]
  (let [jar-file (str (:root project) "/" (str (:name project) "-hadoop.jar"))]
    (println "Building Job Jar:" jar-file)
    (write-jar project jar-file (concat (base-spec project) (deps-spec project)))))
