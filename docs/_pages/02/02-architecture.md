---
title:  "Architecture explained"
permalink: /docs/02/architecture/
date:   2018-03-18
categories:
  - architecture
excerpt: "A short explanation of the Architecture and its important classes."
---

{{page.excerpt}}

## Modules

As seen on the [architecture-view][architecture-view] page, here the involved modules of:
* scala-adapters (the framework)
* adapters-project (the project using scala-adapters)

![Architecture Modules][arch_modules]

## Important Classes

The next diagram displays the most important classes and their relations.

![Important Classes][arch_important_classes]

## Server-Client Communication
Next to REST-services there is a _Bi-Directional_ communication with a Websocket.

As soon as you register to the standard Websocket, you will be informed on changes on your Job immediately.

The next diagram shows the process of registering a Client:
![Websocket Creation][websocket_creation]

In this creation process, it is possible to create Jobs (with different configuration) dynamically:

{:.seventyPercent}
![Job Creation][job_creation]


{% include web-links.md %}
