package com.example.chriniko.webfluxdemo.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import java.util.List;

@Data
@Builder

@UserDefinedType
public class Board {

    @Column
    private List<Integer> numbers;
}
