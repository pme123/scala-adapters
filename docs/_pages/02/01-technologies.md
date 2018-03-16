---
title:  "Full-Stack Technologies"
permalink: /docs/02/technologies/

date:   2015-11-17 16:16:01 -0600
categories:
  - architecture
excerpt: "What technologies are in use in this Scala full-stack implementation"
---
We structure the technologies by the Architecture Tiers.
![Architecture Tiers]({{ "/assets/images/02/arch_tiers.png" | absolute_url }})

## client
* [ScalaJS](https://www.scala-js.org)
* [Binding.scala](https://github.com/ThoughtWorksInc/Binding.scala): reactive web-UI-library.
* [Semantic-UI](https://semantic-ui.com): a semantic CSS-styling

## shared
All shared libraries have a version for ScalaJS and one for Scala(JVM). And so are also used on client and server.
* Scala/ ScalaJS
* [play-json](https://www.playframework.com/documentation/2.6.x/ScalaJson)/ [play-json-derived-codecs](https://github.com/julienrf/play-json-derived-codecs): JSON handling 
* [slogging](https://github.com/jokade/slogging): Used logging library

## server
* [Scala](https://www.scala-lang.org)
* [Play](https://www.playframework.com)