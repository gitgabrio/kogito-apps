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

import * as React from 'react';
import { isEmpty } from 'lodash';
import Moment from 'react-moment';
import { Form, FormGroup, Text, TextVariants } from '@patternfly/react-core';
import { KogitoSpinner } from '@kogito-apps/components-common';
import { componentOuiaProps, OUIAProps } from '@kogito-apps/ouia-tools';
import { TaskState, UserTaskInstance } from '@kogito-apps/task-console-shared';

interface TaskDetailsProps {
  userTask: UserTaskInstance;
}

const TaskDetails: React.FC<TaskDetailsProps & OUIAProps> = ({
  userTask,
  ouiaId,
  ouiaSafe
}) => {
  if (!userTask) {
    return (
      <KogitoSpinner
        {...componentOuiaProps(
          ouiaId,
          'task-details-component-loading',
          ouiaSafe
        )}
        spinnerText={'Loading task details'}
      />
    );
  }

  const resolveTaskPriority = (priority?: string): string => {
    switch (priority) {
      case '0':
        return '0 - High';
      case '5':
        return '5 - Medium';
      case '10':
        return '10 - Low';
    }

    return priority || '-';
  };

  return (
    <Form {...componentOuiaProps(ouiaId, 'task-details-component', ouiaSafe)}>
      <FormGroup label="Name" fieldId="name">
        <Text component={TextVariants.p}>{userTask.referenceName}</Text>
      </FormGroup>
      {userTask.description && (
        <FormGroup label="Description" fieldId="description">
          <Text component={TextVariants.p}>{userTask.description}</Text>
        </FormGroup>
      )}
      <FormGroup label="ID" fieldId="id">
        <Text component={TextVariants.p}>{userTask.id}</Text>
      </FormGroup>
      <FormGroup label="State" fieldId="state">
        <TaskState task={userTask} />
      </FormGroup>
      <FormGroup label="Priority" fieldId="priority">
        <Text component={TextVariants.p}>
          {resolveTaskPriority(userTask.priority)}
        </Text>
      </FormGroup>
      <FormGroup label="Owner" fieldId="owner">
        <Text component={TextVariants.p}>{userTask.actualOwner || '-'}</Text>
      </FormGroup>
      {!isEmpty(userTask.potentialUsers) && (
        <FormGroup label="Potential users" fieldId="potential_users">
          <Text component={TextVariants.p}>
            {userTask.potentialUsers.join(', ')}
          </Text>
        </FormGroup>
      )}
      {!isEmpty(userTask.potentialGroups) && (
        <FormGroup label="Potential groups" fieldId="potential_groups">
          <Text component={TextVariants.p}>
            {userTask.potentialGroups.join(', ')}
          </Text>
        </FormGroup>
      )}
      <FormGroup label="Started" fieldId="started">
        {userTask.started ? (
          <Text component={TextVariants.p}>
            <Moment fromNow>{new Date(`${userTask.started}`)}</Moment>
          </Text>
        ) : (
          '-'
        )}
      </FormGroup>
      {userTask.completed ? (
        <FormGroup label="Completed" fieldId="completed">
          <Text component={TextVariants.p}>
            <Moment fromNow>{new Date(`${userTask.completed}`)}</Moment>
          </Text>
        </FormGroup>
      ) : (
        <FormGroup label="Last Update" fieldId="lastUpdate">
          <Text component={TextVariants.p}>
            <Moment fromNow>{new Date(`${userTask.lastUpdate}`)}</Moment>
          </Text>
        </FormGroup>
      )}
      <FormGroup label="Process" fieldId="process">
        <Text component={TextVariants.p}>{userTask.processId}</Text>
      </FormGroup>
      <FormGroup label="Process Instance ID" fieldId="processInstance">
        <Text component={TextVariants.p}>{userTask.processInstanceId}</Text>
      </FormGroup>
    </Form>
  );
};

export default TaskDetails;
