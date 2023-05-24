package com.itrustmachines.bnsautofolderattest.bns.service;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.itrustmachines.bnsautofolderattest.bns.entity.ReceiptEntity;
import com.itrustmachines.bnsautofolderattest.bns.entity.SigClientEntity;
import com.itrustmachines.bnsautofolderattest.bns.entity.SigServerEntity;
import com.itrustmachines.bnsautofolderattest.bns.util.ReceiptUtil;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.ReceiptLocator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiptService {
  
  private final Dao<SigClientEntity, Long> sigClientDao;
  private final Dao<SigServerEntity, Long> sigServerDao;
  private final Dao<ReceiptEntity, Long> receiptDao;
  
  public ReceiptService(@NonNull final JdbcPooledConnectionSource conn) throws SQLException {
    this.sigClientDao = DaoManager.createDao(conn, SigClientEntity.class);
    this.sigServerDao = DaoManager.createDao(conn, SigServerEntity.class);
    this.receiptDao = DaoManager.createDao(conn, ReceiptEntity.class);
    
    TableUtils.createTableIfNotExists(conn, SigClientEntity.class);
    TableUtils.createTableIfNotExists(conn, SigServerEntity.class);
    TableUtils.createTableIfNotExists(conn, ReceiptEntity.class);
    
    log.info("new instance={}", this);
  }
  
  @SuppressWarnings("UnusedReturnValue")
  private ReceiptEntity save(@NonNull final ReceiptEntity receiptEntity) throws SQLException {
    sigClientDao.createIfNotExists(receiptEntity.getSigClient());
    sigServerDao.createIfNotExists(receiptEntity.getSigServer());
    return receiptDao.createIfNotExists(receiptEntity);
  }
  
  public void save(@NonNull final Receipt receipt) throws SQLException {
    save(ReceiptUtil.toEntity(receipt));
  }
  
  public List<Receipt> findPageByNotVerifiedAndAddressEqualsIgnoreCaseAndClearanceOrderLessThenEqual(String address,
      long clearanceOrder, int pageNumber, int pageSize) throws SQLException {
    final QueryBuilder<ReceiptEntity, Long> queryBuilder = receiptDao.queryBuilder();
    queryBuilder.where()
                .like(ReceiptEntity.CALLER_ADDRESS_KEY, address)
                .and()
                .eq(ReceiptEntity.CLEARANCE_ORDER_KEY, clearanceOrder);
    queryBuilder.offset(((long) pageNumber * pageSize))
                .limit((long) pageSize);
    return queryBuilder.query()
                       .stream()
                       .map(ReceiptUtil::toDomain)
                       .collect(Collectors.toList());
  }
  
  public void deleteByLocator(@NonNull final ReceiptLocator receiptLocator) throws SQLException {
    DeleteBuilder<ReceiptEntity, Long> deleteBuilder = receiptDao.deleteBuilder();
    deleteBuilder.where()
                 .eq(ReceiptEntity.INDEX_VALUE_KEY, receiptLocator.getIndexValue())
                 .and()
                 .eq(ReceiptEntity.CLEARANCE_ORDER_KEY, receiptLocator.getClearanceOrder());
    PreparedDelete<ReceiptEntity> preparedDelete = deleteBuilder.prepare();
    receiptDao.delete(preparedDelete);
  }
  
}
