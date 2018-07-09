package com.example.chriniko.webfluxdemo.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateLottoTicket {

    private UUID ticketId;
    private List<LottoTicketBoard> boards;

    @Data
    public static class LottoTicketBoard {
        private List<Integer> numbers;
    }
}
