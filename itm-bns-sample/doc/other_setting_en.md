## Configure the Setting of SPO Client

### About the Setting of SPO Client

To let the SPO Client operates more flexible, we allow developers to configure some settings in SPO Client. In this document, We are going to guide you all the setting in SPO Client and help you to configure the setting to meet your demanding.

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
  - [Dashboard_URL](#dashboard_url)
  - [LEDGER_INPUT_DELAY_SECOND](#ledger_input_delay_second)
  - [PROP_PATH_LIST](#prop_path_list)

- [Configuration file](#configuration-file-settings)
  - [VerifyBatchSize](#verifybatchsize)
  - [VerifyDelaySec](#verifydelaysec)
  - [RetryDelaySec](#retrydelaysec)

### Main Sample Code Settings

#### Sample_Properties

`SAMPLE_PROPERTIES` is the file name of configuration file. If you change the file name of configuration file, please remember to change the name in `SAMPLE_PROPERTIES`. The default file name of configuration file is [sample.properties](../src/main/resources/sample.properties)

- The settings of `SAMPLE_PROPERTIES`, pleaser refer to [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  public static String SAMPLE_PROPERTIES = "sample.properties";
  ```

#### JDBC_URL

In example code, we use database to store the receipt. You can change the path or name of database in here. The default database name is SpoDevice.db

- The settings of `JDBC_URL`, pleaser refer to [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  public static String JDBC_URL = "jdbc:sqlite:SpoDevice.db";
  ```

#### Dashboard_URL

This is the URL of ITM Dashboard. Please fill in the URL of ITM Dashboard in here.

- The settings of `Dashboard_URL`, pleaser refer to [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  public static String DASHBOARD_URL = "https://azure-prod-rinkeby.itm.monster:8443/";
  ```

#### LEDGER_INPUT_DELAY_SECOND

You can set the delay time of each ledgerInput. The default time is 3 milliseconds.

- The settings of `LEDGER_INPUT_DELAY_SECOND`, pleaser refer to [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)
  
  ```java
  public static int LEDGER_INPUT_DELAY_SECOND = 3;
  ```

#### PROP_PATH_LIST

SPO Client will use `PROP_PATH_LIST` to find the configuration file. If you store configuration file in other path, please remember to change the setting of `PROP_PATH_LIST` to ensure SPO Client can find your configuration file

- The settings of `PROP_PATH_LIST`, pleaser refer to [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)
  
  ```java
  public static final String[] PROP_PATH_LIST = new String[] { "./", "./src/main/resources/",
    "./itm-spo-sdk-java/itm-spo-sdk-sample/src/main/resources/", "./itm-spo-sdk-sample/src/main/resources/" };
  ```

### Configuration File Settings

To change the following setting, please refer to configuration file, [sample.properties](../src/main/resources/sample.properties)

#### verifyBatchSize

`verifyBatchSize` can set the number of receipt that SPO Client verify at a time

#### verifyDelaySec

`verifyDelaySec` can set the delay second in each verification

#### RetryDelaySec

SPO Client will delay second when the count of resend the request meet the maximum resend count. `RetryDelaySec` can set the delay second when SPO Client meet the maximum resend count.

----
Tutorials is now complete. Next, you can explore the overview of SPO Client

## Next Steps

Next Page : [Overview of SPO Client](./summary_en.md)
Last Page : [Build thte ReceiptDao Applications](./receiptDao_en.md)
