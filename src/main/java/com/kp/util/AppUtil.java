package com.kp.util;

import java.time.LocalDateTime;

import com.kp.constants.AppConstants;

public class AppUtil {

	public static String getNewEmployeeHierarchyJsonFileName() {
		LocalDateTime now = LocalDateTime.now();
		StringBuilder builder = new StringBuilder(AppConstants.HIERARCHY_FILE_NAME);
		builder.append(AppConstants.FILENAME_SEPARTOR);
		builder.append(now.getYear());
		builder.append(AppConstants.DATE_SEPARATOR);
		builder.append(now.getMonth());
		builder.append(AppConstants.DATE_SEPARATOR);
		builder.append(now.getDayOfMonth());
		builder.append(AppConstants.DATE_SEPARATOR);
		builder.append(now.getHour());
		builder.append(AppConstants.TIME_SEPARTOR);
		builder.append(now.getMinute());
		builder.append(AppConstants.TIME_SEPARTOR);
		builder.append(now.getSecond());
	        
	    return builder.toString();
	 }
	
	 public static boolean isValidNumeric(String str) {
	        if (str == null || str.isEmpty()) {
	            return false;
	        }
	        String regex = "-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?";
	        return str.matches(regex);
	    }
}
