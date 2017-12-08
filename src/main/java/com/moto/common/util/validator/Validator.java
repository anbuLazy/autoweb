package com.moto.common.util.validator;

/**
 * This interface is used to have its inherited classed validate whichever values
 * are handled in the HTTP Requests and Responses, performed by this framework.
 * 
 * @author eduardof
 *
 */
public interface Validator {
	
	/**
	 * Given an array of String parameters, the implementation of this method is responsible
	 * for validating a customized logic.
	 * 
	 * @param parameters Array of parameters which are the result of either a HTTP Request or Response.
	 * 					One can enter a multitude of parameters so the method has the flexibility for validating any
	 * 					logic which the user deems necessary.
	 * 
	 * @return errors
	 */
	public String[] validate(String[] parameters, String jsonData);
}