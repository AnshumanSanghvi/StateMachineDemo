package com.anshuman.workflow.statemachine.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Pair<T, U> {

    private T first;
    private U second;


}
