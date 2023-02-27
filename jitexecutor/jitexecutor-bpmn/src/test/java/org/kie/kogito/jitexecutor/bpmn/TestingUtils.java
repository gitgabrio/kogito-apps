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
package org.kie.kogito.jitexecutor.bpmn;

public class TestingUtils {

    public static final String CATCH_NOWHERE = "/catch-nowhere.bpmn";

    public static final String AD_HOC_SUBPROCESS = "/ad-hoc-subprocess.bpmn";
    public static final String LANE_WITH_ANNOTATION = "/lane-with-annotation.bpmn";

    public static final String INVALID_EXPRESSION = "/invalid-expression.bpmn";

    public static final String MISSING_ERROR = "/missing-error.bpmn";

    public static final String MISSING_PROCESS_VARIABLE = "/missing-process-variable.bpmn";

    public static final String MISSING_SIGNAL = "/missing-signal.bpmn";
    public static final String SINGLE_BPMN2_FILE = "/SingleProcess.bpmn2";
    public static final String MULTIPLE_BPMN2_FILE = "/MultipleProcess.bpmn2";
    public static final String SINGLE_INVALID_BPMN2_FILE = "/SingleInvalidModel.bpmn2";

    public static final String SINGLE_UNPARSABLE_BPMN2_FILE = "/SingleUnparsableModel.bpmn2";

    public static final String MULTIPLE_INVALID_BPMN2_FILE = "/MultipleInvalidModel.bpmn2";

    public static final String UNPARSABLE_BPMN2_FILE = "/UnparsableModel.bpmn2";

    public static String getFilePath(String fileName) {
        return "src/test/resources" + fileName;
    }

}
