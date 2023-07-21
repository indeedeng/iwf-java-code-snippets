package io.workflow.workflow;

import com.google.common.collect.ImmutableList;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.command.TimerCommand;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.SignalChannelDef;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.communication.SignalCommandResult;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.gen.models.ChannelRequestStatus;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InterruptibleTimerWorkflow implements ObjectWorkflow {

    public static final String SIGNAL_INTERRUPT_TIMER = "SIGNAL_INTERRUPT_TIMER";

    @Override
    public List<StateDef> getWorkflowStates() {
        return ImmutableList.of(StateDef.startingState(new TimerState()));
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return ImmutableList.of(SignalChannelDef.create(Void.class, SIGNAL_INTERRUPT_TIMER));
    }
}

class TimerState implements WorkflowState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(
        final Context context,
        final Void input,
        final Persistence persistence,
        final Communication communication
    ) {
        return CommandRequest.forAnyCommandCompleted(
            TimerCommand.createByDuration(Duration.ofMinutes(1)),
            SignalCommand.create(InterruptibleTimerWorkflow.SIGNAL_INTERRUPT_TIMER)
        );
    }

    @Override
    public StateDecision execute(
        final Context context,
        final Void input,
        final CommandResults commandResults,
        final Persistence persistence,
        final Communication communication
    ) {
        // If the SIGNAL_INTERRUPT_TIMER signal is received, force complete the workflow to interrupt
        // the timer
        final SignalCommandResult signalCommandResult = commandResults.getAllSignalCommandResults().get(0);
        if (signalCommandResult.getSignalRequestStatusEnum() == ChannelRequestStatus.RECEIVED) {
            System.out.println("Timer interrupted!");
            return StateDecision.forceCompleteWorkflow();
        }

        System.out.println("Start executing scheduled/tasked job!");
        return StateDecision.forceCompleteWorkflow();
    }
}
