Extract and rezip each (leaf) file in a given zipped directory tree to it's own separate zip file, while retaining the full directory path.

source.zip
dir1/dir2/file1
dir1/dir3/file2

file1.zip
dir1/dir2/file1

file2.zip
dir1/dir2/file2

Requires >java 1.8

Build with ```javac CreateZipsGui.java```
Package with ```jar cfm CreateZipsGui.jar MANIFEST.MF CreateZipsGui.class```

Run jar with ```java -jar CreateZipsGui.jar```
