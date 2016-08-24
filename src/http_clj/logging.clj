(ns http-clj.logging)

(defprotocol Logger
  (log [this message]))
