## Build the CMD

### About the CMD

CMD is a data you want to clearance to blockchain by SPO Server. CMD is also part of ledgerinput. SPO Client send the ledgerinput to SPO Server and SPO Server will automatically upload the fingerprint of ledgerinput to blockchain. The developers should build your own CMD so that you can use our SPO service. There are two scenarios to build the CMD:

1. [Display on Dashboard](#display-on-dashboard) : Learn how to build CMD in JSON data type and display on ITM Dashboard.

2. [Use verification server to audit the file](#use-verification-server-to-verify-the-file) : Guide you through the steps to convert the file to CMD and use our verification server to audit the file.

### Prerequisites

- Complete the quickstarts document

### Some suggestions

- We highly recommand that you build the CMD in JSON data type
- If you want to display on ITM dashboard, `timestamp` and `indexValue` must be included in CMD
- The maximum length of CMD is 1000 characters

### Display on Dashboard

- If we want to clearance the solar panel data, we need to add the solar panel's data in CMD and use `Gson` to covert the string into JSON data type. Please check the following code in [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

- [Cmd.java](../src/main/java/com/itrustmachines/sample/Cmd.java)

  ```java
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public class Cmd {
    String deviceId;
    Long timestamp;
    Double voltage;
    Double current;
    Double watt;
  }
  ```

- [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final String deviceId = spoClient.getConfig().getIndexValueKey();
  final long timestamp = System.currentTimeMillis();
  final double watt = 15.00 + (Math.random() * 100 % 100 / 100);
  final double voltage = 1000.00;
  final double current = 9999.99;

  final Cmd cmd = Cmd.builder()
                   .deviceId(deviceId)
                   .timestamp(timestamp)
                   .watt(watt)
                   .voltage(voltage)
                   .current(current)
                   .build();
  log.info("cmd create={}", cmd);
  final String cmdJson = new Gson().toJson(cmd);
  ```

- Now, you can implement your own CMD in [Cmd.java](../src/main/java/com/itrustmachines/sample/Cmd.java) and [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java). Then, follow the instructions in quickstarts document to execute the program. We highly recommend that you keep checking the ledgerinput result on ITM Dashboard until CMD is successfully On-chain.

  ![](../image/cmd_example_easy.png)

### Use verification server to verify the file

If we want to clearance the file, we need to convert the file contents into CMD and send to SPO Server. This implementation involved the following steps:

1. Add two addition columns in CMD, one is `fileHash`, the other is `fileName`. The `fileHash` is the hash value of file contents. You can use the SHA256 algorithm that we provided to get the hash value of the file contents. In addition to these 4 columns ( `timestamp`, `indexValue`, `fileName`, `fileHash` ), you can add more columns you want but make sure the CMD is less than 1000 characters.

     - **Please remember to use `binaryLedgerInput` in [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java) and implement the code in `obtainBinaryLedgerInputResponse` callback method which we will discuss in next topic**

     - For the code of SHA256 algorithm, please refer to [HashUtils.java](../../spo-common-domain-objects/src/main/java/com/itrustmachines/common/util/HashUtils.java)

     - [Cmd.java](../src/main/java/com/itrustmachines/sample/Cmd.java)

       ```java
       @Data
       @AllArgsConstructor
       @NoArgsConstructor
       @Builder
       public class Cmd {
         String deviceId;
         Long timestamp;
         String binaryFileName;
         String binaryFileHash;
       }
       ```

     - [SpoClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

       ```java


       final String deviceId = spoClient.getConfig().getIndexValueKey();
       final long timestamp = System.currentTimeMillis();
       final String imgFileName = "_MG_2293.jpg";
       final String imgFilePath = "./src/main/java/com/itrustmachines/sample/" + imgFileName;

       File imgFile = new File(imgFilePath);
      
       final String imgFileHash = HashUtils.sha256(imgFile);
      

       final Cmd cmd = Cmd.builder()
                          .deviceId(deviceId)
                          .timestamp(timestamp)
                          .binaryFileName(imgFileName)
                          .binaryFileHash(imgFileHash)
                          .build();
       log.info("cmd create={}", cmd);
       final String cmdJson = new Gson().toJson(cmd);
       final BinaryLedgerInputResponse binaryLedgerInputResponse = spoClient.binaryLedgerInput(cmdJson, imgFile.toPath());
       log.info("ledger input result={}", binaryLedgerInputResponse);
       ```

2. Follow the instructions in quickstart document to execute the code.
3. Go to [ITM Dashboard Website](https://azure-prod-rinkeby.itm.monster:8443)
4. Click the Photos in left side bar
5. Click the image you upload to check the CMD and status
6. If status of image is On-chain, you can download Off-Chain Proof and use our verification server to audit the file
7. Go to ITM Verification Server
8. Click Raw Data Verification button
9. Upload the image and Off-chain Proof then you can check whether the file is temper-free or not.

----
Build the CMD document is now complete. Next, learn how to build the Callback Applications

## Next Steps

Next Page : [Build the Callback Applications](./callback_en.md)
Last Page : [Quick Starts](./quick_start_en.md)