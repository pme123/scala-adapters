---
title:  "Custom Controller"
permalink: /docs/03/custom-controller/

date:   2018-03-20
categories:
  - development
excerpt: "Create and use a Custom Controller."
---
{{page.excerpt}}

The example is based on `DemoResultController` from [scala-adapters]. 

By default no Play-Controller is needed. 
When does it make sense to create a Custom Controller:
* More simple or semantic URLs.
* Provide functionality that is not related to Job Processing.
 
## Constraints
* You have created the project with [scala-adapters-g8].
* You have your Custom Page ready ([custom-page]).
* A basic understanding of Play Controller and -Routes (see [Play]). 

## Create a Controller
We will create a Controller to provide the [custom-page] on the root URL `http://localhost:9000`.
{% highlight scala %}
@Singleton
class GetStartedController @Inject()(jobController: JobCockpitController
                                     , cc: ControllerComponents
                                     , val config: Configuration)
                                    (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {

  // delegate the index file to the defaultCustomPage
  def index: Action[AnyContent] = jobController.defaultCustomPage()

}
{% endhighlight %}

## Add the Routing
Let's add the Route in `conf/routes`:
{% highlight scala %}
# Here are the custom paths. By default none are needed.
GET        /        server.GetStartedController.index

# Reuse the routes from the ADAPTERS project
->         /        adapters.Routes
{% endhighlight %}

Now we use for / our Controller, everything else is still delegated to the `adapters.Routes`.

## Check Result
Running the Project and **running the Job**,
 should give you now your Custom page on `http://localhost:9000`.



{% include web-links.md %}
