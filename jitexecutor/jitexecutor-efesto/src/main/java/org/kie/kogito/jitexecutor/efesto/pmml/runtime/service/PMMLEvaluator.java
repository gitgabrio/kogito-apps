/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.jitexecutor.efesto.pmml.runtime.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.SimpleTypeInfo;
import org.jpmml.evaluator.TargetField;
import org.jpmml.evaluator.TypeInfo;
import org.jpmml.model.visitors.VisitorBattery;
import org.kie.api.pmml.PMML4Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public abstract class PMMLEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PMMLEvaluator.class.getName());

    private PMMLEvaluator() {
    }

    public static PMML4Result evaluateString(String toLoad, final Map<String, ?> inputRecord) throws JAXBException,
            SAXException,
            IOException {
        // Building a model evaluator from a PMML String
        ModelEvaluator<?> evaluator = getEvaluator(toLoad);
        return commonEvaluate(evaluator, inputRecord);
    }

    public static PMML4Result evaluateInputStream(InputStream toLoad, final Map<String, ?> inputRecord) throws JAXBException, SAXException,
            IOException {
        // Building a model evaluator from a PMML InputStream
        ModelEvaluator<?> evaluator = getEvaluator(toLoad);
        return commonEvaluate(evaluator, inputRecord);
    }

    public static PMML4Result evaluateFile(File toLoad, final Map<String, ?> inputRecord) throws JAXBException,
            SAXException,
            IOException {
        // Building a model evaluator from a PMML file
        ModelEvaluator<?> evaluator = getEvaluator(toLoad);
        return commonEvaluate(evaluator, inputRecord);
    }

    private static PMML4Result commonEvaluate(ModelEvaluator<?> evaluator, final Map<String, ?> inputRecord) {
        final PMML4Result toReturn = new PMML4Result();
        // Printing input (x1, x2, .., xn) fields
        List<? extends InputField> inputFields = evaluator.getInputFields();
        LOGGER.debug("Input fields: {}", inputFields);
        // Printing primary result (y) field(s)
        List<? extends TargetField> targetFields = evaluator.getTargetFields();
        LOGGER.debug("Target field(s): {}", targetFields);
        // Printing secondary result (eg. probability(y), decision(y)) fields
        List<OutputField> outputFields = evaluator.getOutputFields();
        LOGGER.debug("Output fields:  {}", outputFields);
        ///
        toReturn.setResultObjectName(evaluator.getTargetField().getFieldName().getValue());
        Map<FieldName, FieldValue> arguments = getArguments(inputFields, inputRecord);
        FieldName itemFieldName = FieldName.create("item");
        FieldValue itemFieldValue = arguments.get(itemFieldName);
        if (itemFieldValue != null) {
            String itemStringValue = itemFieldValue.asString();
            List<Integer> itemListValue = new ArrayList<>(Arrays.asList(itemStringValue.split(" ")))
                    .stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
            TypeInfo typeInfo = new SimpleTypeInfo(DataType.INTEGER, OpType.CONTINUOUS);
            FieldValue transformedItemFieldValue = FieldValue.create(typeInfo, itemListValue);
            arguments.put(itemFieldName, transformedItemFieldValue);
        }
        // Evaluating the model with known-good arguments
        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        results.forEach((BiConsumer<FieldName, Object>) (fieldName, o) -> toReturn.addResultVariable(fieldName.getValue(), o));
        if (toReturn.getResultVariables().containsKey(toReturn.getResultObjectName())) {
            toReturn.setResultCode("OK");
        } else {
            toReturn.setResultCode("FAIL");
        }
        // Making the model evaluator eligible for garbage collection
        evaluator = null;
        return toReturn;
    }

    private static Map<FieldName, FieldValue> getArguments(List<? extends InputField> inputFields,
                                                           Map<String, ?> inputRecord) {
        Map<FieldName, FieldValue> toReturn = new LinkedHashMap<>();
        // Mapping the record field-by-field from data source schema to PMML schema
        for (InputField inputField : inputFields) {
            FieldName inputName = inputField.getName();
            Object rawValue = inputRecord.get(inputName.getValue());
            // Transforming an arbitrary user-supplied value to a known-good PMML value
            FieldValue inputValue = inputField.prepare(rawValue);
            toReturn.put(inputName, inputValue);
        }
        return toReturn;
    }

    private static ModelEvaluator<?> getEvaluator(String toLoad) throws JAXBException, SAXException {
        // Building a model evaluator from a PMML file
        ModelEvaluator<?> toReturn = new LoadingModelEvaluatorBuilder()
                .setLocatable(false)
                .setVisitors(new VisitorBattery())
                //.setOutputFilter(OutputFilters.KEEP_FINAL_RESULTS)
                .load(new ByteArrayInputStream(toLoad.getBytes(StandardCharsets.UTF_8)))
                .build();
        // Perforing the self-check
        toReturn.verify();
        return toReturn;
    }

    private static ModelEvaluator<?> getEvaluator(InputStream toLoad) throws JAXBException, SAXException, IOException {
        // Building a model evaluator from a PMML file
        ModelEvaluator<?> toReturn = new LoadingModelEvaluatorBuilder()
                .setLocatable(false)
                .setVisitors(new VisitorBattery())
                //.setOutputFilter(OutputFilters.KEEP_FINAL_RESULTS)
                .load(toLoad)
                .build();
        // Perforing the self-check
        toReturn.verify();
        return toReturn;
    }

    private static ModelEvaluator<?> getEvaluator(File toLoad) throws JAXBException, SAXException, IOException {
        // Building a model evaluator from a PMML file
        ModelEvaluator<?> toReturn = new LoadingModelEvaluatorBuilder()
                .setLocatable(false)
                .setVisitors(new VisitorBattery())
                //.setOutputFilter(OutputFilters.KEEP_FINAL_RESULTS)
                .load(toLoad)
                .build();
        // Perforing the self-check
        toReturn.verify();
        return toReturn;
    }
}
