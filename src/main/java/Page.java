/**
 * 页面对象，包含当前页面和父页面
 */
class Page {
    private String pageSource;
    private String fatherPage;
    Page(String pageSource,String fatherPage){
        this.pageSource = pageSource;
        this.fatherPage=fatherPage;
    }

    String getFatherPage() {
        return fatherPage;
    }

    String getPageSource() {
        return pageSource;
    }
}
