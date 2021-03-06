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


package org.anyline.config.db.sql.xml.impl;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.impl.BasicConditionChain;

public class XMLConditionChainImpl extends BasicConditionChain implements ConditionChain{

	public String getRunText(SQLCreater creater){
		initRunValue();
		StringBuilder builder = new StringBuilder();
		if(null != conditions){
			for(Condition condition: conditions){
				if(null == condition){
					continue;
				}
				if(condition.getVariableType() == VARIABLE_FLAG_TYPE_NONE){
					builder.append("\n\t");
					builder.append(condition.getRunText(creater));
				}else if(condition.isActive()){
					builder.append("\n\t");
					builder.append(condition.getRunText(creater));
					addRunValue(condition.getRunValues());
				}
			}
		}
		return builder.toString();
	}
	public void setValue(String name, Object value){
		if(null != conditions){
			for(Condition con:conditions){
				if(null == con){
					continue;
				}
				if(con.getId().equalsIgnoreCase(name)){
					con.setValue(name, value);
					break;
				}
			}
		}
	}
	/**
	 * 拼接查询条件
	 * @param builder
	 */
//	protected void appendCondition(StringBuilder builder){
//		if(null == chain){
//			return;
//		}
//		for(Condition condition: chain.getConditions()){
//			if(condition.getVariableType() == 2){
//				builder.append(condition.getRunText());
//			}else if(condition.isActive()){
//				builder.append(BR_TAB);
//				builder.append(condition.getRunText());
//				addRunValue(condition.getRunValues());
//			}
//		}
//	}

}
