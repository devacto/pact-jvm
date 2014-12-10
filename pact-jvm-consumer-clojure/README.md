pact-jvm-consumer-clojure
==========================

Clojure DSL for Pact JVM

### Usage

Sample code to create pact file is below:

```clojure
(defpacttest sample-test
             :pact-filename "resources/test-me2.json"
             :consumer "orion-service"
             :provider "retail-insurance-service"
             :client-fn get-insurance-control-records
             :expected [{:name         "get policy controls"
                         :method       :get
                         :uri          "/policyUpdateControl"
                         :status       200
                         :query-string "status=RESPONSE_RECEIVED&insurerCode=ZUR"
                         :schema       {:policyUpdateControls [(s/one policy-update-control "first-control")
                                                               (s/maybe policy-update-control)]}
                         }])
```
