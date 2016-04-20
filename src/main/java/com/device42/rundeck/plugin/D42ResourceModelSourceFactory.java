/*
 * Copyright 2011 DTO Solutions, Inc. (http://dtosolutions.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* EC2ResourceModelSource.java
* 
* User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: 9/1/11 4:27 PM
* 
*/
package com.device42.rundeck.plugin;

import com.device42.client.model.Device;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.*;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Plugin(name = "d42", service = "ResourceModelSource")
public class D42ResourceModelSourceFactory implements ResourceModelSourceFactory, Describable {
	public static final String PROVIDER_NAME = "d42";

	public static final String SERVER_URL = "serverUrl";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String FILTER_PARAMS = "filter";
	public static final String REFRESH_INTERVAL = "refreshInterval";

	public static final int GROUPS_AMOUNT = 5;

	public static final String FILTER_KEY_PREFIX = "filterKey";
	public static final String FILTER_VALUE_PREFIX = "filterValue";
	public static final String FILTER_GROUP_PREFIX = "Filter ";
	public static final String[] FILTER_KEYS = { "tags", "os", "service_level", "customer" };

	public static List<Device> list;

	public D42ResourceModelSourceFactory() {

	}

	public ResourceModelSource createResourceModelSource(final Properties properties) throws ConfigurationException {
		final D42ResourceModelSource d42ResourceModelSource = new D42ResourceModelSource(properties);
		d42ResourceModelSource.validate();
		return d42ResourceModelSource;
	}

	private Map<String, Object> getGroupRenderingOptions(String groupName) {
		Map<String, Object> renderingOptions = new HashMap<String, Object>();
		renderingOptions.put("groupName", groupName);
		renderingOptions.put("grouping", "secondary");
		return renderingOptions;
	}

	private void addFilterGroup(DescriptionBuilder descBuilder, int index) {
		String groupName = FILTER_GROUP_PREFIX + index;
		Map<String, Object> renderingOptions = getGroupRenderingOptions(groupName);
		descBuilder.property(PropertyUtil.freeSelect(FILTER_KEY_PREFIX + index,
				"Filter Key", "Please select the key for the filtering from a list or enter your key id", false, null,
				Arrays.asList(FILTER_KEYS), null, null, renderingOptions));
		descBuilder.property(PropertyUtil.string(FILTER_VALUE_PREFIX + index, "Filter Value",
				"Please enter the value you want to filter for", false, null, null, null, renderingOptions));

	}

	public Description getDescription() {
		DescriptionBuilder descBuilder = DescriptionBuilder.builder()
				.name(PROVIDER_NAME)
				.title("Rundeck Resources")
				.description("Devices from d42")
				.property(PropertyUtil.string(SERVER_URL, "API Url", "", false, null))
				.property(PropertyUtil.string(USERNAME, "Username", "D42 console username", false, null))
				.property(
						PropertyUtil.string(
								PASSWORD,
								"Password",
								"D42 console password",
								false,
								null,
								null,
								null,
								Collections.singletonMap("displayType",
										(Object) StringRenderingConstants.DisplayType.PASSWORD)))
				.property(PropertyUtil.integer(REFRESH_INTERVAL, "Refresh Interval",
						"Minimum time in seconds between API requests to Device42 (default is 30)", false, "30",
						new PropertyValidator() {
							public boolean isValid(final String value) throws ValidationException {
								try {
									if (null == value || value.length() == 0)
										return true;
									int num = Integer.parseInt(value);
									if (num <= 0)
										throw new ValidationException(D42ResourceModelSourceFactory.REFRESH_INTERVAL
												+ " value is not valid: " + value);
								} catch (NumberFormatException e) {
									throw new ValidationException(D42ResourceModelSourceFactory.REFRESH_INTERVAL
											+ " value is not valid: " + value);
								}
								return true;
							}
						}));
		for (int i = 1; i <= GROUPS_AMOUNT; i++) {
			addFilterGroup(descBuilder, i);
		}
		descBuilder.property(PropertyUtil.string(FILTER_PARAMS, "Filter Params", "D42 filter params", false, null, null,
				null, getGroupRenderingOptions("Filter String")));

		return descBuilder.build();
	}
}