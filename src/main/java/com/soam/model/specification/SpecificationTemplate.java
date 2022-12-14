package com.soam.model.specification;

import com.soam.model.SoamEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table( name="specification_templates", uniqueConstraints = {@UniqueConstraint(columnNames = { "name"})})
public class SpecificationTemplate extends SoamEntity {

}
