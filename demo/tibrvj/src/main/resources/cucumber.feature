# So where do we want Cucumber to run from, e.g. from outside of docker-compose,
# or as part of the modevelin server container? If outside, then we need a way of
# accessing the server API over e.g. a socket or web server, and also we need to
# know the IP address for this (so either manually pass it in to the tests as a
# configuration parameter, or else query via compose to find the IP of the server
# container and assume a known port, or somesuch. Which we might want to support,
# but which feels (for now) a bit OTT rather than just having the cucumber steps
# run in-process with the modevelin server and just use the regular java API to
# interact with it. Although this then (arguably) makes it harder to invoke the
# cucumber features, especially if we want to set up and tear down the modevelin
# server multiple times, since then we'll need some process outside of compose
# to call up and down. Also, the IDE integration might suffer e.g. the cucumber
# outputs / logs don't get syntax highlighted the same way that running them
# directly from the IDE might do. Plus if we want the cucumber outputs to
# outlive a run of compose then we might need to set up volumes or somesuch.

Feature: modevelin-demo-tibrvj-feature
  # Enter feature description here

  Scenario: tibrvj-demo-client receives and sends a message
    Given a modevelin server running on port 8888
    When agent "tibrvj-demo-client" is ready
    Then send tibrvj message to agent "tibrvj-demo-client" with messageId "1" body "BODY" SENDSUBJ "FOO2YOU" and REPLYSUB "FROMFOO"
    Then receive tibrvjMessage from agent "tibrvj-demo-client" with messageId "1" body "BAR"


