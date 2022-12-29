package com.anshuman.statemachinedemo.workflow.model.converter;

import com.anshuman.statemachinedemo.workflow.model.enums.WorkflowType;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class WorkflowTypeIdConverter implements AttributeConverter<WorkflowType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(WorkflowType attribute) {
        return attribute.getTypeId();
    }

    @Override
    public WorkflowType convertToEntityAttribute(Integer dbData) {
        return WorkflowType.fromId(dbData);
    }
}