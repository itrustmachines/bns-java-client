package com.itrustmachines.bnsautofolderattest.service;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.itrustmachines.bnsautofolderattest.config.Config;
import com.itrustmachines.bnsautofolderattest.vo.AttestationRecord;
import com.itrustmachines.bnsautofolderattest.vo.AttestationStatus;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString(exclude = { "attestationRecordDao" })
@Slf4j
public class AttestationRecordService {
  
  private final Config config;
  private final Cache<String, AttestationRecord> attestedCache;
  private final Dao<AttestationRecord, Long> attestationRecordDao;
  private final ReadWriteLock lock;
  
  @SneakyThrows
  public AttestationRecordService(@NonNull final Config config) {
    this.config = config;
    final long duration = config.getScanDelay() * 5;
    this.attestedCache = CacheBuilder.newBuilder()
                                     .expireAfterAccess(duration > 60 ? duration : 60, TimeUnit.SECONDS)
                                     .build();
    final JdbcPooledConnectionSource conn = new JdbcPooledConnectionSource(config.getJdbcUrl());
    this.attestationRecordDao = DaoManager.createDao(conn, AttestationRecord.class);
    this.lock = new ReentrantReadWriteLock();
    
    TableUtils.createTableIfNotExists(conn, AttestationRecord.class);
    
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  public void save(@NonNull final AttestationRecord attestationRecord) {
    log.debug("save() attestationRecord={}", attestationRecord);
    lock.writeLock()
        .lock();
    try {
      attestationRecordDao.createOrUpdate(attestationRecord);
      if (!config.isDisableCache()) {
        if (!attestationRecord.getStatus()
                              .isAttested()) {
          return;
        }
        final AttestationRecord cacheAttestationRecord = attestedCache.getIfPresent(
            attestationRecord.getRelativeFilePathStr());
        // return if cache is newest, else update cache
        if (cacheAttestationRecord != null && cacheAttestationRecord.getId() > attestationRecord.getId()) {
          return;
        }
        attestedCache.put(attestationRecord.getRelativeFilePathStr(), attestationRecord);
      }
    } finally {
      lock.writeLock()
          .unlock();
    }
  }
  
  @SneakyThrows
  public AttestationRecord findById(@NonNull final Long id) {
    log.debug("findById() start, id={}", id);
    final QueryBuilder<AttestationRecord, Long> queryBuilder = attestationRecordDao.queryBuilder();
    queryBuilder.where()
                .eq(AttestationRecord.ID_KEY, id);
    lock.readLock()
        .lock();
    try {
      final AttestationRecord attestationRecord = queryBuilder.queryForFirst();
      log.debug("findLastAttestedByRelativePath() end, attestationRecord={}", attestationRecord);
      return attestationRecord;
    } finally {
      lock.readLock()
          .unlock();
    }
  }
  
  @SneakyThrows
  public AttestationRecord findLastAttestedByRelativePath(@NonNull final Path relativeFilePath) {
    log.debug("findLastAttestedByRelativePath() start, relativeFilePath={}", relativeFilePath);
    if (!config.isDisableCache()) {
      final AttestationRecord attestationRecord = attestedCache.getIfPresent(relativeFilePath.toString());
      if (attestationRecord != null) {
        log.debug("findLastAttestedByRelativePath() end, from cache, attestationRecord={}", attestationRecord);
        return attestationRecord;
      }
    }
    final QueryBuilder<AttestationRecord, Long> queryBuilder = attestationRecordDao.queryBuilder();
    final Where<AttestationRecord, Long> where = queryBuilder.where();
    where.eq(AttestationRecord.STATUS_KEY, AttestationStatus.ATTESTED);
    where.eq(AttestationRecord.STATUS_KEY, AttestationStatus.VERIFIED);
    where.eq(AttestationRecord.STATUS_KEY, AttestationStatus.VERIFY_FAIL);
    where.or(3);
    final SelectArg relativeFilePathArg = new SelectArg();
    relativeFilePathArg.setValue(relativeFilePath.toString());
    where.eq(AttestationRecord.RELATIVE_FILE_PATH_KEY, relativeFilePathArg);
    where.and(2);
    queryBuilder.orderBy(AttestationRecord.ATTEST_TIME_KEY, false);
    lock.readLock()
        .lock();
    try {
      final AttestationRecord attestationRecord = queryBuilder.queryForFirst();
      log.debug("findLastAttestedByRelativePath() end, attestationRecord={}", attestationRecord);
      return attestationRecord;
    } finally {
      lock.readLock()
          .unlock();
    }
  }
  
  @SneakyThrows
  public AttestationRecord findLastByCOAndIVAndStatus(@NonNull final Long clearanceOrder,
      @NonNull final String indexValue, @NonNull final AttestationStatus status) {
    log.debug("findLastByCOAndIVAndStatus() start, clearanceOrder={}, indexValue={}, status={}", clearanceOrder,
        indexValue, status);
    final QueryBuilder<AttestationRecord, Long> queryBuilder = attestationRecordDao.queryBuilder();
    final Where<AttestationRecord, Long> where = queryBuilder.where();
    where.eq(AttestationRecord.CLEARANCE_ORDER_KEY, clearanceOrder);
    final SelectArg indexValueArg = new SelectArg();
    indexValueArg.setValue(indexValue);
    where.eq(AttestationRecord.INDEX_VALUE_KEY, indexValueArg);
    where.eq(AttestationRecord.STATUS_KEY, status);
    where.and(3);
    queryBuilder.orderBy(AttestationRecord.ATTEST_TIME_KEY, false);
    lock.readLock()
        .lock();
    try {
      final AttestationRecord attestationRecord = queryBuilder.queryForFirst();
      log.debug("findLastByCOAndIVAndStatus() end, attestationRecord={}", attestationRecord);
      return attestationRecord;
    } finally {
      lock.readLock()
          .unlock();
    }
  }
  
}
