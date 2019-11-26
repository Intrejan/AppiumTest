/**
 * xml每个节点对象，包含以下属性
 */
class Attrs {
    private String text;
    private String c_lass;
    private String resource_id;
    private String content_desc;
    private String clickable;
    private String selected;

    Attrs(String text, String c_lass, String resource_id, String content_desc,String clickable, String selected) {
        this.text = text;
        this.c_lass =c_lass;
        this.resource_id = resource_id;
        this.content_desc = content_desc;
        this.clickable = clickable;
        this.selected = selected;
    }

    void setText(String text) {
        this.text = text;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
    }

    void setC_lass(String c_lass) {
        this.c_lass = c_lass;
    }

    void setContent_desc(String content_desc) {
        this.content_desc = content_desc;
    }

    public void setClickable(String clickable) {
        this.clickable = clickable;
    }

    void setSelected(String selected) {
        this.selected = selected;
    }

    public String getText() {
        return text;
    }

    public String getContent_desc() {
        return content_desc;
    }

    public String getResource_id() {
        return resource_id;
    }

    public String getClickable(){
        return clickable;
    }

    public String getSelected() {
        return selected;
    }

    public String getC_lass() {
        return c_lass;
    }
}
