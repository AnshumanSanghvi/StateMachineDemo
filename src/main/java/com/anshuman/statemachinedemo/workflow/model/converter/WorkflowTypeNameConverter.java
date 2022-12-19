package com.anshuman.statemachinedemo.workflow.model.converter;

import com.anshuman.statemachinedemo.workflow.model.enums.WorkflowType;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class WorkflowTypeNameConverter  implements AttributeConverter<WorkflowType, String> {

    @Override
    public String convertToDatabaseColumn(WorkflowType attribute) {
        return attribute.getName();
    }

    @Override
    public WorkflowType convertToEntityAttribute(String dbData) {
        return WorkflowType.fromName(dbData);
    }
}


