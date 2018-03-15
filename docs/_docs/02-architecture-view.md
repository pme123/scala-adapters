---
title: "Architecture View"
permalink: /docs/architecture-view/
excerpt: "The Architecture and important Design aspects of the project. This page explains **HOW** scala-adapters is implemented."
last_modified_at: 2018-01-02T16:28:04-05:00
toc: true
---
{{page.excerpt}}
## Main Structure
A Full Stack projects always consists of 3 modules:
* **client**: Code that runs in the browser.
* **shared**: Code that runs in the browser and on the server.
* **server**: Code that runs on the server.

![image](https://user-images.githubusercontent.com/3437927/35779088-f61630e4-09c7-11e8-8bad-599e2d5aeb4b.png)

## Technology
### client
* [ScalaJS](https://www.scala-js.org)
* [Binding.scala](https://github.com/ThoughtWorksInc/Binding.scala): reactive web-UI-library.
* [Semantic-UI](https://semantic-ui.com): a semantic CSS-styling
### shared
All shared libraries have a version for ScalaJS and one for Scala(JVM). And so are also used on client and server.
* Scala/ ScalaJS
* [play-json](https://www.playframework.com/documentation/2.6.x/ScalaJson)/ [play-json-derived-codecs](https://github.com/julienrf/play-json-derived-codecs): JSON handling 
* [slogging](https://github.com/jokade/slogging): Used logging library
### server
* [Scala](https://www.scala-lang.org)
* [Play](https://www.playframework.com)

## Use Case Architecture
Coming from the Use Case we have the following setup:

![image](https://user-images.githubusercontent.com/3437927/35976310-01a7bdcc-0ce0-11e8-846a-f3ec1eb914cb.png)

This allows us also to have a look on reusability of server and client code (over the project borders).
