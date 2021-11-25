# SpoClientReceiptDao介面方法說明

開發者需實作介面中的方法才能夠進行資料驗證，否則驗證將會失敗。各方法行為說明如下：

`boolean save(Receipt receipt);`
- 儲存回條
----
`boolean saveAll(List<Receipt> receipts);`
- 儲存傳入的List中所有的回條
----
`List<Receipt> findAll();`
- 找出所有儲存在資料庫當中的回條
----
`List<Receipt> findAll(int pageNumber, int pageSize);`
- 根據分頁找出儲存在資料庫中的回條
----
`long countAll();`
- 計算儲存在資料庫中所有的回條數量
----
`Receipt findByLocator(ReceiptLocator receiptLocator);`
- 利用ReceiptLocator找到對應的回條
- ReceiptLocator由Receipt當中的ClearanceOrder與IndexValue組成
- ClearanceOrder與IndexValue組合而成的值會是唯一值
----
`List<Receipt> findByLocators(List<ReceiptLocator> receiptLocators);`
- 利用ReceiptLocatorList找到對應的多個回條
- ReceiptLocator由Receipt當中的ClearanceOrder與IndexValue組成
- ClearanceOrder與IndexValue組合而成的值會是唯一值
----
`boolean delete(Receipt receipt);`
- 刪除回條
- 裝置驗證資料完成後會將回條刪除
- 若開發者需要另外留存回條，可以自行實作方法於回條儲存時另做備份
----  
`boolean deleteAll(List<Receipt> receipts);`
- 根據Receipt List，將對應的回條刪除
- 裝置驗證資料完成後會將回條刪除
- 若開發者需要另外留存回條，可以自行實作方法於回條儲存時另做備份
---- 
`boolean deleteAllByLocators(List<ReceiptLocator> receiptLocators);`
- 根據多個ReceiptLocator，將對應的回條刪除
- 裝置驗證資料完成後會將回條刪除
- 若開發者需要另外留存回條，可以自行實作方法於回條儲存時另做備份
- ReceiptLocator由Receipt當中的ClearanceOrder與IndexValue組成
- ClearanceOrder與IndexValue組合而成的值會是唯一值