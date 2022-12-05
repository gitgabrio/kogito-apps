JITExecutor-Efesto
==================

Simple project to show the usefulness of Efesto to abstract all the common behavior of engines and expose it through a
simple, unified API.

The idea behind Efesto is that
1) all the engines express the same behavior (validation, compilation, execution)
2) all the engines have the same needs (persistence, interaction with other models/engines/, exposition)
3) all the engines must be consumed by some "client" code.

Efesto is meant to provide a common API that hides engine-specific details from the client-code, leaving to engine-developers the freedom to implement the required functionalities as they prefer, leveraging common utilities/functionalities provided by framework (e.g. engine interaction). At the same time, client-code developers would be relieved from the burden of dealing with server-side implementations. At the same time, the two parts (server-side and client-side) would be decoupled.
Efesto makes a clear separation between compile-time and execution-time. It also features the microkernel-style design, so for each specific model there could be one, or more, implementations of both compiler and/or executor,
each of which specific for a given tupla (i.e. kind of model, kind of input, etc).


Please refer [here](https://docs.google.com/document/d/1n9rKcMh0qnP7R4DUb3xqanFZcN0q7SL8aBRoAdQDSH0/) for more detailed information about Efesto itself.

The file `JITEXECUTOR -Efesto Demo.postman_collection.json` contains a collection of Postman requests ready to be used.




Use case
========

JitExecutor exposes engine functionalities through some Rest endpoints.
Such functionalities are:
1) validation
2) compilation
3) execution

A specific requirement is that engines should be able to execute on-the-fly validation/compilation, since the JitExecutor is meant as backend for model tooling.
The jitexecutor is a "client code" for the engines, and does not need to have any inner knowledge of implementation details.


The only tasks to be executed by the jitexecutor itself are

1) ask efesto for validation
2) ask efesto for compilation
2) ask efesto for evaluation
   

Efesto plugins
--------------

There are a couple of modules

1) org.kie.kogito.jitexecutor.efesto.dmn
2) org.kie.kogito.jitexecutor.efesto.pmml

that actually are "efesto-plugins": they are not specific to jitexecutor-efesto and should be moved elsewhere (i.e.
drools repository).
Those plugins exposes the required functionalities mapped to the kind of input used in the jitexecutor.

Beside, the "pmml" module is an efesto-pmml plugin that uses JPMML implementation, and it is an example of how the
kie-jpmml-adapter module should be refactored. It has been implemented because jitexecutor must be able to create a native-image, so it could work only with "statically-compiled" engines (i.e. engines that does not rely upon code-generation).


Caveat
======

1) This branch depends on https://github.com/gitgabrio/drools/tree/CHANGE_KieRuntimeServicePMMLMapInput_INPUT

2) Currently, DMN-PMML invocation is broken (it works on tests "by chance", because DMN looks for imported models in the classloader).
A workaround will be put in place, but the need for that is another example of the kind of problem that Efesto is meant to solve.
Ideally, client-code developer should not be required to solve such low-level problems like engine-interaction.
At the same time, all the engines should rely on common API, provided by the framework, to implement the same behavior.


Execution
=========

issue `mvn quarkus:dev` on `jitexecutor-efesto`directory.



Validation with curl
====================

```bash
curl -X 'POST' \
'http://localhost:8080/jitefesto/validate' \
-H 'accept: */*' \
-H 'Content-Type: application/json' \
-d '{
"mainURI": {
"fullPath": "string",
"fileName": "string",
"modelName": "string"
},
"resources": [
{
"URI": {
"fullPath": "string",
"fileName": "string",
"modelName": "string"
},
"content": "string"
}
]
}'
```

Evaluation with curl
====================

```bash
curl -X 'POST' \
'http://localhost:8080/jitefesto/evaluate' \
-H 'accept: */*' \
-H 'Content-Type: application/json' \
-d '{
"modelsPayload": {
"mainURI": {
"fullPath": "string",
"fileName": "string",
"modelName": "string"
},
"resources": [
{
"URI": {
"fullPath": "string",
"fileName": "string",
"modelName": "string"
},
"content": "string"
}
]
},
"inputData": {
"additionalProp1": {},
"additionalProp2": {},
"additionalProp3": {}
}
}'
```


