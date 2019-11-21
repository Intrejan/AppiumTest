class Target {
    private String type;
    private String value;
    Target(String type,String value){
        this.type = type;
        this.value = value;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    String getType() {
        return type;
    }
    String getValue() {
        return value;
    }
}
