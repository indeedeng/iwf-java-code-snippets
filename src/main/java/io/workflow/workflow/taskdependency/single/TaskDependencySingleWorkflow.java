package io.workflow.workflow.taskdependency.single;

import com.google.common.collect.ImmutableList;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TaskDependencySingleWorkflow implements ObjectWorkflow {

    @Override
    public List<StateDef> getWorkflowStates() {
        return ImmutableList.of(StateDef.startingState(new StateB()), StateDef.nonStartingState(new StateA()));
    }
}

class StateA implements WorkflowState<Integer> {

    @Override
    public Class<Integer> getInputType() {
        return Integer.class;
    }

    @Override
    public StateDecision execute(
        final Context context,
        final Integer input,
        final CommandResults commandResults,
        final Persistence persistence,
        final Communication communication
    ) {
        System.out.printf("Task A received the value %d!%n", input);
        return StateDecision.forceCompleteWorkflow(input);
    }
}

class StateB implements WorkflowState<Integer> {

    @Override
    public Class<Integer> getInputType() {
        return Integer.class;
    }

    @Override
    public StateDecision execute(
        final Context context,
        final Integer input,
        final CommandResults commandResults,
        final Persistence persistence,
        final Communication communication
    ) {
        return StateDecision.singleNextState(StateA.class, input);
    }
}