Testing SWAGGER-UI
=================

1. issue `mvn quarkus:dev`
2. open page `http://localhost:8080/q/swagger-ui/#`
3. insert message in the "Request body"


Validation Messages/content
===========================

Valid DMN
---------

```json
{
  "mainURI": {
    "fullPath": "/dmn/test.dmn",
    "fileName": "test.dmn",
    "modelName": ""
  },
  "resources": [
    {
      "URI": {
        "fullPath": "/dmn/test.dmn",
        "fileName": "test.dmn",
        "modelName": ""
      },
      "content": "<?xml version=\"1.0\" ?>\n<dmn:definitions xmlns:dmn=\"http://www.omg.org/spec/DMN/20180521/MODEL/\" xmlns=\"xls2dmn_741b355c-685c-4827-b13a-833da8321da4\" xmlns:di=\"http://www.omg.org/spec/DMN/20180521/DI/\" xmlns:feel=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" xmlns:dmndi=\"http://www.omg.org/spec/DMN/20180521/DMNDI/\" xmlns:dc=\"http://www.omg.org/spec/DMN/20180521/DC/\" name=\"xls2dmn\" expressionLanguage=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" typeLanguage=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" namespace=\"xls2dmn_741b355c-685c-4827-b13a-833da8321da4\" exporter=\"kie-dmn-xls2dmn\">\n  <dmn:inputData id=\"id_FICO_32Score\" name=\"FICO Score\">\n    <dmn:variable id=\"idvar_FICO_32Score\" name=\"FICO Score\" typeRef=\"number\"></dmn:variable>\n  </dmn:inputData>\n  <dmn:inputData id=\"id_DTI_32Ratio\" name=\"DTI Ratio\">\n    <dmn:variable id=\"idvar_DTI_32Ratio\" name=\"DTI Ratio\" typeRef=\"number\"></dmn:variable>\n  </dmn:inputData>\n  <dmn:inputData id=\"id_PITI_32Ratio\" name=\"PITI Ratio\">\n    <dmn:variable id=\"idvar_PITI_32Ratio\" name=\"PITI Ratio\" typeRef=\"number\"></dmn:variable>\n  </dmn:inputData>\n  <dmn:decision id=\"d_Loan_32Approval\" name=\"Loan Approval\">\n    <dmn:variable id=\"dvar_Loan_32Approval\" name=\"Loan Approval\"></dmn:variable>\n    <dmn:informationRequirement>\n      <dmn:requiredInput href=\"#id_FICO_32Score\"></dmn:requiredInput>\n    </dmn:informationRequirement>\n    <dmn:informationRequirement>\n      <dmn:requiredDecision href=\"#d_DTI_32Rating\"></dmn:requiredDecision>\n    </dmn:informationRequirement>\n    <dmn:informationRequirement>\n      <dmn:requiredDecision href=\"#d_PITI_32Rating\"></dmn:requiredDecision>\n    </dmn:informationRequirement>\n    <dmn:decisionTable id=\"ddt_Loan_32Approval\" hitPolicy=\"ANY\" preferredOrientation=\"Rule-as-Row\" outputLabel=\"Loan Approval\">\n      <dmn:input label=\"FICO Score\">\n        <dmn:inputExpression>\n          <dmn:text>FICO Score</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:input label=\"DTI Rating\">\n        <dmn:inputExpression>\n          <dmn:text>DTI Rating</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:input label=\"PITI Rating\">\n        <dmn:inputExpression>\n          <dmn:text>PITI Rating</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:output></dmn:output>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&lt;=750</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Not approved\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>\"Bad\"</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Not approved\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>\"Bad\"</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Not approved\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&gt;750</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>\"Good\"</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>\"Good\"</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Approved\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n    </dmn:decisionTable>\n  </dmn:decision>\n  <dmn:decision id=\"d_DTI_32Rating\" name=\"DTI Rating\">\n    <dmn:variable id=\"dvar_DTI_32Rating\" name=\"DTI Rating\"></dmn:variable>\n    <dmn:informationRequirement>\n      <dmn:requiredInput href=\"#id_DTI_32Ratio\"></dmn:requiredInput>\n    </dmn:informationRequirement>\n    <dmn:decisionTable id=\"ddt_DTI_32Rating\" hitPolicy=\"ANY\" preferredOrientation=\"Rule-as-Row\" outputLabel=\"DTI Rating\">\n      <dmn:input label=\"DTI Ratio\">\n        <dmn:inputExpression>\n          <dmn:text>DTI Ratio</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:output></dmn:output>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&lt;=0.20</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Good\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&gt;0.20</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Bad\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n    </dmn:decisionTable>\n  </dmn:decision>\n  <dmn:decision id=\"d_PITI_32Rating\" name=\"PITI Rating\">\n    <dmn:variable id=\"dvar_PITI_32Rating\" name=\"PITI Rating\"></dmn:variable>\n    <dmn:informationRequirement>\n      <dmn:requiredInput href=\"#id_PITI_32Ratio\"></dmn:requiredInput>\n    </dmn:informationRequirement>\n    <dmn:decisionTable id=\"ddt_PITI_32Rating\" hitPolicy=\"ANY\" preferredOrientation=\"Rule-as-Row\" outputLabel=\"PITI Rating\">\n      <dmn:input label=\"PITI Ratio\">\n        <dmn:inputExpression>\n          <dmn:text>PITI Ratio</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:output></dmn:output>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&lt;=0.28</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Good\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&gt;0.28</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Bad\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n    </dmn:decisionTable>\n  </dmn:decision>\n</dmn:definitions>\n"
    }
  ]
}
```

