# modevelin

Modevelin is an idea for a test automation framework, based on an approach I've used on some past commercial projects. Basically
it's along the lines of a low-budget <a href="https://www.ca.com/gb/products/ca-service-virtualization.html">Lisa</a>, only instead
of having virtual services we just stub them out. The trick being that rather than roll up a separate test version of our artefact
with e.g. its own Spring configuration file injecting the stubs, we use Java agent libs to redefine classes as they are loaded, 
with the practial upshot being that the app/artefact under test is none the wiser that it's actually an 
<a href="https://en.wikipedia.org/wiki/Brain_in_a_vat">"app in a vat"</a>. 

I haven't played around enough with e.g. Docker to know whether this approach is now redundant, but it may have its uses - and can also
be used (or more likely misused) with component level testing under JUnit.

Very much still a work in progress, although it's past the PoC stage. Mainly I need to figure out / implements some of the practical
details, such as how to configure everything (class redefinitions, text fixtures, response validation etc). 
