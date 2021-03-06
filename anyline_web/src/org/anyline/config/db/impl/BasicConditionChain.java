/* 
 * Copyright 2006-2015 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.config.db.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;

/**
 * 自动生成的参数
 * @author Administrator
 *
 */
public abstract class BasicConditionChain extends BasicCondition implements ConditionChain{
	private static final long serialVersionUID = 8226224203628527018L;
	protected List<Condition> conditions = new ArrayList<Condition>();
	protected int joinSize;
	
	public void init(){
		for(Condition condition:conditions){
			if(null == condition){
				continue;
			}
			condition.init();
		}
	}
	/**
	 * 附加条件
	 * @param condition
	 * @return
	 */
	public ConditionChain addCondition(Condition condition){
		conditions.add(condition);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	protected void addRunValue(Object value){
		if(null == value){
			return;
		}
		if(value instanceof Collection){
			runValues.addAll((Collection<Object>)value);
		}else{
			runValues.add(value);
		}
	}
	public List<Object> getRunValues(){
		return runValues;
	}
	public String getJoin(){
		return Condition.CONDITION_JOIN_TYPE_AND;
	}
	public int getJoinSize(){
		return joinSize;
	}
	public List<Condition> getConditions() {
		return conditions;
	}
	
}