Answer

```json
{
  "validations": [
    {
      "modelIdentifier": "test.dmn",
      "status": "OK",
      "messages": [
        "WARNING:DMN: Variable named 'Loan Approval' is missing its type reference on node 'Loan Approval' (DMN id: dvar_Loan_32Approval, Type ref not defined) ",
        "WARNING:DMN: Variable named 'DTI Rating' is missing its type reference on node 'DTI Rating' (DMN id: dvar_DTI_32Rating, Type ref not defined) ",
        "WARNING:DMN: Variable named 'PITI Rating' is missing its type reference on node 'PITI Rating' (DMN id: dvar_PITI_32Rating, Type ref not defined) ",
        "WARNING:DMN: Skipped Decision Table Analysis of table 'Loan Approval' because: null (DMN id: ddt_Loan_32Approval, DMN Validation, Decision Table Analysis) ",
        "WARNING:DMN: Skipped Decision Table Analysis of table 'DTI Rating' because: null (DMN id: ddt_DTI_32Rating, DMN Validation, Decision Table Analysis) ",
        "WARNING:DMN: Skipped Decision Table Analysis of table 'PITI Rating' because: null (DMN id: ddt_PITI_32Rating, DMN Validation, Decision Table Analysis) "
      ]
    }
  ]
}
```

Invalid DMN
-----------

