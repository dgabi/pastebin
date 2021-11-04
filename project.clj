(defproject pastebin "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [http-kit "2.5.3"]
                 [integrant "0.8.0"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.5.9"]
                 [ring/ring-jetty-adapter "1.8.1"]
                 [ring-cors/ring-cors "0.1.13"]
                 [metosin/reitit-ring "0.5.15"]]
  :main pastebin.core
  :repl-options {:init-ns pastebin.core})
