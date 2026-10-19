package org.example.ksynerx.Application.Usecase;

import org.example.ksynerx.Application.Dto.Result.ProcessInboxEventResult;

import java.util.Optional;

public interface ProcessNextInboxEventUseCase {
    Optional<ProcessInboxEventResult> execute();
}
