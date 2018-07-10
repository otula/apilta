/**
 * Copyright 2014 Tampere University of Technology, Pori Department
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package core.tut.pori.context;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

/**
 * Simple bean name generator, that returns the full canonical name of the class as the bean name.
 *
 * This class is used to produce unique bean names to prevent name collisions when instantiating multiple beans with the same class name, but different package.
 */
public class CoreBeanNameGenerator implements BeanNameGenerator{
	private static final Logger LOGGER = Logger.getLogger(CoreBeanNameGenerator.class);

	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		String name = definition.getBeanClassName();
		LOGGER.debug("Created bean with name: "+name);
		return name;
	}
}
