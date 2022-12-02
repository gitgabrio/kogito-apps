/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.jitexecutor.efesto.pmml.compiler.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;
import org.jpmml.model.visitors.VisitorBattery;
import org.xml.sax.SAXException;

public class PMMLValidator {

    private static final Logger logger = Logger.getLogger(PMMLValidator.class.getName());

    private PMMLValidator() {
    }

    public static PMML validateInputStream(InputStream toLoad) throws JAXBException, SAXException {
        // Building a model evaluator from a PMML file
        ModelEvaluator<?> evaluator = new LoadingModelEvaluatorBuilder()
                .setLocatable(false)
                .setVisitors(new VisitorBattery())
                //.setOutputFilter(OutputFilters.KEEP_FINAL_RESULTS)
                .load(toLoad)
                .build();
        return commonValidate(evaluator);
    }

    public static PMML validateFile(File toLoad) throws JAXBException, SAXException, IOException {
        // Building a model evaluator from a PMML file
        ModelEvaluator<?> evaluator = new LoadingModelEvaluatorBuilder()
                .setLocatable(false)
                .setVisitors(new VisitorBattery())
                //.setOutputFilter(OutputFilters.KEEP_FINAL_RESULTS)
                .load(toLoad)
                .build();
        return commonValidate(evaluator);
    }

    public static PMML commonValidate(ModelEvaluator<?> evaluator) {
        // Perforing the self-check
        evaluator.verify();
        // Printing input (x1, x2, .., xn) fields
        List<? extends InputField> inputFields = evaluator.getInputFields();
        logger.info("Input fields: " + inputFields);

        // Printing primary result (y) field(s)
        List<? extends TargetField> targetFields = evaluator.getTargetFields();
        logger.info("Target field(s): " + targetFields);

        // Printing secondary result (eg. probability(y), decision(y)) fields
        List<OutputField> outputFields = evaluator.getOutputFields();
        logger.info("Output fields: " + outputFields);

        // Iterating through columnar data (eg. a CSV file, an SQL result set)
        while (true) {
            // Reading a record from the data source
            Map<String, ?> inputRecord = new HashMap<>();
            if (inputRecord == null) {
                break;
            }

            Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

            // Mapping the record field-by-field from data source schema to PMML schema
            for (InputField inputField : inputFields) {
                FieldName inputName = inputField.getName();

                Object rawValue = inputRecord.get(inputName.getValue());

                // Transforming an arbitrary user-supplied value to a known-good PMML value
                FieldValue inputValue = inputField.prepare(rawValue);

                arguments.put(inputName, inputValue);
            }

            // Evaluating the model with known-good arguments
            Map<FieldName, ?> results = evaluator.evaluate(arguments);

            // Decoupling results from the JPMML-Evaluator runtime environment
            Map<String, ?> resultRecord = EvaluatorUtil.decodeAll(results);

            // Writing a record to the data sink
            //            writeRecord(resultRecord);
            break;
        }

        PMML toReturn = evaluator.getPMML();
        // Making the model evaluator eligible for garbage collection
        evaluator = null;
        return toReturn;
    }
}
