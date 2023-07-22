package io.workflow.workflow.longtermstorage;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class LongTermStorageInput {

    public String storage;
}
