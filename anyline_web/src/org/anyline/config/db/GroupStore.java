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


package org.anyline.config.db;

import java.io.Serializable;
import java.util.List;

public interface GroupStore extends Serializable{
	public List<Group> getGroups();
	public void group(Group group) ;
	/**
	 * 排序多列以,分隔
	 * gropu("CD");
	 * group("CD,NM");
	 */
	public void group(String str) ;

	public Group getGroup(String group);
	public String getRunText(String disKey);
	public void clear();
}