```json
{
  "mainURI": {
    "fullPath": "/dmn/test.dmn",
    "fileName": "test.dmn",
    "modelName": ""
  },
  "resources": [
    {
      "URI": {
        "fullPath": "/dmn/test.dmn",
        "fileName": "test.dmn",
        "modelName": ""
      },
      "content": "<dmn:definitions xmlns:dmn=\"http://www.omg.org/spec/DMN/20180521/MODEL/\" xmlns=\"https://kiegroup.org/dmn/_730A7A75-F473-4083-93B9-85E0DAF7F4BD\" xmlns:feel=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" xmlns:kie=\"http://www.drools.org/kie/dmn/1.2\" xmlns:dmndi=\"http://www.omg.org/spec/DMN/20180521/DMNDI/\" xmlns:di=\"http://www.omg.org/spec/DMN/20180521/DI/\" xmlns:dc=\"http://www.omg.org/spec/DMN/20180521/DC/\" id=\"_B0E7B3DA-0BD8-4BF4-9A45-24B1671709E8\" name=\"dupContextEntryKey\" typeLanguage=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" namespace=\"https://kiegroup.org/dmn/_730A7A75-F473-4083-93B9-85E0DAF7F4BD\">\n  <dmn:extensionElements/>\n  <dmn:decision id=\"_E571194A-0563-44BF-8F50-CC40283416D1\" name=\"hardcoded\">\n    <dmn:extensionElements/>\n    <dmn:variable id=\"_8313C0ED-508A-480B-B85B-71B2245E42B3\" name=\"hardcoded\" typeRef=\"Any\"/>\n    <dmn:context id=\"_F67C748B-D473-4EFE-AC0C-239DC77FB6F4\">\n      <dmn:contextEntry>\n        <dmn:variable id=\"_95689CB6-FF78-4A2B-9331-CAF52ED30F30\" name=\"a\" typeRef=\"Any\"/>\n        <dmn:literalExpression id=\"_07DEEF4C-B917-4FA1-ACE9-9CE425646A6E\">\n          <dmn:text>1</dmn:text>\n        </dmn:literalExpression>\n      </dmn:contextEntry>\n      <dmn:contextEntry>\n        <dmn:variable id=\"_FC66F79A-080D-482B-8370-8D1743F583C9\" name=\"b\" typeRef=\"Any\"/>\n        <dmn:literalExpression id=\"_D8453BD2-19AE-4DD1-B41A-35398CAA433B\">\n          <dmn:text>2</dmn:text>\n        </dmn:literalExpression>\n      </dmn:contextEntry>\n      <dmn:contextEntry>\n        <dmn:variable id=\"_85EA2479-11BC-4B27-9EBE-CA2BA4DA889C\" name=\"a\" typeRef=\"Any\"/>\n        <dmn:literalExpression id=\"_E521715E-7386-42E3-8577-CEBD7DC2A86E\">\n          <dmn:text>3</dmn:text>\n        </dmn:literalExpression>\n      </dmn:contextEntry>\n    </dmn:context>\n  </dmn:decision>\n</dmn:definitions>"
    }
  ]
}
```

Answer

```json
{
  "validations": [
    {
      "modelIdentifier": "test.dmn",
      "status": "FAIL",
      "messages": [
        "ERROR:DMN: Duplicate context entry with variables named 'a' (The referenced name is not unique with its scope) ",
        "ERROR:DMN: Duplicate context entry with variables named 'a' (The referenced name is not unique with its scope) "
      ]
    }
  ]
}
```

Valid PMML
----------

```json
{
  "mainURI": {
    "fullPath": "/pmml/test_regression.pmml",
    "fileName": "test_regression.pmml",
    "modelName": "LinReg"
  },
  "resources": [
    {
      "URI": {
        "fullPath": "/pmml/test_regression.pmml",
        "fileName": "test_regression.pmml",
        "modelName": "LinReg"
      },
      "content": "<PMML version=\"4.2\" xsi:schemaLocation=\"http://www.dmg.org/PMML-4_2 http://www.dmg.org/v4-2-1/pmml-4-2.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.dmg.org/PMML-4_2\">\n  <Header copyright=\"JBoss\"/>\n  <DataDictionary numberOfFields=\"5\">\n    <DataField dataType=\"double\" name=\"fld1\" optype=\"continuous\"/>\n    <DataField dataType=\"double\" name=\"fld2\" optype=\"continuous\"/>\n    <DataField dataType=\"string\" name=\"fld3\" optype=\"categorical\">\n      <Value value=\"x\"/>\n      <Value value=\"y\"/>\n    </DataField>\n    <DataField dataType=\"double\" name=\"fld4\" optype=\"continuous\"/>\n    <DataField dataType=\"double\" name=\"fld5\" optype=\"continuous\"/>\n  </DataDictionary>\n  <RegressionModel algorithmName=\"linearRegression\" functionName=\"regression\" modelName=\"LinReg\" targetFieldName=\"fld4\">\n    <MiningSchema>\n      <MiningField name=\"fld1\"/>\n      <MiningField name=\"fld2\"/>\n      <MiningField name=\"fld3\"/>\n      <MiningField name=\"fld4\" usageType=\"predicted\"/>\n    </MiningSchema>\n    <Output>\n      <OutputField name=\"result\" targetField=\"fld4\" />\n    </Output>\n    <RegressionTable intercept=\"0.5\">\n      <NumericPredictor coefficient=\"5\" exponent=\"2\" name=\"fld1\"/>\n      <NumericPredictor coefficient=\"2\" exponent=\"1\" name=\"fld2\"/>\n      <CategoricalPredictor coefficient=\"-3\" name=\"fld3\" value=\"x\"/>\n      <CategoricalPredictor coefficient=\"3\" name=\"fld3\" value=\"y\"/>\n    </RegressionTable>\n  </RegressionModel>\n</PMML>"
    }
  ]
}
```

