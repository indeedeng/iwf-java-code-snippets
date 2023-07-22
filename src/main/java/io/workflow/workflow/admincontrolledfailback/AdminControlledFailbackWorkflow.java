package io.workflow.workflow.admincontrolledfailback;

import com.google.common.collect.ImmutableList;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.gen.models.RetryPolicy;
import io.iworkflow.gen.models.WorkflowStateOptions;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AdminControlledFailbackWorkflow implements ObjectWorkflow {

    private final AdminControlledFailbackDependencyService myService;

    @Override
    public List<StateDef> getWorkflowStates() {
        return ImmutableList.of(StateDef.startingState(new FailbackState(myService)));
    }
}

@AllArgsConstructor
class FailbackState implements WorkflowState<Void> {

    private final AdminControlledFailbackDependencyService myService;

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public StateDecision execute(
        final Context context,
        final Void input,
        final CommandResults commandResults,
        final Persistence persistence,
        final Communication communication
    ) {
        myService.simpleMethod();
        System.out.println("Failback is working!");
        return StateDecision.forceCompleteWorkflow();
    }

    /**
     * The admin (iWF) will handle the retry policy, even in the event of the client being down.
     *
     * By default, all state execution will retry infinitely (until workflow timeout).
     * The example below shows a retry policy based on the maximumAttemptsDurationSeconds, which means to retry for at most 1 hour.
     * You can also use another parameter maximumAttempts to control maximum retry attempts instead.
     */
    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptions()
            .executeApiRetryPolicy(
                new RetryPolicy()
                    .maximumAttemptsDurationSeconds(3600)
                    .backoffCoefficient(2f)
                    .initialIntervalSeconds(10)
                    .maximumIntervalSeconds(60)
            );
    }
}
