package com.kp.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import lombok.Data;

@Data
public class Employee implements Cloneable {

	 private int id;
	 private String name;
	 private String city;
	 private String state;
	 private String category;
	 private Integer managerId;
	 private double salary;
	 private LocalDate doj;
	 private List<Employee> reports = new ArrayList<>();

	 public Employee() {}
	 
	 public Employee(int id, String name, String city, String state, String category, Integer managerId, double salary, String dojStr) {
	        this.id = id;
	        this.name = name;
	        this.city = city;
	        this.state = state;
	        this.category = category;
	        this.managerId = managerId;
	        this.salary = salary;
	        this.doj = this.getDateOfJoin(dojStr);
	    }
	  
	 public void setReportsOfEmployee(Employee employee) {
		reports.add(employee);
	 }
	 
	 public LocalDate getDateOfJoin(String dojStr) {
		 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy");
		 LocalDate dDateoJoin =  LocalDate.parse(dojStr, formatter);
		return dDateoJoin;
	 }
	 
	 
	@Override
	public Employee clone() throws CloneNotSupportedException {
		Employee clonedEmployee = (Employee) super.clone();

		clonedEmployee.reports = this.reports.stream().map(e -> {
			try {
				return (Employee) e.clone(); // Clone each Employee
			} catch (CloneNotSupportedException ex) {
				throw new RuntimeException(ex); 
			}
		}).collect(Collectors.toList());

		return clonedEmployee;
	}
}
