---
title: "Architecture View"
permalink: /docs/architecture-view/
excerpt: "The Architecture and important Design aspects of the project. This page explains **HOW** scala-adapters is implemented."
last_modified_at: 2018-01-02T16:28:04-05:00
toc: true
category: architecture
---
{{page.excerpt}}
## Main Structure
A Full Stack projects always consists of 3 modules:
* **client**: Code that runs in the browser.
* **shared**: Code that runs in the browser and on the server.
* **server**: Code that runs on the server.

![Architecture Tiers]({{ "/assets/images/02/arch_tiers.png" | absolute_url }})

## Use Case Architecture
Coming from the Use Case we have the following setup:

![image](https://user-images.githubusercontent.com/3437927/35976310-01a7bdcc-0ce0-11e8-846a-f3ec1eb914cb.png)

This allows us also to have a look on reusability of server and client code (over the project borders).

## Further Reading
{% include pages-list.md %}