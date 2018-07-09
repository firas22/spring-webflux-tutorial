package com.example.chriniko.webfluxdemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateLottoTicket {

    private List<LottoTicketBoard> boards;

    @Data
    public static class LottoTicketBoard {
        private List<Integer> numbers;
    }
}
