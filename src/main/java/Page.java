class Page {
    private String pageSource;
    private String fatherPage;
    Page(String pageSource,String fatherPage){
        this.pageSource = pageSource;
        this.fatherPage=fatherPage;
    }

    public String getFatherPage() {
        return fatherPage;
    }

    public String getPageSource() {
        return pageSource;
    }
}
