package com.itrustmachines.bnsautofolderattest.util;

import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.NonNull;

public class FileUtil {
  
  public static String findFile(@NonNull final String fileName, @NonNull final String[] propPathList) {
    String filePath = null;
    for (final String propPath : propPathList) {
      final String propFilePath = propPath + fileName;
      if (Files.isRegularFile(Paths.get(propFilePath))) {
        filePath = propFilePath;
        break;
      }
    }
    if (filePath == null) {
      throw new RuntimeException(fileName + " not found!");
    }
    return filePath;
  }
  
}
