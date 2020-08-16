# checkpoint-executor
Allows simple creation of checkpoint'ed jobs using annotations and aspectj.

Simply annotate a method with @CheckPoint(order) annotation. 
AspectJ will weave-into during the compilation phase and create synchronizing code.

See CheckPointTest for example. SyncedJobsProcessor can also be considered a simple example.
