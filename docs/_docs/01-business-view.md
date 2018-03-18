---
title: "Business View"
permalink: /docs/business-view/
excerpt: "This part describes WHAT scala-adapters does."
last_modified_at: 2018-03-17
toc: true
category: business
---
{{page.excerpt}}

This content should support the **collaboration** between:
* Developers 
* Architects
* Testers
* Product Owners
* Project Managers

**Topics:**
* Feature Breakdown Structure 
* Domain Model
* New Requirements

## Use Case
The Use Case comes from my daily job with [screenfoodnet](https://www.screenfoodnet.com/de/).
We have a CMS to manage the content of the customer's digital signage projects. Now we provide a small infrastructure to allow customers extensions to the CMS, like:
* Import Data from the customers systems.
* Custom Process - e.g. with Chat Bots

![Use Case][use_case]

As you can see Batman makes sure that there goes no customer code into the CMS system. For that we provide services to the customer specific adapters.
All the other creatures stay for customers and there adapter.

In tiers we have:

![image](https://user-images.githubusercontent.com/3437927/35791017-9eeff01a-0a45-11e8-97e0-64ac183dd9be.png)

* CMS: Our existing CMS.
* adapters-CMS: A unified interface for all adapters to access the CMS.
* adapters: A small framework to provide the general adapter functionality.
* customer adapter: Here goes the customer specific code.
* customer system: The system we want to integrate with.

This Wiki is only about the generic adapters to demonstrate the Scala fullstack:
* **adapters**
* **customer adapter** > will be the examples.

## Further Reading
{% include pages-list.md %}
{% include web-links.md %}
