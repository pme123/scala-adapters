---
permalink: /about/
title: "About"
excerpt: "This is an overview on **WHY** I did this framework."
last_modified_at: 2018-01-10T11:22:24-05:00
toc: true
author_profile: true
---

As a server developer I am looking for an easy way to provide a GUI for my server projects. 
This projects uses a **Scala full-stack**, that is for now the best way do achieve that. 

**This Project has the following purpose:**
* Try this Scala full-stack in a productive project.
* Get some experience in moving to open source.
* Using free open source tools and technologies (like Jekyll and this theme;). 

**The focus is on:**
* the interaction between the server and the client (shared).
* the client

For the server part just check the Play documentation.

**The documentation is organized in 4 views:**
1. [Business View](docs/business-view/)
1. [Architecture & Design View](docs/architecture-view/)
1. [Development View](docs/development-view/)
1. [Deployment View](docs/deployment-view/)

This is how I always organize the docs for the different stack holders that are involved.

## Notable Features

- Setup and running in 5 minutes.
- Compatible with a Scala/ Play project.
- By default no shared- and client implementation needed.
- Styling with [Semantic-UI][Semantic] is setup and ready to use.
- Binding.scala allows you to work with type-safe XHTML tags.

## Examples

| Github                                           | Demo                                                                                                | Description                                                                                          |
| ------------------------------------------------ | --------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| [scala-adapters][scala-adapters]                 | [demo][demo] :: [cockpit-demo][demo_cockpit] :: [results-demo][demo_results]                            | scala-adapters includes a demo project that shows the main features.                                 |
| [scala-adapters-example][scala-adapters-example] | [demo-example][demo_example]                                                                        | scala-adapters-example shows a minimal project that is identical to a newly one created with giter8. |
| [scala-adapters-images][scala-adapters-images]   | [images][demo_images] :: [cockpit-images][demo_images_cockpit] :: [results-images][demo_images_results] | scala-adapters-images has a simple process that sends a flag to switch the page (Emojis or Photos).  |

## Credits
{% include credits.md %}

Scala Adapters is designed, developed, and maintained by Pascal Mengelt. {{ author.bio }}

{% include web-links.md %}
