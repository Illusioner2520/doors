package net.illusioncraft.Doors;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
	  public JsonNode readJson(File file) { 
	        ObjectMapper objectMapper = new ObjectMapper(); 
	        JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(file);
				return jsonNode;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
	   } 
}
