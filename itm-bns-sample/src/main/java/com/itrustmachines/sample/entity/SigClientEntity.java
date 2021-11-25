package com.itrustmachines.sample.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@DatabaseTable(tableName = "SIG_CLIENT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SigClientEntity {
  
  @DatabaseField(generatedId = true)
  Long id;
  
  @DatabaseField(columnName = "R", canBeNull = false)
  String r;
  
  @DatabaseField(columnName = "S", canBeNull = false)
  String s;
  
  @DatabaseField(columnName = "V", canBeNull = false)
  String v;
  
}
