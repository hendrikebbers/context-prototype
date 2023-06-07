# How to use a context for base operations

This document will describe the idea of a global context concept that can be used by all base facilities to collect and
provide metadata in an easy way. The concept use several functionalities that are used in microservice environments
today and maps that concepts to a modulitic monolith.

## Definition of a context

When talking about a context a specific state is meant. This state can be specific to the complete process or to a
thread. In HTTP based services such context is often used to store information about the current request. In our
scenario the context can be used to store information about a current workflow like a transaction.

The data of a context is normally stored in a key-value-map. That means that key-value pairs can easily be stored in a
context. To keep the context as simple as possible storing string-pairs is a good way to go. Based on that a context can
be defined by the following api draft:

```java

public interface Context {

    String get(String key);

    void set(String key, String value);

    void remove(String key);

    Stream<String> keys();

}

```

As said a common usecase is to have a thread bound context. That means that the context is bound to the current thread.
By using `ThreadLocal` this can be easily achieved and the ThreadContext can be accessed in a static and easy way:

```java

ThreadContext.set("key","value");

```

## The benefit of having context based metadata

The benefit of having a context based metadata is that it can be used by all base facilities to collect and provide
data. A common scenario is logging that can add the metadata of a context automatically to all log messages that are
handled. The following illustrations shows an example of that behavior:

![Context based logging](./logger-acces-context.drawio.png)

By doing so a simple `log.info("Hello World")` call can result in the following log message:

```
2023-06-07T11:50:17.345943 - INFO - Hello World - {spanId=5e337baa-1bb8, nodeId=0x3d4e, thread=Thread-1}
```

In that sample the information that is added in the curly brackets is the context metadata that is automatically
added by the logger. For logging that pattern is called `MDC` (Mapped Diagnostic Context). Using MDC is a common pattern
that provides a lot of benefit. While we can already extract some good information from the log line it becomes more
interesting when we use a log aggregation tool like `ELK` (Elasticsearch, Logstash, Kibana). By using such a tool we can
easily search for all log messages that are related to a specific transaction or workflow. The following illustration
shows an example of that:

![Kibana logging filter](./kibana-filters.png)

As you can see in the image such tooling can handle metadata of log messages (like it defined for example in the GELF
format) and provide useful views and visualization for such metadata. Like Prometheus and Grafana we can provide a
Docker based environment of our own log aggregation tooling and provide that functionality to all developers.

## How a resource manager can use the context

We plan to define a so-called resource manager in future that takes care of the lifecycle of resources like threads.
Such manager can provide a lot of information to the context. Let's assume we have a specific resource manager for the
platform and a manager for the node. Whenever a thread is created the resource manager can add that information directly
to the thread context of the new thread. Such information contains the resource manager id, the thread name and the
thread type (native, virtual) for example. Once we make more use of the concurrency api of Java instead of using the
`Thread` class directly we can even define task information like the task type and a description in the context. A
custom Executor can easily be created that adds such information to the context before executing a task. The following
code snippet shows how that might look like:

```java

resourceManager.executeTask("checkAccountBalance","This tasks checks the account balance for "+account,()->{
        // do something
        });

```

As long as the `Runnable` that is passed as a lambda expression is executed the context information about that task is
stored in the context:

```java

assert"checkAccountBalance".equals(ThreadContext.get("taskName"));
        assert"This tasks checks the account balance for 0x3de4a".equals(ThreadContext.get("taskDescription"));

```

When a logger is used inside the task we will have a lot of information that is automatically added to the log
message. The following block gives an idea about the metadata that could be part of such log message:

```
resourceManagerId=0x3d4e
thread=Thread-1
taskName=checkAccountBalance
taskDescription=This tasks checks the account balance for 0xf424a
```

## Adding tracing

The last sample already provides several information that can be used for tracing. Tracing is a common pattern that is
used by microservice environments to track the flow of a transaction or a request over several services. The following
illustration shows an example of that:

![Tracing](./apm-distributed-tracing.png)

The illustration shows a request that is handled by several services. In each service a span is created that is part of
a trace. The trace id is used to identify the complete request. The span id is used to identify the current service.
Next to that a span can have a parent span.

