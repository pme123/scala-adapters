---
title: "Using Redux with Binding.scala"
categories:
  - development
excerpt: "My interpretation on how to use the Redux framework with Binding.scala."

tags:
  - binding.scala
  - client
  - scalajs
  - redux
---
{{page.excerpt}}

This blog is about Redux and Binding.scala. 
If you don't know them please check these intros first:
* [A Beginners Guide to Redux][Redux-intro]
* [Binding.scala][Binding]

I used Redux in a Polymer/ Typescript project and I really liked it. 
So I wanted to use it in my Binding.scala projects as well.

## Principles of Redux
Let's go through the Redux principles and see how I implemented it (if possible) in 
[scala-adapters][scala-adapters] with _Binding.scala_.

### Single Source of Truth
> The state of your whole application is stored in an object tree within a single store.

I have a Singleton UIStore that holds all the required state. 
{% highlight scala %}
object UIStore extends Logger {
  val uiState = UIState()
  ...
{% endhighlight %}

### State is Read Only
>The only way to change the state is to emit an action, an object describing what happened.

The UIStore is responsible for all state changes. 
So if a component wants to change a state it calls a dedicated function of the store.
{% highlight scala %}

object UIStore extends Logger {
  val uiState = UIState()

  protected def clearLogData() {
    info("UIStore: clearLogData")
    uiState.logData.value.clear()
  }

  protected def addLogReport(logReport: LogReport) {
    info(s"UIStore: addLogReport")
    uiState.logData.value ++= logReport.logEntries
  }

  protected def addLogEntry(logEntry: LogEntry) {
    info(s"UIStore: addLogEntry ${logEntry.level}: ${logEntry.msg}")
    uiState.logData.value += logEntry
  }
  ...
}
{% endhighlight %}

This is maybe the weakest point in my implementation.

As the components can read the state (`UIStore.uiState`), 
it's only a convention that they will not change the state directly (which is possible with `Binding.Var` and `Binding.Vars`). 

This is how the state looks like:
 {% highlight scala %}
case class UIState(logData: Vars[LogEntry] = Vars[LogEntry]()
                   , isRunning: Var[Boolean] = Var(false)
                   , filterText: Var[String] = Var("")
                   , filterLevel: Var[LogLevel] = Var[LogLevel](LogLevel.INFO)
                   , lastLogLevel: Var[Option[LogLevel]] = Var[Option[LogLevel]](None)
                   , logEntryDetail: Var[Option[LogEntry]] = Var[Option[LogEntry]](None)
                   ...
                  )
 {% endhighlight %}

### Use Pure Functions for Changes
>To specify how the state tree is transformed by actions, you write pure reducers.

{% highlight scala %}
  protected def addLogReport(logReport: LogReport) {
    info(s"UIStore: addLogReport")
    uiState.logData.value ++= logReport.logEntries
  }
{% endhighlight %}

This is not the case here as the update function mutates the state.

## Three Pillars of Redux
Let's compare it to a standard implementation of Redux.

## Store
As seen above we use also a singleton Store (`object UIStore`).

## getState
`UIStore.uiState` gives you the state. Here we have the next big difference:
* In Redux the State is evaluated and created with each change.
* With Binding.scala we only change the value(s) of the `Var` and `Vars`.

So this object (reference) does not change (only its values).

## dispatch
As with Scala.js everything is type safe, 
I saw no sense to create actions that the components could dispatch.

Instead the `UIStore` provides for each _change-Action_ a dedicated function, like:
{% highlight scala %}
  protected def addLogReport(logReport: LogReport) {
    info(s"UIStore: addLogReport")
    uiState.logData.value ++= logReport.logEntries
  }
{% endhighlight %}

The only disadvantage I see is that the Logging is needed in each function
(not just in one `dispatch` function).

## subscribe
Here we just bind the values we are interested in.
{% highlight scala %}
  val logData = uiState.logData.bind
{% endhighlight %}

## unsubscribe
According to [Binding.scala][Binding] this is handled automatically.

## Action
As mentioned with `dispatch`, my Actions are concrete functions.

## Reducers
We could say that the _dispatch-function_ and the _reducer-functions_ are merged into _change-functions_. 

But as mentioned they are not pure.

# Conclusion
With Scala.js (type-safe) and Binding.scala (data-binding) the Redux framework can
be reduced quite a lot.

Or in other words: 
_We can use some of the principles and implementation ideas of Redux._

Two points that would be nice:
* ensure that the components cannot modify the state
* having a log filter that log the changes in a generic way

Let me know if you see improvements or mistakes in my thinking!


{% include web-links.md %}