Answer

```json
{
    "validations": [
        {
            "modelIdentifier": "test_regression.pmml",
            "status": "OK",
            "messages": []
        }
    ]
}
```

Invalid PMML
------------

```json
{
"mainURI": {
"fullPath": "/pmml/test_invalid.pmml",
"fileName": "test_invalid.pmml",
"modelName": "LinReg"
},
"resources": [
{
"URI": {
"fullPath": "/pmml/test_invalid.pmml",
"fileName": "test_invalid.pmml",
"modelName": "LinReg"
},
"content": "<PMML version=\"4.2\" xsi:schemaLocation=\"http://www.dmg.org/PMML-4_2 http://www.dmg.org/v4-2-1/pmml-4-2.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.dmg.org/PMML-4_2\">\n  <Header copyright=\"JBoss\"/>\n  <DataDictionary numberOfFields=\"5\">\n    <DataField dataType=\"double\" name=\"fld1\" optype=\"continuous\"/>\n    <DataField dataType=\"double\" name=\"fld2\" optype=\"continuous\"/>\n    <DataField dataType=\"string\" name=\"fld3\" optype=\"categorical\">\n      <Value value=\"x\"/>\n      <Value value=\"y\"/>\n    </DataField>\n    <DataField dataType=\"double\" name=\"fld4\" optype=\"continuous\"/>\n    <DataField dataType=\"double\" name=\"fld5\" optype=\"continuous\"/>\n  </DataDictionary>\n  <RegressionModel algorithmName=\"linearRegression\" functionName=\"regression\" modelName=\"LinReg\" targetFieldName=\"fld4\">\n    <MiningSchema>\n      <MiningField name=\"fld5\"/>\n      <MiningField name=\"fld2\"/>\n      <MiningField name=\"fld7\"/>\n      <MiningField name=\"fld4\" usageType=\"predicted\"/>\n    </MiningSchema>\n    <Output>\n      <OutputField name=\"result\" targetField=\"fld4\" />\n    </Output>\n    <RegressionTable intercept=\"0.5\">\n      <NumericPredictor coefficient=\"5\" exponent=\"2\" name=\"fld1\"/>\n      <NumericPredictor coefficient=\"2\" exponent=\"1\" name=\"fld2\"/>\n      <CategoricalPredictor coefficient=\"-3\" name=\"fld3\" value=\"x\"/>\n      <CategoricalPredictor coefficient=\"3\" name=\"fld3\" value=\"y\"/>\n    </RegressionTable>\n  </RegressionModel>\n</PMML>"
}
]
}
```

Answer

```json
{
    "validations": [
        {
            "modelIdentifier": "test_invalid.pmml",
            "status": "FAIL",
            "messages": [
                "Field \"fld7\" is not defined",
                "Field \"fld1\" is not defined"
            ]
        }
    ]
}
```

Evaluation Messages/content
===========================

DMN
---

