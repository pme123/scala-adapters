---
title: "Deployment View"
permalink: /docs/deployment-view/
excerpt: "This part describes **WHERE** scala-adapters runs. "
last_modified_at: 2018-01-02T16:28:04-05:00
category: deployment
---
{{page.excerpt}}

This content should support the collaboration between:
* Developers 
* Architects
* System Administrators
* System Operators

**Topics:**
* System Requirements
* System environments
* Supported Technologies / Formats
* Operation Instructions
* Troubleshooting/System optimizing tasks

## Continous Integration CI
We added  a `.travis.yml` to [scala-adapters-g8][scala-adapters-g8].
At the moment the sbt command looks a bit complex:
sbt ++$TRAVIS_SCALA_VERSION clean sharedJVM/test sharedJS/test server/test 'set scalaJSStage in Global := FullOptStage'`

The problem is described here: [scalajs-scalatest-referenceerror-jquery-is-not-defined](https://stackoverflow.com/questions/48395676/scalajs-scalatest-referenceerror-jquery-is-not-defined)
  
## Further Reading
{% include pages-list.md %}
{% include web-links.md %}
