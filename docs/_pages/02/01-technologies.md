---
title:  "Full-Stack Technologies"
permalink: /docs/02/technologies/
date:   2018-03-16
categories:
  - architecture
excerpt: "What technologies are in use in this Scala full-stack implementation."
---
{{page.excerpt}}

We structure the technologies by the Architecture Tiers.

{:.fiftyPercent}
![Architecture Tiers][arch_tiers]

## client
### [ScalaJS][ScalaJS]
- A safer way to build robust front-end web applications!
- Working on the client side with familiar Scala. 
- Of course not everything is supported, 
especially if you use Java-Libraries. 
But you will be surprised how much is already supported (see here [ScalaJS Libraries][ScalaJSLib]).
- Version 1 is soon to be released

### [Binding.scala][Binding]
I tried quite some libraries before I came across this one (eg. Scalatags, scalajs-react, Outwatch etc.).
- Binding.scala is a data-binding framework for Scala, running on both JVM and Scala.js.
- Use your XHTML code directly in your Scala code - and of course it is type save.
- Easily to compose, alike your server code.
- A great way to manage the GUIs state, you can forget about Redux - this is way simpler.
- **This framework is actually the reason for this page - I love it;)**

### [Semantic-UI][Semantic]
- easy way to create nice Web-UIs.
- like Bootstrap, but semantic style.

## shared
All shared libraries have a version for ScalaJS and one for Scala(JVM). 
And so everything here can also be used on client and server.

### [Scala][Scala]/ [ScalaJS][ScalaJS]
The shared module compiles to the JVM and to Javascript. 
So everything you use here must support that.

### [play-json][PlayJson]
- Marshalling and Unmarshalling of JSON.

### [play-json-derived-codecs][PlayJsonCodecs]
- Reads, OWrites and OFormat derivation for algebraic data types (sealed traits and case classes, possibly recursive), powered by [Shapeless][Shapeless].

### [slogging][SLog]
- Logging library that supports:
  - SLF4J on the server
  - Console Logger on the browser

## server
The server is a standard Scala/ Play application.
### [Scala][Scala] / [Play][Play]

{% include web-links.md %}
