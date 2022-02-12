package com.itrustmachines.bnsautofolderattest.bns.service;

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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiptService {
  
  private final Dao<SigClientEntity, Long> sigClientDao;
  private final Dao<SigServerEntity, Long> sigServerDao;
  private final Dao<ReceiptEntity, Long> receiptDao;
  
  @SneakyThrows
  public ReceiptService(@NonNull final JdbcPooledConnectionSource conn) {
    this.sigClientDao = DaoManager.createDao(conn, SigClientEntity.class);
    this.sigServerDao = DaoManager.createDao(conn, SigServerEntity.class);
    this.receiptDao = DaoManager.createDao(conn, ReceiptEntity.class);
    
    TableUtils.createTableIfNotExists(conn, SigClientEntity.class);
    TableUtils.createTableIfNotExists(conn, SigServerEntity.class);
    TableUtils.createTableIfNotExists(conn, ReceiptEntity.class);
    
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  private ReceiptEntity save(@NonNull final ReceiptEntity receiptEntity) {
    sigClientDao.createIfNotExists(receiptEntity.getSigClient());
    sigServerDao.createIfNotExists(receiptEntity.getSigServer());
    return receiptDao.createIfNotExists(receiptEntity);
  }
  
  public Receipt save(@NonNull final Receipt receipt) {
    final ReceiptEntity createEntity = save(ReceiptUtil.toEntity(receipt));
    return ReceiptUtil.toDomain(createEntity);
  }
  
  public List<Receipt> saveAll(@NonNull final List<Receipt> receipts) {
    return receipts.stream()
                   .map(this::save)
                   .collect(Collectors.toList());
  }
  
  @SneakyThrows
  public List<Receipt> findAll() {
    return receiptDao.queryForAll()
                     .stream()
                     .map(ReceiptUtil::toDomain)
                     .collect(Collectors.toList());
  }
  
  @SneakyThrows
  public List<Receipt> findAll(final int pageNumber, final int pageSize) {
    final QueryBuilder<ReceiptEntity, Long> queryBuilder = receiptDao.queryBuilder();
    queryBuilder.offset((long) (pageNumber * pageSize))
                .limit((long) pageSize);
    return queryBuilder.query()
                       .stream()
                       .map(ReceiptUtil::toDomain)
                       .collect(Collectors.toList());
  }
  
  @SneakyThrows
  public long countAll() {
    return receiptDao.countOf();
  }
  
  @SneakyThrows
  public Receipt findByLocator(@NonNull final ReceiptLocator receiptLocator) {
    final QueryBuilder<ReceiptEntity, Long> queryBuilder = receiptDao.queryBuilder();
    queryBuilder.where()
                .eq(ReceiptEntity.CLEARANCE_ORDER_KEY, receiptLocator.getClearanceOrder())
                .and()
                .eq(ReceiptEntity.INDEX_VALUE_KEY, receiptLocator.getIndexValue());
    final ReceiptEntity entity = queryBuilder.queryForFirst();
    return ReceiptUtil.toDomain(entity);
  }
  
  public List<Receipt> findByLocators(@NonNull final List<ReceiptLocator> receiptLocators) {
    return receiptLocators.stream()
                          .map(this::findByLocator)
                          .collect(Collectors.toList());
  }
  
  @SneakyThrows
  public int deleteByLocator(@NonNull final ReceiptLocator receiptLocator) {
    DeleteBuilder<ReceiptEntity, Long> deleteBuilder = receiptDao.deleteBuilder();
    deleteBuilder.where()
                 .eq(ReceiptEntity.INDEX_VALUE_KEY, receiptLocator.getIndexValue())
                 .and()
                 .eq(ReceiptEntity.CLEARANCE_ORDER_KEY, receiptLocator.getClearanceOrder());
    PreparedDelete<ReceiptEntity> preparedDelete = deleteBuilder.prepare();
    return receiptDao.delete(preparedDelete);
  }
  
  @SneakyThrows
  public int deleteAll() {
    final DeleteBuilder<ReceiptEntity, Long> deleteBuilder = receiptDao.deleteBuilder();
    deleteBuilder.where();
    final PreparedDelete<ReceiptEntity> preparedDelete = deleteBuilder.prepare();
    return receiptDao.delete(preparedDelete);
  }
  
  public int deleteByLocatorList(@NonNull final List<ReceiptLocator> receiptLocators) {
    return receiptLocators.stream()
                          .map(this::deleteByLocator)
                          .reduce(Integer::sum)
                          .orElse(0);
  }
  
}
