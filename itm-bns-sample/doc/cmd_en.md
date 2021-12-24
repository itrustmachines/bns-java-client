## Build the CMD

### About the CMD

In this document, we will teach you how to build the CMD. CMD is part of the ledgerinput. To attest your data, you need to convert your data into CMD. BNS Java Client will send the ledgerinput to BNS server. After BNS server clear your ledgerinput, you can follow the [check the result](./quick_start_en.md#5-check-the-result) section in quickstarts document to verify your proof. The developers should build your own CMD so that you can use our BNS. There are two scenarios to build the CMD:

1. [Text Attestation](#text-attestation) : Learn how to build CMD in JSON data type.

2. [File Attestation](#file-attestation) : Use Hash Utility to obtain the hash of your file and build the CMD

### Prerequisites

- Complete the quickstarts document

### Suggestion

- We highly recommend that you build the CMD in JSON data type

### Text Attestation

#### Example

We want to attest the solar panels data so we need to convert the solar panels data into CMD. In Java, we can use `Gson` to convert the data into JSON data type. Please check the following code in [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

- [Cmd.java](../src/main/java/com/itrustmachines/sample/Cmd.java)

  ```java
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public class Cmd {
    String deviceId;
    Long timestamp;
    Double watt;
  }
  ```

- [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final String deviceId = keyInfo.getAddress();
  final long timestamp = System.currentTimeMillis();
  final double watt = 15.00 + (Math.random() * 100 % 100 / 100);

  final double watt = 15.00 + (Math.random() * 100 % 100 / 100);
  final Cmd cmd = Cmd.builder()
                .deviceId(deviceId)
                .timestamp(timestamp)
                .watt(watt)
                .build();
  log.info("cmd create={}", cmd);
  cmdJson = new Gson().toJson(cmd);
  ```

- Now, you can implement your own CMD in [Cmd.java](../src/main/java/com/itrustmachines/sample/Cmd.java) and [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java).
- Then, follow the instructions in quickstarts document to execute the program. We highly recommended that you keep checking the ledgerinput records on [BNS Website](https://azure-dev-membership.itm.monster/) to make sure your code  can succesfully send attestation data to BNS server.

### File Attestation

#### Example

If we want to attest the file, we use `sha256` algorithm to obtain the hash value of file so that we can convert the file contents into CMD.

- [CmdBinary.java](../src/main/java/com/itrustmachines/sample/CmdBinary.java)

  ```java
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public class CmdBinary {

    String deviceId;
    Long timestamp;
    String fileName;
    String fileHash;
  }
  ```

- [BnsClientSample.java](../src/main/java/com/itrustmachines/sample/BnsClientSample.java)

  ```java
  final String deviceId = keyInfo.getAddress();
  final File imgFile = new File(filePath);
  final String fileName = imgFile.getName();
  final String binaryFileHash = HashUtils.sha256(imgFile);
  final CmdBinary cmdBinary = CmdBinary.builder()
            .deviceId(deviceId)
            .timestamp(timestamp)
            .fileName(fileName)
            .fileHash(binaryFileHash)
            .build();
  log.info("cmdBinary create={}", cmdBinary);
  cmdJson = new Gson().toJson(cmdBinary);
  ```

1. Follow the instructions in quickstart document and execute the code with `java -jar ./target/itm-bns-sample-1.1.1-SNAPSHOT.jar --file {filepath}`
2. Visit [BNS Website](https://azure-dev-membership.itm.monster/) to check the ledgerinput result

----
Build the CMD document is now complete. Next, learn how to build the Callback Applications

## Next Steps

Next Page : [Build the Callback Applications](./callback_en.md)  
Last Page : [Quick Starts](./quick_start_en.md)