While tracing is mostly used in a microservice environment it can also be used in a monolith. Let's assume we define
start and end timestamps for each task that is executed. By using that information we can easily create a span for that
task. In such concept a span already has a lot of information that we could store in the thread context. The following
illustrations contains a generic overview how such behavior could look like:

![Spans in a monolith](./spans.drawio.png)

When a logger is used inside a span we will again have additional information that is automatically added to the log
message:

```
resourceManagerId=0x3d4e
thread=Thread-1
taskName=checkAccountBalance
taskDescription=This tasks checks the account balance for 0xf424a

spanId=0xdd56e
spanType=checkAccountBalance
spanParentId=0x67f4a
spanStart=2023-06-07T11:50:17.345943
```

Since the spans are bound to the resource handling of the application we can easily create a span for each task that is
executed. By doing so we do not even need to change the code of the resource manager. The manager will automatically
create a span. If the calling thread has already a span we can use that span as a parent span. If not we can create a
span without a parent span. The following code snippet shows how that might look like:

```java
resourceManager.executeTask("executePreHandles","Executes preHandlers for "+transactionId,()->{

        // do something

        resourceManager.executeTask("checkAccountBalance","checks  balance for "+accountId,()->logger.info("Hello World"));

        });

```

In the given scenario the log message would contain information about the current span and can even be linked to the
task that started the inner task since we know the id of the parent span.

## Visualize tracing

Grafana is currently implementing a tracing feature that can be used to visualize traces. Since we already use Grafana
that would be a great fit for us. Like any other service we can provide the functionality directly to the developers to
inspect spans for performance issues. The following illustration shows an example spans that are visualized in Grafana:

![Grafana tracing](./grafana-tracing.png)

To visualize spans we need to provide the span information in a specific format like we do it for our metrics. Open
Telemetry already provides a specification for that. Since Open Telemetry is used more and more in the industry it would
make sense to support that format. Since the tracing in Open Telemetry provides a lot of functionality that we not
need (information about microservices, http requests,..) we could provide a more lightweight api that uses Open
Telemetry internally.

## Measure performance automatically

Thanks to our metrics api we can easily measure the performance of each span and span type. Let's assume that the span
api use the metrics api internally and stores execution times (average, max, min) for each span type. By doing so we
would automatically receive information about the performance of each span type.

## Going down the rabbit hole: Measure performance of locks

While the given approache is often used to measure the performance of services in a microprofile world it provides all
information and functionality to measure much smaller units. Let's assume we want to measure the performance of a lock.
We already have a quite good api to handle locks by using the `AutoLock` and our `Locks` facade. Today creating and
using a
lock to lock a resource looks like this:

```java
AutoLock lock=Locks.createLock();

        try(var ignore=lock.lock()){
        // use locked resource 
        }

```

Let's assume we would add a `String` param to the `createLock()` that is used as the type definition of spans that are
created whenever the lock instance is locked. Since each span stores execution times (average, max, min) in metrics we
could automatically get information about the usage of resources and what resources / locks might need a refactoring to
receive better performance. The following code shows the snippet how that might look like:

```java
AutoLock lock=Locks.createLock("transactionQueue");

        try(var ignore=lock.lock()){
        // use locked resource 
        }

```

One problem with such small spans might be that we create a lot of spans. Based on that the visualization might become
problematic. While the metrics will give a good overview about the performance of each span type we might need concrete
numbers for span instances at runtime.

## Making use of JFR

Since Java 11 the JFR api is part of the JDK. JFR is a great tool to measure performance of Java applications. One
benefit of JFR is the support for custom events: An application can define custom JFR events that will be fired during
execution. Such events can be monitored by any tool that supports JFR like Java Mission Control (JMC) or IntelliJ. The
Profiler of IntelliJ can be used to inspect a running application and collect all the JFR events of that application. By
doing so we can easily collect all the needed JFR events of a running application and analyze them. If the ending of
each span would create a custom JFR event we can use them to analyze the performance of a specific span type in IntelliJ
by looking at all individual events. 