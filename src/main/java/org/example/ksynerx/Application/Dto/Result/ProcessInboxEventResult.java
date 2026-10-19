package org.example.ksynerx.Application.Dto.Result;

import org.example.ksynerx.Domain.Model.InboxStatus;

public record ProcessInboxEventResult(long eventId, InboxStatus status) { }
