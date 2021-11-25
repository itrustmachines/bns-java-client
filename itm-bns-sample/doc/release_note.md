## Release Note

### 2.2.1

- Add `close()` method: developers can now close spo-client instance completely to prevent left over thread or database connection

### 2.1.0.RELEASE

- Verify receipts is now doing asynchronously
- Add retry delay, set `retryDelaySec` in properties file
- Add verify setting, set `verifyBatchSize` & `verifyDelaySec` in properties file
- Change `ReceiptDao` method

