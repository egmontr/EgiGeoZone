/*
* Copyright 2014 - 2015 Egmont R. (egmontr@gmail.com)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/		

package de.egi.geofence.geozone.db;

public class RequirementsEntity {

	private Integer id;
	private String name;
	private String enter_bt;
	private String exit_bt;
	private boolean mon;
	private boolean tue;
	private boolean wed;
	private boolean thu;
	private boolean fri;
	private boolean sat;
	private boolean sun;
	
	public RequirementsEntity() {
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the enter_bt
	 */
	public String getEnter_bt() {
		return enter_bt;
	}

	/**
	 * @param enter_bt the enter_bt to set
	 */
	public void setEnter_bt(String enter_bt) {
		this.enter_bt = enter_bt;
	}

	/**
	 * @return the exit_bt
	 */
	public String getExit_bt() {
		return exit_bt;
	}

	/**
	 * @param exit_bt the exit_bt to set
	 */
	public void setExit_bt(String exit_bt) {
		this.exit_bt = exit_bt;
	}

	/**
	 * @return the mon
	 */
	public boolean isMon() {
		return mon;
	}

	/**
	 * @param mon the mon to set
	 */
	public void setMon(boolean mon) {
		this.mon = mon;
	}

	/**
	 * @return the tue
	 */
	public boolean isTue() {
		return tue;
	}

	/**
	 * @param tue the tue to set
	 */
	public void setTue(boolean tue) {
		this.tue = tue;
	}

	/**
	 * @return the wed
	 */
	public boolean isWed() {
		return wed;
	}

	/**
	 * @param wed the wed to set
	 */
	public void setWed(boolean wed) {
		this.wed = wed;
	}

	/**
	 * @return the thu
	 */
	public boolean isThu() {
		return thu;
	}

	/**
	 * @param thu the thu to set
	 */
	public void setThu(boolean thu) {
		this.thu = thu;
	}

	/**
	 * @return the fri
	 */
	public boolean isFri() {
		return fri;
	}

	/**
	 * @param fri the fri to set
	 */
	public void setFri(boolean fri) {
		this.fri = fri;
	}

	/**
	 * @return the sat
	 */
	public boolean isSat() {
		return sat;
	}

	/**
	 * @param sat the sat to set
	 */
	public void setSat(boolean sat) {
		this.sat = sat;
	}

	/**
	 * @return the sun
	 */
	public boolean isSun() {
		return sun;
	}

	/**
	 * @param sun the sun to set
	 */
	public void setSun(boolean sun) {
		this.sun = sun;
	}

}
