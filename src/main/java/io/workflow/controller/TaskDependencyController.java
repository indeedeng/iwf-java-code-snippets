package io.workflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.ClientSideException;
import io.iworkflow.gen.models.ErrorSubStatus;
import io.workflow.workflow.taskdependency.multiple.TaskDependencyMultipleWorkflow;
import io.workflow.workflow.taskdependency.single.TaskDependencySingleWorkflow;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dependency")
@AllArgsConstructor
public class TaskDependencyController {

    private final Client client;

    @PostMapping("/single/start")
    public ResponseEntity<String> startSingleDepedency(final @RequestBody Map<String, String> requestBody) {
        final String taskId = requestBody.get("taskId");
        final String workflowId = getWorkflowId(taskId, true);

        try {
            client.startWorkflow(TaskDependencySingleWorkflow.class, workflowId, 0, 11);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                return ResponseEntity.ok(String.format("The workflow %s has been running.", workflowId));
            }
            throw new RuntimeException(String.format("Workflow error: %s", workflowId), e);
        }

        final Integer output = client.getSimpleWorkflowResultWithWait(Integer.class, workflowId);

        return ResponseEntity.ok(String.format("The output of single dependency task %s: %d", taskId, output));
    }

    @PostMapping("/multiple/start")
    public ResponseEntity<String> startMultipleDependency(final @RequestBody Map<String, String> requestBody) {
        final String taskId = requestBody.get("taskId");
        final String workflowId = getWorkflowId(taskId, false);

        try {
            client.startWorkflow(TaskDependencyMultipleWorkflow.class, workflowId, 0);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                return ResponseEntity.ok(String.format("The workflow %s has been running.", workflowId));
            }
            throw new RuntimeException(String.format("Workflow error: %s", workflowId), e);
        }

        final Integer output = client.getSimpleWorkflowResultWithWait(Integer.class, workflowId);

        return ResponseEntity.ok(String.format("The output of multiple dependency task %s: %d", taskId, output));
    }

    private String getWorkflowId(final String taskId, final boolean isSingleDependency) {
        if (isSingleDependency) {
            return "task_dependency_single_" + taskId;
        }
        return "task_dependency_multiple_" + taskId;
    }
}
