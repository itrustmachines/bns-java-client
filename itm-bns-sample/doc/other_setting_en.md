## Configure the Setting of BNS Client

### About the Setting of BNS Client

To let the BNS Client operates more flexible, we allow developers to configure some settings in BNS Client. In this document, We are going to guide you all the setting in BNS Client and help you to configure the setting to meet your demanding.

### Prerequisites

- Complete the quickstarts document
- Complete the CMD document
- Complete the Callback document
- Complete the ReceiptDao document

### Settings

There are 5 settings in Main Sample Code and 3 settings in configuration files

<!-- no toc -->
- [Main Sample Code](#main-sample-code-settings)
  - [Sample Properties](#sample_properties)
  - [JDBC_URL](#jdbc_url)
  - [LEDGER_INPUT_DELAY_SECOND](#ledger_input_delay_second)
  - [PROP_PATH_LIST](#prop_path_list)

- [Configuration file](#configuration-file-settings)
  - [VerifyBatchSize](#verifybatchsize)
  - [VerifyDelaySec](#verifydelaysec)
  - [RetryDelaySec](#retrydelaysec)

### Main Sample Code Settings

#### Sample_Properties

`SAMPLE_PROPERTIES` is the file name of configuration file. If you change the file name of configuration file, please remember to change the name `SAMPLE_PROPERTIES` in [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java).

- The settings of `SAMPLE_PROPERTIES`, pleaser refer to [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  public static String SAMPLE_PROPERTIES = "sample.properties";
  ```

#### JDBC_URL

In example code, we use database to store the receipt. You can change the path or name of database in here. The default database name is BnsDevice.db

- The settings of `JDBC_URL`, pleaser refer to [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  public static String JDBC_URL = "jdbc:sqlite:BnsDevice.db";
  ```

#### LEDGER_INPUT_DELAY_SECOND

You can set the delay time of each ledgerInput. The default time is 3 milliseconds.

- The settings of `LEDGER_INPUT_DELAY_SECOND`, pleaser refer to [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)
  
  ```java
  public static int LEDGER_INPUT_DELAY_SECOND = 3;
  ```

#### PROP_PATH_LIST

BNS Client will use `PROP_PATH_LIST` to find the configuration file. If you store configuration file in other path, please remember to change the setting of `PROP_PATH_LIST` to ensure BNS Client can find your configuration file

- The settings of `PROP_PATH_LIST`, pleaser refer to [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  public static final String[] PROP_PATH_LIST = new String[] { "./", "./src/main/resources/",
    "./itm-bns-java-client/itm-bns-sample/src/main/resources/", "./itm-bns-sample/src/main/resources/" };
  ```

### Configuration File Settings

To change the following setting, please refer to configuration file, [sample.properties](../src/main/resources/sample.properties)

#### verifyBatchSize

`verifyBatchSize` set how many receipts that BNS Client verify at a time.

#### verifyDelaySec

`verifyDelaySec` set the delay second between each verification.

#### RetryDelaySec

`RetryDelaySec` set the time duration between each retrying.

----
Tutorials are now complete. Next, you can explore the Auto Folder attestation

## Next Steps

Next Page : [BNS Auto folder attestation](./bns-auto-folder-attest_en.md)  
Last Page : [Build the ReceiptDao Applications](./receiptDao_en.md)
