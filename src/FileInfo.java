// Class for storing file information
class FileInfo {
	// file name 
    String fileName;
    // Line count in the file
    long lineCount;
    // Running time for using stack
    long stackTime;
    // Running time for using deque
    long dequeTime;
    
    // Constructor for creating the file info
    public FileInfo(String fileName, long lineCount, long stackTime, long dequeTime) {
        this.fileName = fileName;
        this.lineCount = lineCount;
        this.stackTime = stackTime;
        this.dequeTime = dequeTime;
    }
}