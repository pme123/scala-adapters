---
title:  "Feature Breakdown Structure FBS"
permalink: /docs/01/fbs/
date:   2018-03-29
categories:
  - business
excerpt: "What Features are handled by scala-adapters."
---
{{page.excerpt}}

We structure the functionality in a so called Feature Breakdown Structure (from the Feature Driven Development). The structure has the following elements:

Activities. 

* **Subsystem**: This can be a physically or logically separate system that can be handled independently of each other.
* **Business Activity**: A group of Features that can be grouped together from a Business point of view. An ideal size of Features would be 10 to 20.
* **Feature**: The implemented functionality.

The Feature itself is then described in Tables with this structure:

* **Feature**: This is the Feature itself.
* **Contract Description**: Describes the Contract you have to follow in order to use the functionality. The goal is to have a consistent behavior for functionality that does similar things.
* **Examples**: This could be:
  * Acceptance Tests that verify the correctness of the feature.
  * Code that uses this feature.
  * Screenshots of the UI.
  
## Business Activities
The overview shows the Business Activities in relation to the Subsystems.

{:.fiftyPercent}
![Business Activities][business_activities]


Subsystem | Description
---|---
scala-adapters | This Project that provides the Features described here.

Actor | Description
---|---
adapters-project | Your project that uses scala-adapters.
Admin | The User that can monitor and run the Jobs manually.
Third Party System | Optional third party system your process uses.

Each Project is structured in the following way:

{:.fiftyPercent}
![Job- and Client Handling][job_client_handling]

The following Features will refer to this structure.

## Job Processing
**TODO**

Feature | Contract Description | Examples
---|---|---
Add the JobProcess to a Project. | TODO | See [server-jobs]

## Job- and Client Handling

**TODO**

## Job Monitoring
**TODO**

Feature | Contract Description | Examples
---|---|---
Provide the registered Jobs to an Admin. | Path: `/jobConfigs` <br>Parameters: - | See [server-jobs]





{% include web-links.md %}
