---
title:  "Get Started"
permalink: /docs/03/get-started/

date:   2018-03-20
categories:
  - development
excerpt: "How to setup a new Project and get started."
---
{{page.excerpt}}

To start and create a new Project should only take a few minutes!

## Constraints
`sbt` is installed.

## Create a new Project
We provide a [Giter8][g8] to create a new Project.

Go to a directory of your choice and type:

`sbt new pme123/scala-adapters.g8`

This will guide you through the creation process.

{% highlight terminal %}
$ sbt new pme123/scala-adapters.g8
...
name [My Something Project]: Get Started
project_description [Say something about this project.]: A project to show the get started process!
scala_version [2.12.4]: 
play_version [2.6.6]: 
sbt_version [1.1.1]: 
github_id [pme123]: 
github_name [get-started]: 
developer_url [https://github.com/pme123]: 
project_url [https://github.com/pme123/get-started]: 

Template applied in ./get-started

$ cd get-started
$
{% endhighlight %}

## Running the Project
Make sure you are in the project directory, e.g. `get-started/`
{% highlight terminal %}
$ sbt run
{% endhighlight %}

**SBT remark:** You don't have to restart your server after changing the code, just refresh the Browser window. 
SBT will take care of the rest.
{: .notice--info}

See also [local-deployment][local-deployment]

Open your Browser and open:

`http://localhost:9000`

This will open the default Job in the Job Cockpit. 
Hit the Run button (red cycle) to start the dummy Job!

The content should look like:
{:.fiftyPercent}
![Screenshot Job Cockpit][screenshot_cockpit]

## Project structure
After opening and initializing with Intellij, it should look like:

{:.fiftyPercent}
![Project File structure][file_structure]

You can see now the 3 modules:

{:.fiftyPercent}
![Architecture Tiers][arch_tiers]

As mentioned for the default setup there is nothing in the shared- and client-module.

{% include web-links.md %}
