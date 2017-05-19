# modevelin

Modevelin is an idea for a test automation framework, based on an approach I've used on some past commercial projects. Basically
it's along the lines of a low (or rather, no) budget <a href="https://www.ca.com/gb/products/ca-service-virtualization.html">Lisa</a>, only instead
of having virtual services we just "stub" them out. The trick being that the stubbing occurs outside the boundaries of our application
classes - without the need for a separate test version of our artefact. This is done using a Java agent which redefines classes as they are loaded, 
with the practial upshot being that the app/artefact under test is none the wiser that it's actually an 
<a href="https://en.wikipedia.org/wiki/Brain_in_a_vat">"app in a vat"</a>. 

Very much still a work in progress, although it's past the PoC stage. Mainly I need to figure out / implements some of the practical
details, such as how to configure test fixtures (class redefinitions, message workflows, response validation etc) on the server side. 

![Outline of basic agent server communication during initialisation](https://github.com/downinja/modevelin/blob/master/sequence.png?raw=true)