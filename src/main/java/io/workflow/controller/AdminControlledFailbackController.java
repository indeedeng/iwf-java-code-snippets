package io.workflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.ClientSideException;
import io.iworkflow.gen.models.ErrorSubStatus;
import io.workflow.workflow.admincontrolledfailback.AdminControlledFailbackWorkflow;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/failback")
@AllArgsConstructor
public class AdminControlledFailbackController {

    private final Client client;

    @PostMapping("/start")
    public ResponseEntity<String> start(final @RequestBody Map<String, String> requestBody) {
        final String taskId = requestBody.get("taskId");
        final String workflowId = getWorkflowId(taskId);

        try {
            client.startWorkflow(AdminControlledFailbackWorkflow.class, workflowId, 3600);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                return ResponseEntity.ok(String.format("The workflow %s has been running.", workflowId));
            }
            throw new RuntimeException(String.format("Workflow error: %s", workflowId), e);
        }

        return ResponseEntity.ok(
            String.format("Started admin controlled failback workflow for task %s: %s", taskId, workflowId)
        );
    }

    private String getWorkflowId(final String taskId) {
        return "admin_controlled_failback_" + taskId;
    }
}
