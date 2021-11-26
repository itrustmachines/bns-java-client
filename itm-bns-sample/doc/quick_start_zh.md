## 快速開始

### 關於快速開始

使用我們的 BNS Java Client SDK 需要:

- ITM BNS JAVA Client SDK
- 執行 Windows 10, MacOS, Linux 的機器

根據您的作業系統，BNS JAVA Client SDK 支援下列數種開發環境

- Visual Studio Code (VSCode)。但您需要安裝 VSCcode [Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) 延伸套件，才能讓您在 VScode 上執行並開發我們的 SDK

- Command Line Interface (CLI)。

- IntelliJ IDEA

此份快速開始文件將透過下列 5 個步驟引導您開始使用我們的 SDK

<!-- no toc -->
1. [安裝所需套件](#1-安裝所需套件)
2. [下載 SDK](#2-下載-sdk)
3. [修改 SDK 設定檔](#3-修改-sdk-設定檔)
4. [執行範例程式](#4-執行範例程式)
5. [確認執行結果](#5-確認執行結果)

完成上述步驟後，您就可以參考教學文件中的範例整合及開發我們的 SDK。

### 1. 安裝所需套件

- 執行此 Java SDK 前，需先安 Java 8 以上的版本以及 `Maven`
- 若機器已經安裝上述套件，請透過 `java --version` 確認 java 版本為 8 以上
- 若無請用戶參考 [Java 官方網站](https://www.oracle.com/java/technologies/javase-downloads.html) 以及 [Maven 官方網站](https://maven.apache.org) 進行安裝或更新

### 2. 下載 SDK

- 下載 BNS JAVA Client SDK

  ```shell
  git clone https://github.com/itrustmachines/bns-java-client.git
  ```

### 3. 修改 SDK 設定檔

- 範例主程式 [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java) 中會使用此設定檔進行私鑰、區塊鏈位址、電子郵件 ... 等設定，**本設定檔內容十分重要，請依照下方說明實作，再執行範例程式**

- **在修改設定檔前，請先依照下方三點說明建立執行環境並取得私鑰以及區塊鏈位址**

  1. 在您的 .m2 資料夾中建立 `settings.xml` 檔，並將下列內容複製貼上。
    ```xml
    <settings>
      <servers>
        <server>
          <id>kuro-nexus-releases</id>
          <username>guest</username>
          <password>guest</password>
        </server>
      </servers>
    </settings>
    ```
  
3. 為了確保每個資料來源的可信度，所以需要一組專屬私鑰進行數位簽章，避免他人冒用，**每個資料來源的私鑰必須唯一**。私鑰請至 MetaMask 帳戶中輸出

4. 我們使用以太坊 Rinkeby 測試鏈作為測試環境。為了向 Rinkeby 測試鏈取得鏈上證據，所以需要 Rinkeby 測試鏈節點位址。Rinkeby 測試鏈位址可透過 Infura 取得，請參考下方 Infura 教學連結
   
     - [Infura 教學](./infura_zh.md)

- 取得私鑰及 Rinkey 測試鏈位址後，我們可以開始修改設定檔 [sample.properties](../src/main/resources/sample.properties) ，修改方式請參考下方說明 :

    ```Java
    /**
     * 請按照描述依序填入，詳細資訊請參考下方說明
     * 填入由 MetaMask 匯出的 32 Bytes (128 bits) Hex 編碼專屬私鑰
     */ 
    privateKey=

    /** 填入 BNS Server 的 URL */
    bnsServerUrl=https://azure-dev-membership.itm.monster:8088/
    
    /** 
     * 我們提供的測試環境使用以太坊 Rinkeby 測試鏈
     * 請確認此節點位址屬於 Rinkeby 測試鏈的節點位址，再填入 
     * 節點位址取得方式，請見上方說明
     */
    nodeUrl=https://rinkeby.infura.io/v3/{InfuraProjectId}
  
    /**
     * 請填入您的電子郵件信箱
     */
    email=  
  
    /** 驗證會以分頁索取回條進行驗證，填寫此值設定分頁一次拿幾筆驗證， 預設為 10 筆 */
    verifyBatchSize=10

    /** 驗證會以分頁索取回條進行驗證，填寫此值設定每次驗證之間的時間間隔， 預設為 1 秒 */
    verifyDelaySec=1

    /** 當遇到網路問題無法順利得到 response， Client將會重試Request， 填寫此值設定每次嘗試的間隔時間， 預設為 5 秒 */
    retryDelaySec=5
    ```

### 4. 執行範例程式

#### Command Line Interface
```shell
  # 切換至 itm-spo-sdk-sample 資料夾
  $ cd bns-java-client/itm-bns-sample

  # 執行主程式，將存證內容 LedgerInput 傳送至 SPO Server
  $ mvn clean package
  $ java -jar ./target/itm-bns-sample-1.1.1-SNAPSHOT.jar
```
**如果您的 Maven 版本高於 3.8.1 版，maven 預設會阻擋 HTTP repo，請參考 [此連結](https://stackoverflow.com/questions/67001968/how-to-disable-maven-blocking-external-http-repositores) 解決方式**
#### Visual Studio Code

1. 開啟命令例
2. 輸入 Maven 並選擇 `Maven : Execute Commands`
3. 選擇 itm-spo-sdk-sample 專案
4. 選擇 Custom 並輸入 `mvn clean package` 編譯程式
5. 執行 `./target/itm-bns-sample-1.1.1-SNAPSHOT.jar`

#### Intellij IDEA

1. 開啟 itm-spo-sdk-java with Maven Project
2. 點選右邊側欄的 Maven Tools 選擇 execute maven goal
3. 輸入 `mvn clean package` 並執行
4. 左邊側欄檔案系統中，右鍵點選的位在 `target` 資料夾中的 `itm-bns-sample-1.1.1-SNAPSHOT.jar` 執行程式

### 5. 確認執行結果

執行範例程式後，可透過 [BNS Website](https://azure-dev-membership.itm.monster:8088/) 確認範例程式能否將資料傳送至 BNS Server 進行存證上鏈。確認步驟如下 :

  1. 用瀏覽器開啟 [BNS Website](https://azure-dev-membership.itm.monster:8088/)
  2. 點擊 Check Records，您便可以查看所有的 ledgerinputs
  3. 選擇其中一個 ledgerinput 的 '+' 圖示，您可以查看該次 legerinput 的所有資訊
  5. 若該筆資料的 Status 顯示綠色 Success 則表示資料已上鏈，反之為灰色 Waiting 等待上鏈中
  6. 資料狀態若為**綠色**已上鏈，可點擊 On-Chain Proof 連接至區塊鏈瀏覽器查看該筆資料在區塊鏈上的證據
  7. 資料狀態若為**綠色**已上鏈，可點擊 Off-Chain Proof 下載該筆資料的鏈下證據，該證據可以透過公開的 [Verification Server](https://verification.itrustmachines.com/) 或自行整合的 [Verification Program](https://github.com/itrustmachines/spo-verification-program) 來驗證資料
  8. 點選 verification 圖示
  9. 點選 Proof verification
  10. 上傳剛下載的 Off-chain proof
  11. 如果 Off-Chain proof 被篡改，verify result icon 會是綠色，否則，會是灰色

您已完成快速開始文件中所有教學步驟，並且已經成功將此 SDK 整合至您的裝置，接下來我們會引導你如何將想存證的資料設計成 CMD 傳送至 BNS Server

----

- [下一頁 : 設計存證內容 CMD](./cmd_zh.md)
- [上一頁 : 首頁](../../README_ZH.md)
