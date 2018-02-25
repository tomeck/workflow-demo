# workflow-demo
Demo containing 4 components illustrating how to use WorkflowManager w/RabbitMQ

## Prerequisites
Install Docker image of RabbitMQ

## Message flow

componentA --> wf-demo/wf-demo.requests --> componentB --> wf-demo/wf-demo.processed --> componentC
 --> wf-demo/wf-demo.requests --> componentB --> wf-demo/wf-demo.processed --> componentC --> [reply-to queue for componentA] --> componentA
 
ComponentA.java contains the workflow descriptor that governs this flow.  The WorkflowManager facilitates driving the flow of queue messages, without a central bus or active management server

Here is the representation of the flow above (found in ComponentA.java):
"{ \"remainWkflw\":[ {\"Name\":\"A\", \"NextAddr\":\"requests\"}, {\"Name\":\"B\", \"NextAddr\":\"processed\"},{\"Name\":\"C\", \"NextAddr\":\"requests\"}, {\"Name\":\"D\", \"NextAddr\":\"processed\"}, {\"Name\":\"C\", \"NextAddr\":\"reply-to\"} ]}"

## Build steps
Build Workflow Manager first
$ mvn clean package -f workflow-lib/pom.xml

Build the other components similarly: ComponentA, ComponentB, ComponentC, restComponent

Start the 'processing' components: 
$ java -jar componentA/target/componentB-0.0.1-SNAPSHOT.jar
$ java -jar componentB/target/componentC-0.0.1-SNAPSHOT.jar

Start the 'sourcing' component, ComponentA, which will immediately start placing queue messages on the input queue
and Components B and C will process them and return the result back to ComponentA
$ java -jar componentC/target/componentA-0.0.1-SNAPSHOT.jar

Start the Rest Controller which also acts as a source
There is a Jmeter script incuded in the restComponent folder that shows the HTTP POST
that will trigger the rest controller
$ java -jar restComponent/target/restComponent-0.0.1-SNAPSHOT.jar

