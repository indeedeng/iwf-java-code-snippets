package io.workflow.workflow.taskdependency.multiple;

import static io.workflow.workflow.taskdependency.multiple.TaskDependencyMultipleWorkflow.INTERNAL_CHANNEL_STATE_B;
import static io.workflow.workflow.taskdependency.multiple.TaskDependencyMultipleWorkflow.INTERNAL_CHANNEL_STATE_C;

import com.google.common.collect.ImmutableList;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.InternalChannelCommand;
import io.iworkflow.core.communication.InternalChannelCommandResult;
import io.iworkflow.core.communication.InternalChannelDef;
import io.iworkflow.core.persistence.Persistence;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TaskDependencyMultipleWorkflow implements ObjectWorkflow {

    public static final String INTERNAL_CHANNEL_STATE_B = "INTERNAL_CHANNEL_STATE_B";
    public static final String INTERNAL_CHANNEL_STATE_C = "INTERNAL_CHANNEL_STATE_C";

    @Override
    public List<StateDef> getWorkflowStates() {
        return ImmutableList.of(
            StateDef.startingState(new StartState()),
            StateDef.nonStartingState(new StateA()),
            StateDef.nonStartingState(new StateB()),
            StateDef.nonStartingState(new StateC())
        );
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return ImmutableList.of(
            InternalChannelDef.create(Integer.class, INTERNAL_CHANNEL_STATE_B),
            InternalChannelDef.create(Integer.class, INTERNAL_CHANNEL_STATE_C)
        );
    }
}

class StartState implements WorkflowState<Void> {

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
        return StateDecision.multiNextStates(StateA.class, StateB.class, StateC.class);
    }
}

class StateA implements WorkflowState<Integer> {

    @Override
    public Class<Integer> getInputType() {
        return Integer.class;
    }

    @Override
    public CommandRequest waitUntil(
        final Context context,
        final Integer input,
        final Persistence persistence,
        final Communication communication
    ) {
        return CommandRequest.forAllCommandCompleted(
            InternalChannelCommand.create(INTERNAL_CHANNEL_STATE_B),
            InternalChannelCommand.create(INTERNAL_CHANNEL_STATE_C)
        );
    }

    @Override
    public StateDecision execute(
        final Context context,
        final Integer input,
        final CommandResults commandResults,
        final Persistence persistence,
        final Communication communication
    ) {
        final List<InternalChannelCommandResult> internalChannelCommandResults = commandResults.getAllInternalChannelCommandResult();
        final int valueB = (int) internalChannelCommandResults.get(0).getValue().get();
        final int valueC = (int) internalChannelCommandResults.get(1).getValue().get();

        System.out.printf("Task A received the value %d!%n", valueB + valueC);
        return StateDecision.forceCompleteWorkflow(valueB + valueC);
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
        communication.publishInternalChannel(INTERNAL_CHANNEL_STATE_B, 1);
        return StateDecision.deadEnd();
    }
}

class StateC implements WorkflowState<Integer> {

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
        communication.publishInternalChannel(INTERNAL_CHANNEL_STATE_C, 2);
        return StateDecision.deadEnd();
    }
}
