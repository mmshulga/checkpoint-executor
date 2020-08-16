# checkpoint-executor
Allows simple creation of checkpoint'ed jobs using annotations and aspectj.

Simply annotate a method with @CheckPoint(order) annotation. Order starts with 0. 
AspectJ will weave-into during the compilation phase and create synchronization code.

See CheckPointTest for example.
