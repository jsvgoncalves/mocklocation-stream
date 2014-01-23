package org.up.fe.jsvgoncalves.ssim.firstapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {

	public static JSONObject string2JSON(String json){
		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			return new JSONObject();
		}
	}

	public static String getValue(JSONObject json, String... path) {
		JSONObject newJson = json;
		int i = 0;
		for (; i < path.length-1; i++) {
			JSONObject temp = newJson;
			try {
				newJson = string2JSON(temp.getString(path[i]));
			} catch (JSONException e) {
				return "";
			}
		}
		try {
			return newJson.getString(path[i]);
		} catch (JSONException e) {
			System.err.println("getValue: Invalid JSON; " + path[i] + " not found.");
			return "";
		}
	}

	public static ArrayList<String> getArray(JSONObject json, String... path) {
		JSONObject newJson = json;
		JSONArray arr = new JSONArray();
		int i = 0;
		for (; i < path.length-1; i++) {
			JSONObject temp = newJson;
			try {
				newJson = string2JSON(temp.getString(path[i]));
			} catch (JSONException e) {
				System.err.println("getArray: Invalid JSON path; " + path[i] + " not found.");
				return new ArrayList<String>();
			}
		}
		
		try {
			arr = newJson.getJSONArray(path[i]);
			System.err.println("Parsing JSON Array of size " + arr.length());
			ArrayList<String> strArray = new ArrayList<String>();
			for (int j = 0; j < arr.length(); j++) {
				strArray.add(arr.getString(j));
			}
			return strArray;
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			System.err.println("getArray: Invalid JSON array; " + path[i] + " not found.");
			return new ArrayList<String>();
		}
	}
	
	
	public static String changeDateFormat(String oldFormat, String newFormat, String dateString) {
		try {
			String newDateString;
	
			SimpleDateFormat sdf = new SimpleDateFormat(oldFormat, Locale.getDefault());
			Date d;
		
			d = sdf.parse(dateString);
			sdf.applyPattern(newFormat);
			newDateString = sdf.format(d);
			return newDateString;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Undefined date";
	}
	
}