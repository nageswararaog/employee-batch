package com.imaginnovate.demo.batch;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.imaginnovate.demo.dto.EmployeeDTO;

public class EmployeeFieldSetMapper implements FieldSetMapper<EmployeeDTO> {

    @Override
    public EmployeeDTO mapFieldSet(FieldSet fieldSet) throws BindException {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId(Integer.valueOf(fieldSet.readString("employeeId")));
        employeeDTO.setFirstName(fieldSet.readString("firstName"));
        employeeDTO.setLastName(fieldSet.readString("lastName"));
        employeeDTO.setEmail(fieldSet.readString("email"));
        employeeDTO.setDoj(fieldSet.readDate("doj", "yyyy-MM-dd").toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        employeeDTO.setSalary(fieldSet.readBigDecimal("salary"));

        // Split phone numbers by comma
        String phoneNumbersString = fieldSet.readString("phoneNumber");
        List<String> phoneNumbers = Arrays.asList(phoneNumbersString.split(","));
        employeeDTO.setPhoneNumber(phoneNumbers);

        return employeeDTO;
    }
}
