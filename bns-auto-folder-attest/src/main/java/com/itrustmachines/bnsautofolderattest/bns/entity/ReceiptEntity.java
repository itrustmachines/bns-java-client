package com.itrustmachines.bnsautofolderattest.bns.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@DatabaseTable(tableName = "RECEIPT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiptEntity {
  
  public static final String ID_KEY = "ID";
  public static final String CALLER_ADDRESS_KEY = "CALLER_ADDRESS";
  public static final String TIMESTAMP_KEY = "TIMESTAMP";
  public static final String CMD_KEY = "CMD";
  public static final String INDEX_VALUE_KEY = "INDEX_VALUE";
  public static final String METADATA_KEY = "METADATA";
  public static final String CLEARANCE_ORDER_KEY = "CLEARANCE_ORDER";
  public static final String TIMESTAMP_SPO_KEY = "TIMESTAMP_SPO";
  public static final String RESULT_KEY = "RESULT";
  public static final String SIG_CLIENT_KEY = "SIG_CLIENT";
  public static final String SIG_SERVER_KEY = "SIG_SERVER";
  
  @DatabaseField(generatedId = true, columnName = ID_KEY)
  Long id;
  
  @DatabaseField(columnName = CALLER_ADDRESS_KEY, canBeNull = false)
  String callerAddress;
  
  @DatabaseField(columnName = TIMESTAMP_KEY, canBeNull = false)
  Long timestamp;
  
  @DatabaseField(columnName = CMD_KEY, canBeNull = false)
  String cmd;
  
  @DatabaseField(columnName = INDEX_VALUE_KEY, canBeNull = false)
  String indexValue;
  
  @DatabaseField(columnName = METADATA_KEY, canBeNull = false)
  String metadata;
  
  @DatabaseField(columnName = CLEARANCE_ORDER_KEY, canBeNull = false)
  Long clearanceOrder;
  
  @DatabaseField(columnName = TIMESTAMP_SPO_KEY, canBeNull = false)
  Long timestampSPO;
  
  @DatabaseField(columnName = RESULT_KEY, canBeNull = false)
  String result;
  
  @DatabaseField(columnName = SIG_CLIENT_KEY, foreign = true, foreignAutoRefresh = true)
  SigClientEntity sigClient;
  
  @DatabaseField(columnName = SIG_SERVER_KEY, foreign = true, foreignAutoRefresh = true)
  SigServerEntity sigServer;
  
}
