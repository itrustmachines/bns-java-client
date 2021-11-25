# ITM SPO JAVA SDK 使用手冊

## 簡介

此使用手冊內將使用 `itm-spo-sdk-sample` 作為範例引導您了解並整合此 SDK。手冊中包含 SDK 基本設定、SDK 各功能整合實作說明以及主程式運作流程。在開始閱讀手冊前，我們希望您先了解 SPO Client 與 SPO Server 基本的[系統架構](https://github.com/itrustmachines/itm-spo-sdk-doc)，若您已經閱讀過並了解系統架構，您可以前網快速開始文件安裝並執行 SDK 範例程式

## 開始使用 JAVA SDK

### 快速開始

- 在[快速開始](./doc/quick_start_zh.md)文件中我們將透過數個步驟引導您使用我們的 SDK
  - [安裝所需套件](./doc/quick_start_zh.md#1-安裝所需套件)
  - [下載 SDK](./doc/quick_start_zh.md#2-下載-sdk)
  - [修改 SDK 的設定檔](./doc/quick_start_zh.md#3-修改-sdk-設定檔)
  - [執行範例程式](./doc/quick_start_zh.md#4-執行範例程式)
  - [確認執行結果](./doc/quick_start_zh.md#5-確認執行結果)

- **若您已經完成快速開始文件中的每個步驟，您可以參考教學文件中的範例整合及開發我們的 SDK。**

### 教學

- 在教學文件中，我們將引導您了解並實作 SPO Client 中的設定以及服務
  - [存證內容 CMD 設計教學](./doc/cmd_zh.md)
  - [Callback 串接實作教學](./doc/callback_zh.md)
  - [ReceiptDao 功能教學](./doc/receiptDao_zh.md)
  - [SPO Client 其他設定教學](./doc/other_setting_zh.md)

- **若您已經完成教學文件中的每個步驟，您可以參考前往主程式流程說明，了解程式詳細的運作流程。**

## 主程式流程說明

- 在主程式流程說明文件中，我們會統整前面文件的內容並詳細解釋程式運作流程，在閱讀主程式流程說明前，我們建議您先閱讀完前面的快速開始以及教學文件，再閱讀此份文件
- [主程式流程說明](./doc/summary_zh.md)

## Release Note

### 2.2.1

- Add `close()` method: developers can now close spo-client instance completely to prevent left over thread or database connection

### 2.1.0.RELEASE

- Verify receipts is now doing asynchronously
- Add retry delay, set `retryDelaySec` in properties file
- Add verify setting, set `verifyBatchSize` & `verifyDelaySec` in properties file
- Change `ReceiptDao` method

## Release Note
- [Release Note](./doc/release_note.md)