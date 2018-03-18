---
title: "Architecture View"
permalink: /docs/architecture-view/
excerpt: "This content explains **HOW** scala-adapters is implemented."
last_modified_at: 2018-01-02T16:28:04-05:00
toc: true
category: architecture
---
{{page.excerpt}}

This content should support the **collaboration** between:

* Developers 
* Architects

**Topics:**

* System Architecture
* Design Pattern
* Implementation Guidelines
* Technical Decisions
* Non-functional Requirements
* Quality Assurance QA

## Main Structure
A Full Stack projects always consists of 3 modules:
* **client**: Code that runs in the browser.
* **shared**: Code that runs in the browser and on the server.
* **server**: Code that runs on the server.

{:.fiftyPercent}
![Architecture Tiers][arch_tiers]

## Use Case Architecture
Coming from the Use Case we have the following setup:

![Architecture Use Case][arch_use_case]

This allows us also to have a look on reusability of server and client code (over the project borders).

## Further Reading
{% include pages-list.md %}
{% include web-links.md %}
