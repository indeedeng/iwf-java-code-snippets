package io.workflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.ClientSideException;
import io.iworkflow.gen.models.ErrorSubStatus;
import io.workflow.workflow.interruptibletimer.InterruptibleTimerWorkflow;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/timer")
@AllArgsConstructor
public class InterruptibleTimerWorkflowController {

    private final Client client;

    @PostMapping("/start")
    public ResponseEntity<String> startTimer(final @RequestBody Map<String, String> requestBody) {
        final String taskId = requestBody.get("taskId");
        final String workflowId = getWorkflowId(taskId);

        try {
            client.startWorkflow(InterruptibleTimerWorkflow.class, workflowId, 0);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                return ResponseEntity.ok(String.format("The workflow %s has been running.", workflowId));
            }
            throw new RuntimeException(String.format("Workflow error: %s", workflowId), e);
        }

        return ResponseEntity.ok(String.format("Started timer for task %s: %s", taskId, workflowId));
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopTimer(final @RequestBody Map<String, String> requestBody) {
        final String taskId = requestBody.get("taskId");
        final String workflowId = getWorkflowId(taskId);

        try {
            client.signalWorkflow(
                InterruptibleTimerWorkflow.class,
                workflowId,
                "",
                InterruptibleTimerWorkflow.SIGNAL_INTERRUPT_TIMER,
                null
            );
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_NOT_EXISTS_SUB_STATUS) {
                return ResponseEntity.ok(String.format("The workflow %s is not running.", workflowId));
            }
            throw new RuntimeException(String.format("Workflow error: %s", workflowId), e);
        }

        return ResponseEntity.ok(String.format("Stopped timer for task %s: %s", taskId, workflowId));
    }

    private String getWorkflowId(final String taskId) {
        return "interruptible_timer_" + taskId;
    }
}
