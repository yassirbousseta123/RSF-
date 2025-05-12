#!/bin/bash
cp src/main/java/com/rsf/controller/ImportController.java src/main/java/com/rsf/controller/ImportController.java.bak
sed -i '' 's/fieldMap.put("fieldName", def.getName());/fieldMap.put("fieldName", "Field_" + def.getStartIndex() + "_to_" + def.getEndIndex());/' src/main/java/com/rsf/controller/ImportController.java
