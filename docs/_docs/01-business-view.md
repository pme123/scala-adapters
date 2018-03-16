---
title: "Business View"
permalink: /docs/business-view/
excerpt: "Business aspects of the project."
last_modified_at: 2018-01-02T16:28:04-05:00
toc: true
sidebar:
  nav: docs
---
This page is the entry page for all aspects that relate with the business side, like:
* Use Cases / Features
* Domain Model

## Use Case
The Use Case comes from my daily job with [screenfoodnet](https://www.screenfoodnet.com/de/).
We have a CMS to manage the content of the customer's digital signage projects. Now we provide a small infrastructure to allow customers extensions to the CMS, like:
* Import Data from the customers systems.
* Custom Process - e.g. with Chat Bots

![image](https://user-images.githubusercontent.com/3437927/35923565-e83ce1e2-0c20-11e8-911f-c255323a5cee.png)

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