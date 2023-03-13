package com.sttl.hrms.workflow.statemachine.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor // required by Kryo
@ToString
@EqualsAndHashCode
public class Pair<T, U> {

    private T first;
    private U second;


}
