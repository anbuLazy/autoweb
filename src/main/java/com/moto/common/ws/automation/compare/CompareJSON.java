package com.moto.common.ws.automation.compare;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import com.jayway.jsonpath.JsonPath;

public class CompareJSON {
	private static final Logger logger = LogManager.getLogger(CompareJSON.class);
	
	List<String> includes;
	List<String> excludes;
	Object source;
	Object matcher;

	public CompareJSON(String matcher, String source) {
		assertNotNull("source should not be null.", source);
		assertNotNull("matcher should not be null.", matcher);

		if (source.startsWith("{") && matcher.startsWith("{")) {
			this.source = JSONObject.fromObject(source);
			this.matcher = JSONObject.fromObject(matcher);
		} else if (source.startsWith("[") && matcher.startsWith("[")) {
			this.source = JSONArray.fromObject(source);
			this.matcher = JSONArray.fromObject(matcher);
		} else {
			fail("Passed Object cannot be converted to JSON.");
		}
	}
	
	/*public CompareJSON(JSONObject source, JSONObject matcher) {
		assertNotNull("source should not be null.", source);
		assertNotNull("matcher should not be null.", matcher);

		this.source = JSONObject.fromObject(source);
		this.matcher = JSONObject.fromObject(matcher);
	}
	
	public CompareJSON(JSONArray source, JSONArray matcher) {
		assertNotNull("source should not be null.", source);
		assertNotNull("matcher should not be null.", matcher);

		this.source = JSONArray.fromObject(source);
		this.matcher = JSONArray.fromObject(matcher);
	}
	
	public CompareJSON(Object source, Object matcher) {
		assertNotNull("source should not be null.", source);
		assertNotNull("matcher should not be null.", matcher);

		if (source instanceof JSONObject && matcher instanceof JSONObject) {
			this.source = JSONObject.fromObject(source);
			this.matcher = JSONObject.fromObject(matcher);
		} else if (source instanceof JSONArray && matcher instanceof JSONArray) {
			this.source = JSONArray.fromObject(source);
			this.matcher = JSONArray.fromObject(matcher);
		} else {
			fail("Passed Object cannot be converted to JSON.");
		}
	}*/

	public void include(List<String> includes) {
		this.includes = includes;
	}

	public void exclude(List<String> excludes) {
		this.excludes = excludes;
	}

	private void excludeFromJSON(final Object jsonObj, final String exclude) {
		String strExclude = exclude;
		int excludeIndex = -1;
		if (exclude.endsWith("]")) {
			strExclude = exclude.substring(0, exclude.indexOf("["));
			final String str = exclude.substring(exclude.indexOf("[") + 1, exclude.indexOf("]"));
			if (NumberUtils.isNumber(str)) {
				excludeIndex = NumberUtils.toInt(str);
			}
		}
		if (jsonObj instanceof net.minidev.json.JSONArray) {
			net.minidev.json.JSONArray jsonArr = (net.minidev.json.JSONArray) jsonObj;
			final int len = jsonArr.size();
			for (int i = 0; i < len; ++i) {
				final Object obj = jsonArr.get(i);
				if (obj instanceof JSONObject) {
					final JSONObject json = (JSONObject) obj;
					json.remove(exclude);
				}
			}
		} else if (jsonObj instanceof JSONObject) {
			final JSONObject json = (JSONObject) jsonObj;
			if (excludeIndex == -1) {
				json.remove(exclude);
			} else {
				final Object obj = json.get(strExclude);
				if (obj != null && obj instanceof JSONArray) {
					final JSONArray jArray = (JSONArray) obj;
					if (jArray.size() > excludeIndex) {
						jArray.remove(excludeIndex);
					}
				}
			}
		}
	}

	private void isExcludeEqual() {
		//	final String exclude = excludes.get(count);
		for (final String exclude : excludes) {
			if (StringUtils.isNotEmpty(exclude)) {
				final String parent = exclude.substring(0, exclude.lastIndexOf("."));
				final String child = exclude.substring(exclude.lastIndexOf(".") + 1);

				final Object oSource = JsonPath.read(source, parent);
				final Object oMatcher = JsonPath.read(matcher, parent);

				excludeFromJSON(oSource, child);
				excludeFromJSON(oMatcher, child);
			}
		}
		logger.info("source : " + source);
		logger.info("matcher : " + matcher);
	}

	private JSONCompareResult isIncludeEqual() {
		final JSONObject jsonSource = new JSONObject();
		final JSONObject jsonMatcher = new JSONObject();
		
		for (final String include : includes) {
			final Object oSource = JsonPath.read(source, include);
			final Object oMatcher = JsonPath.read(matcher, include);
			
			logger.info("source: " + oSource);
			logger.info("matcher: " + oMatcher);

			/*if (oSource != null && oMatcher != null) {
				jsonSource.put(include, oSource.toString());
				jsonMatcher.put(include, oMatcher.toString());
			}*/
			jsonSource.put(include, oSource);
			jsonMatcher.put(include, oMatcher);
		}
		return JSONCompare.compareJSON(jsonMatcher.toString(), jsonSource.toString(), JSONCompareMode.LENIENT);
	}

	public JSONCompareResult compare() {
		if (CollectionUtils.isNotEmpty(includes)) {
			return isIncludeEqual();
		} else if (CollectionUtils.isNotEmpty(excludes)) {
			isExcludeEqual();
		}
		return JSONCompare.compareJSON(matcher.toString(), source.toString(), JSONCompareMode.LENIENT);
	}

	/*public JSONObject[] getComparison(boolean matchType) {
		JSONObject[] results = new JSONObject[includes.length];
		for (int count = 0; count < includes.length; count += 2) {
			this.source = (JSONObject) JsonPath.read(source, includes[count]);
			// matcher path can be ignored if identical to source path
			if (includes[count + 1] == null) {
				includes[count + 1] = includes[count];
			}

			this.matcher = (JSONObject) JsonPath.read(matcher,
					includes[count + 1]);
			results[count] = compareJson(source, matcher, matchType);
		}
		return results;
	}

	private static JSONObject compareJson(JSONObject source,
			JSONObject matcher, boolean matchType) {
		JSONObject result = JSONObject.fromObject(source);
		Iterator<?> keys = source.keys();

		while (keys.hasNext()) {
			Object key = keys.next();
			Object value = source.get(key);

			// if object is another JSONObject
			if (value instanceof JSONObject) {

				// if matcher doesn't have the key
				// set the result to null
				if (!matcher.containsKey(key)) {
					result.put(key, null);
					return result;
				}

				// if matcher has the key
				// recursively evaluate
				result.put(
						key,
						compareJson((JSONObject) value,
								matcher.getJSONObject((String) key), matchType));
				continue;
			}

			if (value instanceof JSONArray) {
				JSONArray resultArray = new JSONArray();
				JSONArray sourceArray = (JSONArray) value;
				JSONArray matcherArray = matcher.optJSONArray((String) key);

				if (matcherArray == null) {
					result.put(key, null);
					return result;
				}

				for (int index = 0; index <= sourceArray.size(); index++) {
					resultArray.add(
							index,
							compareJson(sourceArray.getJSONObject(index),
									matcherArray.getJSONObject(index),
									matchType));
				}
				result.put(key, resultArray);
				continue;
			}
			if (matchType && value.getClass() != matcher.get(key).getClass()) {
				result.put(key, false);
			}
			result.put(key, value.equals(matcher.get(key)));
		}
		return result;
	}*/
}