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

public class MoreEntity {

	private Integer id;
	private String name;
	private String enter_task;
	private Integer enter_wifi;
	private Integer enter_sound;
	private Integer enter_soundMM;
	private Integer enter_bt;
	private String exit_task;
	private Integer exit_wifi;
	private Integer exit_sound;
	private Integer exit_soundMM;
	private Integer exit_bt;

	public MoreEntity() {
	}

	public Integer getEnter_soundMM() {
		return enter_soundMM;
	}

	public void setEnter_soundMM(Integer enter_soundMM) {
		this.enter_soundMM = enter_soundMM;
	}

	public Integer getExit_soundMM() {
		return exit_soundMM;
	}

	public void setExit_soundMM(Integer exit_soundMM) {
		this.exit_soundMM = exit_soundMM;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEnter_task() {
		return enter_task;
	}

	public void setEnter_task(String enter_task) {
		this.enter_task = enter_task;
	}

	public Integer getEnter_wifi() {
		return enter_wifi;
	}

	public void setEnter_wifi(Integer enter_wifi) {
		this.enter_wifi = enter_wifi;
	}

	public Integer getEnter_sound() {
		return enter_sound;
	}

	public void setEnter_sound(Integer enter_sound) {
		this.enter_sound = enter_sound;
	}

	public Integer getEnter_bt() {
		return enter_bt;
	}

	public void setEnter_bt(Integer enter_bt) {
		this.enter_bt = enter_bt;
	}

	public String getExit_task() {
		return exit_task;
	}

	public void setExit_task(String exit_task) {
		this.exit_task = exit_task;
	}

	public Integer getExit_wifi() {
		return exit_wifi;
	}

	public void setExit_wifi(Integer exit_wifi) {
		this.exit_wifi = exit_wifi;
	}

	public Integer getExit_sound() {
		return exit_sound;
	}

	public void setExit_sound(Integer exit_sound) {
		this.exit_sound = exit_sound;
	}

	public Integer getExit_bt() {
		return exit_bt;
	}

	public void setExit_bt(Integer exit_bt) {
		this.exit_bt = exit_bt;
	}

}
