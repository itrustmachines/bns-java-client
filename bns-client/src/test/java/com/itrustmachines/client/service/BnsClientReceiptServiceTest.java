package com.itrustmachines.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.itrustmachines.client.todo.BnsClientReceiptDao;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.ReceiptLocator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class BnsClientReceiptServiceTest {
  
  @Test
  public void test_SpoClientReceiptService_assert_receiptLocatorsMap() {
    // given
    int count = 10;
    final String indexValue = "JasonTest_R0";
    final BnsClientReceiptDao receiptDao = mock(BnsClientReceiptDao.class);
    final List<Receipt> receiptList1 = new ArrayList<>();
    final List<Receipt> receiptList2 = new ArrayList<>();
    for (long i = 1; i <= count / 2; i++) {
      final Receipt receipt = Receipt.builder()
                                     .clearanceOrder(i)
                                     .indexValue(indexValue)
                                     .build();
      receiptList1.add(receipt);
    }
    for (long i = count / 2 + 1; i <= count; i++) {
      final Receipt receipt = Receipt.builder()
                                     .clearanceOrder(i)
                                     .indexValue(indexValue)
                                     .build();
      receiptList2.add(receipt);
    }
    when(receiptDao.findAll(anyInt(), anyInt())).thenReturn(receiptList1)
                                                .thenReturn(receiptList2)
                                                .thenReturn(new ArrayList<>());
    
    // when
    final BnsClientReceiptService service = new BnsClientReceiptService(receiptDao);
    
    // then
    verify(receiptDao, timeout(5_000).times(2 + 1)).findAll(anyInt(), anyInt());
    final Map<Long, Set<String>> needVerifyReceiptLocatorMap = service.getNeedVerifyReceiptLocatorMap(count);
    assertThat(needVerifyReceiptLocatorMap).hasSize(count);
    for (long i = 1; i <= count; i++) {
      assertThat(needVerifyReceiptLocatorMap).containsKey(i);
      assertThat(needVerifyReceiptLocatorMap.get(i)).containsOnly(indexValue);
    }
  }
  
  @Test
  public void test_save_assert_receiptLocatorsMap() {
    // given
    int count = 10;
    final String indexValue = "JasonTest_R0";
    final BnsClientReceiptService service = new BnsClientReceiptService(mock(BnsClientReceiptDao.class));
    
    // when
    for (long i = 1; i <= count; i++) {
      final Receipt receipt = Receipt.builder()
                                     .clearanceOrder(i)
                                     .indexValue(indexValue)
                                     .build();
      service.save(receipt);
    }
    
    // then
    final Map<Long, Set<String>> needVerifyReceiptLocatorMap = service.getNeedVerifyReceiptLocatorMap(count);
    assertThat(needVerifyReceiptLocatorMap).hasSize(count);
    for (long i = 1; i <= count; i++) {
      assertThat(needVerifyReceiptLocatorMap).containsKey(i);
      assertThat(needVerifyReceiptLocatorMap.get(i)).containsOnly(indexValue);
    }
  }
  
  @Test
  public void test_saveAll_assert_receiptLocatorsMap() {
    // given
    int count = 10;
    final String indexValue = "JasonTest_R0";
    final BnsClientReceiptService service = new BnsClientReceiptService(mock(BnsClientReceiptDao.class));
    final List<Receipt> receiptList = new ArrayList<>();
    for (long i = 1; i <= count; i++) {
      final Receipt receipt = Receipt.builder()
                                     .clearanceOrder(i)
                                     .indexValue(indexValue)
                                     .build();
      receiptList.add(receipt);
    }
    
    // when
    service.saveAll(receiptList);
    
    // then
    final Map<Long, Set<String>> needVerifyReceiptLocatorMap = service.getNeedVerifyReceiptLocatorMap(count);
    assertThat(needVerifyReceiptLocatorMap).hasSize(count);
    for (long i = 1; i <= count; i++) {
      assertThat(needVerifyReceiptLocatorMap).containsKey(i);
      assertThat(needVerifyReceiptLocatorMap.get(i)).containsOnly(indexValue);
    }
  }
  
  @Test
  public void test_delete_assert_receiptLocatorsMap() {
    // given
    int count = 10;
    final String indexValue = "JasonTest_R0";
    final BnsClientReceiptService service = new BnsClientReceiptService(mock(BnsClientReceiptDao.class));
    final List<Receipt> receiptList = new ArrayList<>();
    for (long i = 1; i <= count; i++) {
      final Receipt receipt = Receipt.builder()
                                     .clearanceOrder(i)
                                     .indexValue(indexValue)
                                     .build();
      service.save(receipt);
      receiptList.add(receipt);
    }
    
    // when
    for (Receipt receipt : receiptList) {
      service.delete(receipt);
    }
    
    // then
    final Map<Long, Set<String>> needVerifyReceiptLocatorMap = service.getNeedVerifyReceiptLocatorMap(count);
    assertThat(needVerifyReceiptLocatorMap).hasSize(0);
  }
  
  @Test
  public void test_deleteAll_assert_receiptLocatorsMap() {
    // given
    int count = 10;
    final String indexValue = "JasonTest_R0";
    final BnsClientReceiptService service = new BnsClientReceiptService(mock(BnsClientReceiptDao.class));
    final List<Receipt> receiptList = new ArrayList<>();
    for (long i = 1; i <= count; i++) {
      final Receipt receipt = Receipt.builder()
                                     .clearanceOrder(i)
                                     .indexValue(indexValue)
                                     .build();
      service.save(receipt);
      receiptList.add(receipt);
    }
    
    // when
    service.deleteAll(receiptList);
    
    // then
    final Map<Long, Set<String>> needVerifyReceiptLocatorMap = service.getNeedVerifyReceiptLocatorMap(count);
    assertThat(needVerifyReceiptLocatorMap).hasSize(0);
  }
  
  @Test
  public void test_deleteAllByLocators_assert_receiptLocatorsMap() {
    // given
    int count = 10;
    final String indexValue = "JasonTest_R0";
    final BnsClientReceiptService service = new BnsClientReceiptService(mock(BnsClientReceiptDao.class));
    final List<ReceiptLocator> receiptLocatorList = new ArrayList<>();
    for (long i = 1; i <= count; i++) {
      final Receipt receipt = Receipt.builder()
                                     .clearanceOrder(i)
                                     .indexValue(indexValue)
                                     .build();
      final ReceiptLocator receiptLocator = ReceiptLocator.builder()
                                                          .clearanceOrder(receipt.getClearanceOrder())
                                                          .indexValue(receipt.getIndexValue())
                                                          .build();
      service.save(receipt);
      receiptLocatorList.add(receiptLocator);
    }
    
    // when
    service.deleteAllByLocators(receiptLocatorList);
    
    // then
    final Map<Long, Set<String>> needVerifyReceiptLocatorMap = service.getNeedVerifyReceiptLocatorMap(count);
    assertThat(needVerifyReceiptLocatorMap).hasSize(0);
  }
  
  @Test
  public void test_getNeedVerifyReceiptLocatorMap_assert_receiptLocatorsMap() {
    // given
    int count = 10;
    final String indexValue = "JasonTest_R0";
    final BnsClientReceiptService service = new BnsClientReceiptService(mock(BnsClientReceiptDao.class));
    for (long i = 1; i <= count; i++) {
      final Receipt receipt = Receipt.builder()
                                     .clearanceOrder(i)
                                     .indexValue(indexValue)
                                     .build();
      service.save(receipt);
    }
    
    // when
    final Map<Long, Set<String>> needVerifyReceiptLocatorMap = service.getNeedVerifyReceiptLocatorMap(count);
    
    // then
    assertThat(needVerifyReceiptLocatorMap).hasSize(count);
    for (long i = 1; i <= count; i++) {
      assertThat(needVerifyReceiptLocatorMap).containsKey(i);
      assertThat(needVerifyReceiptLocatorMap.get(i)).containsOnly(indexValue);
    }
  }
}