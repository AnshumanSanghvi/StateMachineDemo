package com.anshuman.statemachinedemo.entity;

import java.io.Serializable;

public interface Identifiable<ID extends Serializable> {

    ID getId();
}
