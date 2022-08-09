package com.base.util.json;

import com.alibaba.fastjson.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class DJSONObject implements DJSON{

	
	private Map<String,Object> json = new HashMap<String, Object>();
	
	private static String jsonContext;
	
	private static LinkedList<Character> jsonChar = new LinkedList<>();
	
	
	public static DJSONObject paseJson(String jsonText) {
		jsonContext = jsonText;
		analysis();
		return new DJSONObject();}
	@Override
	public int size() {
		return json.size();
	}

	@Override
	public boolean isEmpty() {
		return json.isEmpty();
	}
	
	private static void analysis() {
		checkJsonContext();
	}
	
	private static void checkJsonContext() {
		if(jsonContext==null || jsonContext.trim().length()==0) {
			throw new JSONParseErrorException("json parse error : empty String");
		}
		jsonContext = jsonContext.trim();
		System.out.println(jsonContext);
		Stack<String>stack = new Stack<>();
		for(int i =0;i<jsonContext.length();i++) {
			String s = jsonContext.substring(i,i+1);
			if(s.equals("{") || s.equals("[")) {
				stack.add(s+":"+i);
			}else if(s.equals("}") || s.equals("]")) {
				if(stack.isEmpty()) {
					throw new JSONParseErrorException("json parse error : char at "+i+" "+s);
				}else {
					String st  =stack.pop();
					String c = st.substring(0,st.indexOf(":"));
					int index = Integer.parseInt(st.substring(st.indexOf(":")+1));
					if((s.equals("}") && !c.equals("{")) || (s.equals("]") && !c.equals("["))) {
						throw new JSONParseErrorException("json parse error : char at "+i+" "+s);
					}
					System.out.println(jsonContext.substring(index,i+1));
				}
			}
		}
	}
	
	private static DJSONObject json(){
		
		return null;
	}
	
	private static DJSONArray array(){
		
		return null;
	}
	
	@Override
	public boolean containsKey(Object key) {
		return json.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return json.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return json.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		return json.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return json.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		json.putAll(m);
	}

	@Override
	public void clear() {
		json.clear();		
	}

	@Override
	public Set<String> keySet() {
		return json.keySet();
	}

	@Override
	public Collection<Object> values() {
		return json.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return json.entrySet();
	}

	@Override
	public String toJSONString() {
		return null;
	}
	
	@Override
	public String toString() {
		return null;
	}

	public static void main(String[] args) {
		String s = "{\"stop_y\":421,\"videoId\":\"video_camera+da9e28114c354584a1a5fa528b7e0eda\",\"isZoomTele\":1,\"host_id\":\"video_host+02305259-7fc1-42dd-9558-9e7715eec675\",\"central_y\":750,\"central_x\":1266,\"stop_x\":554,\"start_x\":480,\"centralY\":750.46875,\"camera_code\":\"8\",\"centralX\":1266,\"start_y\":360,\"width\":1266,\"startY\":360,\"startX\":480,\"stopX\":554,\"stopY\":421,\"height\":750.46875,\"is_zoom_tele\":1}\n";
		JSONObject jsonObject = (JSONObject) JSONObject.parse(s);
		char EOI            = 0x1A;
		System.out.println((char)65279);
	}
}
