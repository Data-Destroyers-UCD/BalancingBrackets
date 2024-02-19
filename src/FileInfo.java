class FileInfo {
    String fileName;
    long lineCount;
    long stackTime;
    long dequeTime;

    public FileInfo(String fileName, long lineCount, long stackTime, long dequeTime) {
        this.fileName = fileName;
        this.lineCount = lineCount;
        this.stackTime = stackTime;
        this.dequeTime = dequeTime;
    }
}