```json
{
  "modelsPayload": {
    "mainURI": {
      "fullPath": "/dmn/test.dmn",
      "fileName": "test.dmn",
      "modelName": ""
    },
    "resources": [
      {
        "URI": {
          "fullPath": "/dmn/test.dmn",
          "fileName": "test.dmn",
          "modelName": ""
        },
        "content": "<?xml version=\"1.0\" ?>\n<dmn:definitions xmlns:dmn=\"http://www.omg.org/spec/DMN/20180521/MODEL/\" xmlns=\"xls2dmn_741b355c-685c-4827-b13a-833da8321da4\" xmlns:di=\"http://www.omg.org/spec/DMN/20180521/DI/\" xmlns:feel=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" xmlns:dmndi=\"http://www.omg.org/spec/DMN/20180521/DMNDI/\" xmlns:dc=\"http://www.omg.org/spec/DMN/20180521/DC/\" name=\"xls2dmn\" expressionLanguage=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" typeLanguage=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" namespace=\"xls2dmn_741b355c-685c-4827-b13a-833da8321da4\" exporter=\"kie-dmn-xls2dmn\">\n  <dmn:inputData id=\"id_FICO_32Score\" name=\"FICO Score\">\n    <dmn:variable id=\"idvar_FICO_32Score\" name=\"FICO Score\" typeRef=\"number\"></dmn:variable>\n  </dmn:inputData>\n  <dmn:inputData id=\"id_DTI_32Ratio\" name=\"DTI Ratio\">\n    <dmn:variable id=\"idvar_DTI_32Ratio\" name=\"DTI Ratio\" typeRef=\"number\"></dmn:variable>\n  </dmn:inputData>\n  <dmn:inputData id=\"id_PITI_32Ratio\" name=\"PITI Ratio\">\n    <dmn:variable id=\"idvar_PITI_32Ratio\" name=\"PITI Ratio\" typeRef=\"number\"></dmn:variable>\n  </dmn:inputData>\n  <dmn:decision id=\"d_Loan_32Approval\" name=\"Loan Approval\">\n    <dmn:variable id=\"dvar_Loan_32Approval\" name=\"Loan Approval\"></dmn:variable>\n    <dmn:informationRequirement>\n      <dmn:requiredInput href=\"#id_FICO_32Score\"></dmn:requiredInput>\n    </dmn:informationRequirement>\n    <dmn:informationRequirement>\n      <dmn:requiredDecision href=\"#d_DTI_32Rating\"></dmn:requiredDecision>\n    </dmn:informationRequirement>\n    <dmn:informationRequirement>\n      <dmn:requiredDecision href=\"#d_PITI_32Rating\"></dmn:requiredDecision>\n    </dmn:informationRequirement>\n    <dmn:decisionTable id=\"ddt_Loan_32Approval\" hitPolicy=\"ANY\" preferredOrientation=\"Rule-as-Row\" outputLabel=\"Loan Approval\">\n      <dmn:input label=\"FICO Score\">\n        <dmn:inputExpression>\n          <dmn:text>FICO Score</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:input label=\"DTI Rating\">\n        <dmn:inputExpression>\n          <dmn:text>DTI Rating</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:input label=\"PITI Rating\">\n        <dmn:inputExpression>\n          <dmn:text>PITI Rating</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:output></dmn:output>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&lt;=750</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Not approved\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>\"Bad\"</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Not approved\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>-</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>\"Bad\"</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Not approved\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&gt;750</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>\"Good\"</dmn:text>\n        </dmn:inputEntry>\n        <dmn:inputEntry>\n          <dmn:text>\"Good\"</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Approved\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n    </dmn:decisionTable>\n  </dmn:decision>\n  <dmn:decision id=\"d_DTI_32Rating\" name=\"DTI Rating\">\n    <dmn:variable id=\"dvar_DTI_32Rating\" name=\"DTI Rating\"></dmn:variable>\n    <dmn:informationRequirement>\n      <dmn:requiredInput href=\"#id_DTI_32Ratio\"></dmn:requiredInput>\n    </dmn:informationRequirement>\n    <dmn:decisionTable id=\"ddt_DTI_32Rating\" hitPolicy=\"ANY\" preferredOrientation=\"Rule-as-Row\" outputLabel=\"DTI Rating\">\n      <dmn:input label=\"DTI Ratio\">\n        <dmn:inputExpression>\n          <dmn:text>DTI Ratio</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:output></dmn:output>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&lt;=0.20</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Good\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&gt;0.20</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Bad\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n    </dmn:decisionTable>\n  </dmn:decision>\n  <dmn:decision id=\"d_PITI_32Rating\" name=\"PITI Rating\">\n    <dmn:variable id=\"dvar_PITI_32Rating\" name=\"PITI Rating\"></dmn:variable>\n    <dmn:informationRequirement>\n      <dmn:requiredInput href=\"#id_PITI_32Ratio\"></dmn:requiredInput>\n    </dmn:informationRequirement>\n    <dmn:decisionTable id=\"ddt_PITI_32Rating\" hitPolicy=\"ANY\" preferredOrientation=\"Rule-as-Row\" outputLabel=\"PITI Rating\">\n      <dmn:input label=\"PITI Ratio\">\n        <dmn:inputExpression>\n          <dmn:text>PITI Ratio</dmn:text>\n        </dmn:inputExpression>\n      </dmn:input>\n      <dmn:output></dmn:output>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&lt;=0.28</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Good\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n      <dmn:rule>\n        <dmn:inputEntry>\n          <dmn:text>&gt;0.28</dmn:text>\n        </dmn:inputEntry>\n        <dmn:outputEntry>\n          <dmn:text>\"Bad\"</dmn:text>\n        </dmn:outputEntry>\n      </dmn:rule>\n    </dmn:decisionTable>\n  </dmn:decision>\n</dmn:definitions>\n"
      }
    ]
  },
  "inputData": {
    "FICO Score": 800,
    "DTI Ratio": 0.1,
    "PITI Ratio": 0.1
  }
}
```

