public class SharedObject {
    private boolean flag;
    private String file;

    public SharedObject() {
        this.flag = false;
        this.file = "";
    }

//    public SharedObject(String s){
//        file = s;
//    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean getFlag() {
        return flag;
    }
}
