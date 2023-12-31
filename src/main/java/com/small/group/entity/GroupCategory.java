package com.small.group.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name = "tbl_group_category")
public class GroupCategory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer groupCategoryNo;
	
	@Column(nullable = false)
	private String groupCategoryName;
	
	@OneToMany(mappedBy = "groupCategory", fetch = FetchType.LAZY)
	private List<Group> groupList;
	
	@Override
	public String toString() {
	    return "GroupCategory{" +
	            "groupCategoryNo=" + groupCategoryNo +
	            ", groupCategoryName='" + groupCategoryName + '\'' +
	            // 나머지 필드들...
	            '}';
	}

}
