package com.kp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kp.dto.Employee;
import com.kp.exception.EmployeeHierarchyBuildException;
import com.kp.service.EmployeeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class EmployeeController {
	
	@Autowired
	private EmployeeService employeeService;
	
	@GetMapping("/buildEmployeeHierarchyTree")
	public String builEmployeeHierarchyTree() {
		boolean result = false;
		log.info("REST Call to build and save employe hierarchy tree into json file");
		try {
			   result = employeeService.buildEmployeeHierarchyTreeFromEmployeesAndSaveToJsonFile();
		} catch (EmployeeHierarchyBuildException e) {
			log.info("REST Call to build and save employe hierarchy tree into json file, FAILED : {}", e.getMessage());
			return "FAILED";
		}
		return result ? "SUCCESS: Please use the API /employeeHierarchyTree to view Tree" : ""
				+ "Please load employee info from excel using the API /loadEmployeeInfo ";
	}
	
	@GetMapping("/employees")
	public ResponseEntity<?> getAllEmployees() {
		log.info("REST Call to getAllEmployees ");
		List<Employee> employees = employeeService.getAllEmployeesFromCache();
		
		if(employees == null)
			return ResponseEntity.ok("Please load employee info from excel using the API /loadEmployeeInfo ");
		
		return new ResponseEntity<>(employees, HttpStatus.OK);
	}
	
	@GetMapping("/loadEmployeeInfo")
	public String loadEmployeeInfoFromExcelFile() {
		log.info("REST Call to load employee info from excel file");
		try {
			employeeService.loadEmployeeInfoFromExcelFile();
		} catch (Exception e) {
			log.error("Error occurred! : {}", e);
			return "FAILED";
		}
		return "SUCCESS: Please use the API /employees to view all employees";
	}
	
	
	@GetMapping("/employeeHierarchyTree")
	public ResponseEntity<?> getEmployeeHierarchy() {
		log.info("REST Call to get employee hierarchy tree");
		Employee employee = employeeService.getEmployeeHierarchyFromCache();
		
		if(employee == null)
			return ResponseEntity.ok("Please build employee hierarchy tree using the API /buildEmployHierarchyTree ");
		
		return new ResponseEntity<>(employee, HttpStatus.OK);
	}
	
	@GetMapping("/employeesEligibleForGratuity")
	public ResponseEntity<?> getAllEmployeesEligibleForGratuity() {
		log.info("REST Call to get all employees eligible for gratuity");
		List<Employee> employees = employeeService.getAllEmployeesEligibleForGratuity();
		
		return new ResponseEntity<>(employees, HttpStatus.OK);
	}
	
	
	@GetMapping("/employeesWhoseSalaryIsGreaterManager")
	public ResponseEntity<?> getAllEmployeesWhoseSalaryIsGreaterThanTheirManager() {
		log.info("REST Call to get all employees whose salary is higher than their managers");
		List<Employee> employees = employeeService.getAllEmployeesWhoseSalaryIsGreaterThanTheirManager();
		return new ResponseEntity<>(employees, HttpStatus.OK);
	}
	
	@GetMapping("/employeeSalaryNthHighestInDesc")
	public ResponseEntity<?> findAnEmployeeWhoeSalaryIsNthHighestInDescOrder(@RequestParam("order") long order) {
		log.info("REST Call to get all employees whose salary is higher than their managers");
		Employee employee = employeeService.findAnEmployeeWhoeSalaryIsNthHighestInDescOrder(order);
		return new ResponseEntity<>(employee, HttpStatus.OK);
	}
	
}
