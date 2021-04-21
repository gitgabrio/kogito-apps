/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

export interface TaskDetailsEnvelopeApi {
  taskDetails__init(
    association: Association,
    initArgs: TaskDetailsInitArgs
  ): Promise<void>;
}

export interface Association {
  origin: string;
  envelopeServerId: string;
}

export interface TaskDetailsInitArgs {
  task: UserTaskInstance;
}

export interface UserTaskInstance {
  id: string;
  description?: string;
  name?: string;
  priority?: string;
  processInstanceId: string;
  processId: string;
  rootProcessInstanceId?: string;
  rootProcessId?: string;
  state: string;
  actualOwner?: string;
  adminGroups?: string[];
  adminUsers?: string[];
  completed?: Date;
  started: Date;
  excludedUsers?: string[];
  potentialGroups?: string[];
  potentialUsers?: string[];
  inputs?: string;
  outputs?: string;
  referenceName?: string;
  lastUpdate: Date;
  endpoint?: string;
}