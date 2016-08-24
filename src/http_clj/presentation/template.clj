(ns http-clj.presentation.template
  (:require [hiccup.core :refer [html]]))

(defn directory [files]
  (html [:ul
         (for [{:keys [href name]} files]
           [:li [:a {:href href} name]])]))
