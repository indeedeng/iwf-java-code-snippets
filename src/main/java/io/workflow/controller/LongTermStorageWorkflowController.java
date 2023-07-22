package io.workflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.ClientSideException;
import io.iworkflow.gen.models.ErrorSubStatus;
import io.workflow.workflow.longtermstorage.LongTermStorageInput;
import io.workflow.workflow.longtermstorage.LongTermStorageWorkflow;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
@AllArgsConstructor
public class LongTermStorageWorkflowController {

    private final Client client;

    @PostMapping("/start")
    public ResponseEntity<String> startStore(final @RequestBody Map<String, String> requestBody) {
        final String id = requestBody.get("id");
        final String storage = requestBody.get("storage");

        final String workflowId = LongTermStorageWorkflowController.getStorageWorkflowId(id);

        final LongTermStorageInput input = LongTermStorageInput.builder().storage(storage).build();

        try {
            // The timeout is set to 0, indicating that the workflow will never time out
            client.startWorkflow(LongTermStorageWorkflow.class, workflowId, 0, input);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                return ResponseEntity.ok(String.format("The workflow %s has been running.", workflowId));
            }
            throw new RuntimeException(String.format("Workflow error: %s", workflowId), e);
        }

        return ResponseEntity.ok(String.format("Started workflowId: %s", workflowId));
    }

    @GetMapping("/get")
    public ResponseEntity<String> getStorage(final @RequestParam(defaultValue = "id") String id) {
        final String workflowId = LongTermStorageWorkflowController.getStorageWorkflowId(id);

        final LongTermStorageWorkflow rpcStub = client.newRpcStub(LongTermStorageWorkflow.class, workflowId, "");

        try {
            final String storage = client.invokeRPC(rpcStub::getStorage);
            return ResponseEntity.ok(storage);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_NOT_EXISTS_SUB_STATUS) {
                return ResponseEntity.ok("");
            }
            throw new RuntimeException(String.format("Workflow error: %s", workflowId), e);
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopStore(final @RequestBody Map<String, String> requestBody) {
        final String id = requestBody.get("id");

        final String workflowId = LongTermStorageWorkflowController.getStorageWorkflowId(id);

        final LongTermStorageWorkflow rpcStub = client.newRpcStub(LongTermStorageWorkflow.class, workflowId, "");

        try {
            client.invokeRPC(rpcStub::stop);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_NOT_EXISTS_SUB_STATUS) {
                return ResponseEntity.ok(String.format("The workflow %s is not running.", workflowId));
            }
            throw new RuntimeException(String.format("Workflow error: %s", workflowId), e);
        }

        return ResponseEntity.ok(String.format("Stopped workflowId: %s", workflowId));
    }

    public static String getStorageWorkflowId(final String id) {
        return "long_term_storage_" + id;
    }
}
