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
@Table(name = "tbl_region")
public class Region {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer regionNo;
	
	@Column(nullable = false)
	private String regionName;
	
	@OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
	private List<Group> groupList;
	
	@Override
	public String toString() {
	    return "Region{" +
	            "RegionNo=" + regionNo +
	            ", regionName='" + regionName + '\'' +
	            // 나머지 필드들...
	            '}';
	}
}
