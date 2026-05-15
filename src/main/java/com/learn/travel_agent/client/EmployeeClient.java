package com.learn.travel_agent.client;

import com.learn.travel_agent.model.Employee;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "employee-client", url = "http://localhost:8080")
public interface EmployeeClient {

    @GetMapping("/employee/{employeeId}")
    public Employee getEmployee(@PathVariable String employeeId);
}
