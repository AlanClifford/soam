/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soam.model.specification;

import com.soam.model.SoamEntity;
import com.soam.model.stakeholder.Stakeholder;
import jakarta.persistence.*;
import org.springframework.core.style.ToStringCreator;

import java.util.List;
import java.util.Optional;

/**
 * Simple JavaBean domain object representing a specification.
 */
@Entity
@Table(name = "specifications")
public class Specification extends SoamEntity {

	@OneToMany( fetch = FetchType.EAGER, mappedBy = "specification")
	@OrderBy("name")
	private List<Stakeholder> stakeholders;

	public List<Stakeholder> getStakeholders() {
		return this.stakeholders;
	}

	public void setStakeholders(List<Stakeholder> stakeholders) {
		this.stakeholders = stakeholders;
	}

	public void addStakeholder(Stakeholder stakeholder) {
		if (stakeholder.isNew()) {
			getStakeholders().add(stakeholder);
		}
	}

	/**
	 * Return an Optional Stakeholder with the given name
	 * @param name to test
	 * @return an Optional Stakeholder if stakeholder name is already in use
	 */
	public Optional<Stakeholder> getStakeholder(String name) {
		return getStakeholder(name, false);
	}

	/**
	 * Return an Optional Stakeholder with the given id
	 * @param id to test
	 * @return an Optional Stakeholder if stakeholder id is already in use
	 */
	public Optional<Stakeholder> getStakeholder(Integer id) {
		for (Stakeholder stakeholder : getStakeholders()) {
			if (!stakeholder.isNew()) {
				Integer compId = stakeholder.getId();
				if (compId.equals(id)) {
					return Optional.of(stakeholder);
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Return an Stakeholder with the given name. If the id is blanks, skip
	 * @param name to test
	 * @param ignoreNew if set to true, do not return unsaved stakeholders
	 * @return an optional pet if pet name is already in use
	 */
	public Optional<Stakeholder> getStakeholder(String name, boolean ignoreNew) {
		name = name.toLowerCase();
		for (Stakeholder stakeholder : getStakeholders()) {
			if (!ignoreNew || !stakeholder.isNew()) {
				String compName = stakeholder.getName();
				compName = compName == null ? "" : compName.toLowerCase();
				if (compName.equals(name)) {
					return Optional.of(stakeholder);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId()).append("new", this.isNew())
				.append("name", this.getName())
				.append("description", this.getDescription()).append("notes", this.getNotes())
				.toString();
	}
}
