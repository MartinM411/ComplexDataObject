package com.github.TKnudsen.ComplexDataObject.model.io.writers.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Title: JSONWriter
 * </p>
 * 
 * <p>
 * Description: writes a ComplexDataObject as JSON
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public class JSONWriter {

	public static String writeToString(ComplexDataObject complexDataObject) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES,true);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		String stringRepresentation;
		try {
			stringRepresentation = mapper.writeValueAsString(complexDataObject);
			return stringRepresentation;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void writeToFile(ComplexDataObject complexDataObject, String file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES,true);
		mapper.configure(SerializationFeature.INDENT_OUTPUT,true);
		
		try {
			mapper.writeValue(new File(file), complexDataObject);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}
}