Answer

```json
{
  "/dmn/test.dmn": {
    "messages": [],
    "decisionResults": [
      {
        "decisionId": "d_Loan_32Approval",
        "decisionName": "Loan Approval",
        "result": "Approved",
        "messages": [],
        "evaluationStatus": "SUCCEEDED"
      },
      {
        "decisionId": "d_DTI_32Rating",
        "decisionName": "DTI Rating",
        "result": "Good",
        "messages": [],
        "evaluationStatus": "SUCCEEDED"
      },
      {
        "decisionId": "d_PITI_32Rating",
        "decisionName": "PITI Rating",
        "result": "Good",
        "messages": [],
        "evaluationStatus": "SUCCEEDED"
      }
    ]
  }
}
```

PMML
----

```json
{
  "modelsPayload":{
  "mainURI": {
    "fullPath": "/pmml/test_regression.pmml",
    "fileName": "test_regression.pmml",
    "modelName": "LinReg"
  },
  "resources": [
    {
      "URI": {
        "fullPath": "/pmml/test_regression.pmml",
        "fileName": "test_regression.pmml",
        "modelName": "LinReg"
      },
      "content": "<PMML version=\"4.2\" xsi:schemaLocation=\"http://www.dmg.org/PMML-4_2 http://www.dmg.org/v4-2-1/pmml-4-2.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.dmg.org/PMML-4_2\">\n  <Header copyright=\"JBoss\"/>\n  <DataDictionary numberOfFields=\"5\">\n    <DataField dataType=\"double\" name=\"fld1\" optype=\"continuous\"/>\n    <DataField dataType=\"double\" name=\"fld2\" optype=\"continuous\"/>\n    <DataField dataType=\"string\" name=\"fld3\" optype=\"categorical\">\n      <Value value=\"x\"/>\n      <Value value=\"y\"/>\n    </DataField>\n    <DataField dataType=\"double\" name=\"fld4\" optype=\"continuous\"/>\n    <DataField dataType=\"double\" name=\"fld5\" optype=\"continuous\"/>\n  </DataDictionary>\n  <RegressionModel algorithmName=\"linearRegression\" functionName=\"regression\" modelName=\"LinReg\" targetFieldName=\"fld4\">\n    <MiningSchema>\n      <MiningField name=\"fld1\"/>\n      <MiningField name=\"fld2\"/>\n      <MiningField name=\"fld3\"/>\n      <MiningField name=\"fld4\" usageType=\"predicted\"/>\n    </MiningSchema>\n    <Output>\n      <OutputField name=\"result\" targetField=\"fld4\" />\n    </Output>\n    <RegressionTable intercept=\"0.5\">\n      <NumericPredictor coefficient=\"5\" exponent=\"2\" name=\"fld1\"/>\n      <NumericPredictor coefficient=\"2\" exponent=\"1\" name=\"fld2\"/>\n      <CategoricalPredictor coefficient=\"-3\" name=\"fld3\" value=\"x\"/>\n      <CategoricalPredictor coefficient=\"3\" name=\"fld3\" value=\"y\"/>\n    </RegressionTable>\n  </RegressionModel>\n</PMML>"
    }
  ]
},
  "inputData": {
    "fld1": 3.0,
    "fld2": 2.0,
    "fld3": "y"
  }
}
```

Answer

```json
{
  "/pmml/test_regression.pmml": {
    "correlationId": null,
    "segmentationId": null,
    "segmentId": null,
    "segmentIndex": 0,
    "resultCode": "OK",
    "resultObjectName": "fld4",
    "resultVariables": {
      "result": 52.5,
      "fld4": 52.5
    }
  }
}
```