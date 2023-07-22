package io.workflow.workflow.longtermstorage;

import static io.workflow.workflow.longtermstorage.LongTermStorageWorkflow.DATA_ATTRIBUTE_LONG_TERM_STORAGE;

import com.google.common.collect.ImmutableList;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.StateMovement;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LongTermStorageWorkflow implements ObjectWorkflow {

    public static final String DATA_ATTRIBUTE_LONG_TERM_STORAGE = "DATA_ATTRIBUTE_LONG_TERM_STORAGE";

    @Override
    public List<StateDef> getWorkflowStates() {
        return ImmutableList.of(StateDef.startingState(new StartState()), StateDef.nonStartingState(new StopState()));
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return ImmutableList.of(DataAttributeDef.create(String.class, DATA_ATTRIBUTE_LONG_TERM_STORAGE));
    }

    @RPC
    public void stop(final Context context, final Persistence persistence, final Communication communication) {
        communication.triggerStateMovements(StateMovement.create(StopState.class));
    }

    @RPC
    public String getStorage(final Context context, final Persistence persistence, final Communication communication) {
        return persistence.getDataAttribute(DATA_ATTRIBUTE_LONG_TERM_STORAGE, String.class);
    }
}

class StartState implements WorkflowState<LongTermStorageInput> {

    @Override
    public Class<LongTermStorageInput> getInputType() {
        return LongTermStorageInput.class;
    }

    @Override
    public StateDecision execute(
        final Context context,
        final LongTermStorageInput input,
        final CommandResults commandResults,
        final Persistence persistence,
        final Communication communication
    ) {
        persistence.setDataAttribute(DATA_ATTRIBUTE_LONG_TERM_STORAGE, input.getStorage());
        // The whole workflow continues running when the timeout is set to 0
        return StateDecision.deadEnd();
    }
}

class StopState implements WorkflowState<Void> {

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
        persistence.setDataAttribute(DATA_ATTRIBUTE_LONG_TERM_STORAGE, "");
        return StateDecision.forceCompleteWorkflow();
    }
}
