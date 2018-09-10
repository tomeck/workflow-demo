# workflow-demo
Demo containing multiple headless and REST components illustrating how to use teck's WorkflowManager w/RabbitMQ

## Prerequisites
Install Docker image of RabbitMQ

Create a 'topic' exchange named 'wf-demo-topic'

Create a queue 'q1' bound to exchange 'wf-demo' with routing key 'q1'

Create a queue 'q2' bound to exchange 'wf-demo' with routing key 'q2'

## Message flow

`componentA --> [wf-demo/wf-demo.requests] --> componentB --> [wf-demo/wf-demo.processed] --> componentC
 --> [wf-demo/wf-demo.requests] --> componentB --> [wf-demo/wf-demo.processed] --> componentC --> [reply-to queue for componentA] --> componentA`
 
The new WorkflowManager facilitates driving the flow of queue messages, without a central bus or active management server.  ComponentA.java contains the workflow descriptor that governs this flow.  You can also look at restComponent for a Spring Boot-based REST controller that initiates a workflow, or nodeServer that shows how to do it from Node.js

Here is the representation of the flow above (found in ComponentA.java):

`{ 'remainWkflw':[ {'Name':'A', 'NextAddr':'requests'}, {'Name':'B', 'NextAddr':'processed'},{'Name':'C', 'NextAddr':'requests'}, {'Name':'D', 'NextAddr':'processed'}, {'Name':'C', 'NextAddr':'reply-to'} ]}`

## Build steps
Build Workflow Manager first (note that also installs the jar into your local Maven repo)

```$ mvn clean install -f workflow-lib/pom.xml```

Build the processing components

```$ mvn clean package -DskipTests -f componentA/pom.xml```

*Build the other components similarly: ComponentB, ComponentC, restComponent*


Start the 'processing' components: 

```$ java -jar componentB/target/componentB-0.0.1-SNAPSHOT.jar```

```$ java -jar componentC/target/componentC-0.0.1-SNAPSHOT.jar```


Now start the 'sourcing' component, ComponentA, which will immediately start placing queue messages on the input queue
and Components B and C will process them and return the result back to ComponentA

```$ java -jar componentA/target/componentA-0.0.1-SNAPSHOT.jar```


Start the Rest Controller which also acts as a source .There is a Jmeter script incuded in the restComponent folder that shows the HTTP POST
that will trigger the rest controller.


```$ java -jar restComponent/target/restComponent-0.0.1-SNAPSHOT.jar```


There is also a sample Node.js server that uses a customized amqplib-rpc to execute the WorkflowManager concept

*The ```npm link``` steps are required only b/c the updated amqplib-rpc has not been registered to npm central. It allows the local version to be linked to the NodeServer*


**MAKE SURE componentB and componentC are running as per above**

```
$ cd amqplib-rpc
$ npm install
$ sudo npm link
$ cd ../nodeServer
# npm link amqplib-rpc
$ npm install
$ node server.js
$ curl localhost:3000  (>>Hello from node | Hello from B.....)
```
