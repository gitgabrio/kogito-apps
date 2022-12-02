JITExecutor-Efesto
==================

Simple project to show the usefulness of Efesto to abstract all the common behavior of engines and expose it through a simple, unified API.

The only task to be executed by the jitexecutor itself is
1) ask efesto for compilation
2) ask efesto for evaluation
The jitexecutor (client code) does not need to have any inner knowledge of what's behind.

There are a couple of modules
1) org.kie.kogito.jitexecutor.efesto.dmn
2) org.kie.kogito.jitexecutor.efesto.pmml

that actually are "efesto-plugins": they are not specific to jitexecutor-efesto and should be moved elsewhere (i.e. drools repository).
Beside, the "pmml" module is an efesto-pmml plugin that uses JPMML implementation, and it is an example of how the kie-jpmml-adapter module should be refactored