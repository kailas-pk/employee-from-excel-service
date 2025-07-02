package com.kp.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kp.constants.AppConstants;
import com.kp.dto.Employee;
import com.kp.exception.EmployeeHierarchyBuildException;
import com.kp.util.AppUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmployeeService {

	@Value("${employee-info.excel.path}")
	private String filePathOfEmployeeInfo;
	
	@Autowired
	private ObjectMapper mapper;
	
	//Employee Info, In Memory Cache 
	private List<Employee> employees = null;
	
	//Employee Hierarchy, In Memory Cache 
	private Employee employeeHierarchyTree = null;
	
	/**
	 * Builds Hierarchy tree from employees loaded to application, in this scenario from employee cache,
	 * Took deep copy for employees before building the tree to avoid any changes to employees structure.
	 * Can easily be extended to load the employees from db for building hierarchy tree.
	 * @return 
	 * @throws EmployeeHierarchyBuildException
	 */
	
	public boolean buildEmployeeHierarchyTreeFromEmployeesAndSaveToJsonFile() throws EmployeeHierarchyBuildException {
		log.info("Building Employee Hierarchy tree at : {} ", LocalDateTime.now());

		//Employees is empty which means employee-info is not loaded yet to the cache, hence return false
		if(employees == null)
			return false;
		
		if(employeeHierarchyTree != null)
			return true;
		
		List<Employee> copyOfEmployees = employees.stream().map(e -> {
			try {
				return e.clone();
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}
			
			return null;
		}).collect(Collectors.toList());
		
		Map<Integer, Employee> managerIdEmployeeMap = copyOfEmployees.stream()
				.collect(Collectors.toMap(Employee::getId, Function.identity()));

		Employee employeeDirector = null;

		for (Employee employee : copyOfEmployees) {
			if (employee.getManagerId() != 0) {
				Employee employeeManager = managerIdEmployeeMap.get(employee.getManagerId());
				if (employeeManager != null)
					employeeManager.setReportsOfEmployee(employee);
			} else {
				employeeDirector = employee;
			}
		}

		// Write to JSON
		try {
			mapper.writeValue(new File(AppUtil.getNewEmployeeHierarchyJsonFileName() + ".json"), employeeDirector);
		} catch (IOException e) {
			log.error("Exception occurred while writing the tree json to a file : {}", e);
			throw new EmployeeHierarchyBuildException(e.getMessage(), e);
		}
		
		employeeHierarchyTree = employeeDirector;
		
		return true;
	}

	/**
	 * Loads Employee information from an excel file and saves it into in memory employee cache
	 * @return
	 */
	public void loadEmployeeInfoFromExcelFile() throws Exception {
		if(employees != null)
			return;
		
		List<Employee> employeesFromExcel = new ArrayList<>();
		DataFormatter dataFormatter = new DataFormatter();
		ClassPathResource resource = new ClassPathResource(filePathOfEmployeeInfo);
		try (InputStream inputStream = resource.getInputStream();
				Workbook workbook = new XSSFWorkbook(inputStream);) {
			Sheet employeeInfoSheet = workbook.getSheetAt(0);
		
			for (Row row : employeeInfoSheet) {
				if (row.getRowNum() == 0)
					continue; 

				Employee employee = new Employee();
				employee.setId(AppUtil.isValidNumeric(dataFormatter.formatCellValue(row.getCell(0))) ? 
						Integer.parseInt(dataFormatter.formatCellValue(row.getCell(0))) : 0);
				employee.setName(dataFormatter.formatCellValue(row.getCell(1)));
				employee.setCity(dataFormatter.formatCellValue(row.getCell(2)));
				employee.setState(dataFormatter.formatCellValue(row.getCell(3)));
				employee.setCategory(dataFormatter.formatCellValue(row.getCell(4)));
				employee.setManagerId(AppUtil.isValidNumeric(dataFormatter.formatCellValue(row.getCell(5))) ? 
						Integer.parseInt(dataFormatter.formatCellValue(row.getCell(5))) : 0);
				employee.setSalary(AppUtil.isValidNumeric(dataFormatter.formatCellValue(row.getCell(6))) ? 
						Integer.parseInt(dataFormatter.formatCellValue(row.getCell(6))) : 0);
				
				employee.setDoj(employee.getDateOfJoin(dataFormatter.formatCellValue(row.getCell(7))));
				employeesFromExcel.add(employee);
			}
		}
		employees = employeesFromExcel;
		log.info("Employees : {}", employeesFromExcel);
	}

	public List<Employee> getAllEmployeesFromCache(){	
		return employees;
	}
	
	public Employee getEmployeeHierarchyFromCache() {
		return employeeHierarchyTree;
	}
	
	/**
	 * Gets all the employees who are eligible for gratuity
	 * @return
	 */
	public List<Employee> getAllEmployeesEligibleForGratuity(){
		if(employees != null) {
			return employees.stream().filter(employee -> {
				long monthDiff = ChronoUnit.MONTHS.between(employee.getDoj(), LocalDate.now());
				return monthDiff > AppConstants.GRATUITY_ELIGIBILITY_IN_MONTH ;
			 }).collect(Collectors.toList());
		} 
		
		return null;
	}
	
	/**
	 * Gets all the employees whose salary is greater than their manager
	 * @return
	 */
	public List<Employee> getAllEmployeesWhoseSalaryIsGreaterThanTheirManager(){
		if(employees != null) {
			Map<Integer, Employee> managerIdEmployeeMap = employees.stream()
					.collect(Collectors.toMap(Employee::getId, Function.identity()));
			
			return employees.stream().filter(employee -> {
				Employee manager = managerIdEmployeeMap.get(employee.getManagerId());
				return employee.getSalary() > ((manager == null) ? 0 : manager.getSalary()) ;
			 }).collect(Collectors.toList());
		}
		
		return null;
    }
	
	/**
	 * Gets all the employees whose salary is nth Highest in desc order
	 * @return
	 */
	public Employee findAnEmployeeWhoeSalaryIsNthHighestInDescOrder(long order){
		if(employees != null) {
			return employees.stream()
			 	.sorted(Comparator.comparing(Employee::getSalary).reversed())
			 	   .skip(order)
			 	   .findFirst().orElseGet(null);
		}
		
		return null;
    }
}
