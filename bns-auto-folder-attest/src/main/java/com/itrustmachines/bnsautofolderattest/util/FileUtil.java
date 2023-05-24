package com.itrustmachines.bnsautofolderattest.util;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.itrustmachines.bnsautofolderattest.exception.InitializationException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {
  
  public static String findFile(@NonNull final String[] propPathList) throws InitializationException {
    String filePath = null;
    for (final String propPath : propPathList) {
      if (Files.isRegularFile(Paths.get(propPath))) {
        filePath = propPath;
        break;
      }
    }
    if (filePath == null) {
      throw new InitializationException(propPathList[0] + " not found!");
    }
    return filePath;
  }
  
}
