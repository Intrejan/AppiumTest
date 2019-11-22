import io.appium.java_client.AppiumDriver;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class AppTestDemo {

    /**
     * 所有和AppiumDriver相关的操作都必须写在该函数中
     * @param driver appium的驱动
     */
    private void test(AppiumDriver<org.openqa.selenium.WebElement> driver)  {
        try {
            Thread.sleep(6000);		//等待6s，待应用完全启动
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.manage().timeouts().implicitlyWait(8, TimeUnit.SECONDS); //设置尝试定位控件的最长时间为8s,也就是最多尝试8s
        String pageSource = driver.getPageSource();

        if(pageSource.contains("确定") || pageSource.contains("允许")){
            Prepare(pageSource, driver);
        }

        try {
            pageSource = driver.getPageSource();
        } catch (Exception e) {
            System.err.print("");
        }

        Page page = new Page(pageSource,"");
        //深度优先遍历
        DFSTest(page,driver,0);
        driver.closeApp();
    }

    /**
     * 这个方法对待测app进行预处理，包括设置权限等内容
     * @param pageSource 页面的xml
     */
    private void Prepare(String pageSource, AppiumDriver<WebElement> driver) {
        try{
            driver.findElementsByName("确定").get(0).click();
        } catch (Exception e) {
            System.out.println("未定位到‘确定’。");
        }
        try{
            driver.findElementsByName("允许").get(0).click();

        } catch (Exception e) {
            System.out.println("未定位到‘允许’。");
        }

    }

    private ArrayList<String> stack = new ArrayList<String>();
    /**
     * 对App的页面进行深度优先遍历测试
     * @param page 当前页面的对象，需要对其进行处理，获得可以进行测试的元素的位置。
     * @param level 当前深度
     */
    private void DFSTest(Page page,AppiumDriver<org.openqa.selenium.WebElement> driver,int level) {

        try {
            Thread.sleep(2000);		//等待2s，待新页面完全启动
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String pageSource = page.getPageSource();       //获得当前页面的xml

        List<Target> targetList = new ArrayList<Target>();      //目标元素列表
        try{
            Document doc = DocumentHelper.parseText(pageSource);
            Element root = doc.getRootElement();
            targetList = readNode(root,targetList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Target> new_targetList = new ArrayList<Target>();

        if(targetList.size()>0){

            if(targetList.get(0).getValue().equals("android.widget.ImageButton") ){
                for(int i=1;i<targetList.size();i++){
                    new_targetList.add(targetList.get(i));
                }
                new_targetList.add(targetList.get(0));
            }
            else{
                new_targetList=targetList;
            }
        }

        if(new_targetList.size()>0){
            if(new_targetList.get(0).getValue().equals("登录")){
                new_targetList.add(new Target("text","登录"));
            }
            for(Target all_target:new_targetList){
                System.err.println(all_target.getType() + " : " + all_target.getValue());
            }
        }

        for (Target target : new_targetList) {
            String type = target.getType();
            String value = target.getValue();
            int state = 0;
            String nowPageSource = "";
            if ("text".equals(type) && !stack.contains(value)) {
                if(value.contains("输入") || value.contains("搜索") || value.contains("search")){
                    try {
                        System.out.print(target.getType() + " : " + target.getValue());
                        stack.add(value);
                        driver.findElementsByName(value).get(0).sendKeys("123456");
                        driver.hideKeyboard();
                    } catch (Exception e) {

                        //driver.navigate().back();
                        continue;
                    }
                }else{
                    try{
                        System.out.print(target.getType() + " : " + target.getValue());
                        if(driver.findElementsByName(value).size()>1){
                            driver.findElementsByName(value).get(1).click();
                        }else{
                            if(!value.equals("取消") && !value.equals("确定")){
                                stack.add(value);
                            }
                            driver.findElementsByName(value).get(0).click();
                        }
                    } catch (Exception e) {
                        driver.findElementsByClassName("android.widget.ImageButton");
                        try{
                            driver.findElementsByName(value).get(0).click();
                        }
                        catch (Exception ex) {
                            continue;
                        }
                        //driver.navigate().back();
                    }
                }

                try {
                    Thread.sleep(1000);		//等待2s，待应用完全启动
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nowPageSource = driver.getPageSource();
                state = comparePage(nowPageSource,page);
                System.err.println(state);
            }
            else if("content-desc".equals(type) && !stack.contains(value)){
                if(!value.contains("转到")){
                    stack.add(value);
                }try{
                    System.out.print(target.getType() + " : " + target.getValue());
                    driver.findElementsByAccessibilityId(value).get(0).click();
                } catch (Exception e) {
                    //driver.navigate().back();
                    continue;
                }
                try {
                    Thread.sleep(1000);		//等待6s，待应用完全启动
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nowPageSource = driver.getPageSource();
                state = comparePage(nowPageSource,page);
                System.err.println(state);
            }
            else if("class".equals(type)){
                try{
                    System.out.print(target.getType() + " : " + target.getValue());
                    driver.findElementsByClassName(value).get(0).click();
                } catch (Exception e) {
                    driver.navigate().back();
                    try{driver.findElementsByClassName(value).get(0).click();} catch (Exception ex) {
                        continue;
                    }
                }
                try {
                    Thread.sleep(1000);		//等待6s，待应用完全启动
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nowPageSource = driver.getPageSource();
                state = comparePage(nowPageSource,page);
                System.err.println(state);

            }

            if(state == 2){
                return;
            }
            else if(state == 1){
                Page nextPage = new Page(nowPageSource, pageSource);
                DFSTest(nextPage,driver,level+1);
            }
        }

//        driver.navigate().back();
//        System.err.println("back");

        try {
            Thread.sleep(1000);		//等待6s，待应用完全启动
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 此方法用于判断两个页面的相似程度，用来决定是否进行了页面的跳转。
     * @param nowPageSource 经过操作之后的页面的xml
     * @param page 当前页面对象
     * @return 0：没有进行页面的跳转；1 跳入的新的页面；2 回到父亲页面。
     */
    private int comparePage(String nowPageSource, Page page) {
        boolean isEqualRoot = true;
        boolean isEqualFather = true;
        String pageSource = page.getPageSource();
        String fatherSource = page.getFatherPage();
        int state=0;
        try{
            //读取操作后的当前页面
            Document now_doc = DocumentHelper.parseText(nowPageSource);
            Element now_root = now_doc.getRootElement();
            ArrayList<String> now_count = new ArrayList<String>();
            now_count = CalcNode(now_root,now_count);

            //读取原先的页面
            Document doc = DocumentHelper.parseText(pageSource);
            Element root = doc.getRootElement();
            ArrayList<String> root_count = new ArrayList<String>();
            root_count= CalcNode(root,root_count);

            //读取父亲页面
            ArrayList<String> father_root_count = new ArrayList<String>();
            if(fatherSource.length() < 1){
                father_root_count.add("launched Activity");
            } else{
                Document father_doc = DocumentHelper.parseText(fatherSource);
                Element father_root = father_doc.getRootElement();
                father_root_count = CalcNode(father_root,father_root_count);
            }

            if(Math.abs(now_count.size() - root_count.size()) <= 0){
                for(int i=0;i<root_count.size();i++){
                    if(!now_count.get(i).equals(root_count.get(i))){
                        isEqualRoot = false;break;
                    }
                }
            }else{
                isEqualRoot = false;
            }

            if(Math.abs(now_count.size()-father_root_count.size()) <= 0){
                for(int i=0;i<father_root_count.size();i++){
                    if(!now_count.get(i).equals(father_root_count.get(i))){
                        isEqualFather = false;break;
                    }
                }
            }else{
                isEqualFather = false;
            }

        if(fatherSource.contains("id/design_navigation_view") && nowPageSource.contains("id/design_navigation_view")){
            isEqualFather = true;
        }

        } catch (Exception e) {
            System.err.println("获取页面XML解析失败");
        }
        if(isEqualRoot){
            state = 0;
        }else if(isEqualFather){
            state = 2;
        }else{
            state = 1;
        }
        return state;
    }

    /**
     *  此方法用于统计页面中所有节点的名称和数量，并且以此作为依据来判断是否进行了页面的跳转。
     * @param root 页面根节点
     * @param result 结果
     * @return 一个节点arrayList。
     */
    private ArrayList<String> CalcNode(Element root, ArrayList<String> result) {
        if(root==null)return result;
        //System.out.println(root.getName());
        result.add(root.getName());
        List<Element> childNodes = root.elements();
        for (Element e : childNodes) {
            CalcNode(e,result);
        }
        return result;
    }

    /**
     * 本方法用于获得当前页面所有可以进行点击操作的元素的定位必要数据
     * @param root 根节点
     * @param targetList 存储目标的list
     * @return targetList
     */
    private static List<Target> readNode(Element root,List<Target> targetList) {
        List<Target> target_end = new ArrayList<Target>();
        if (root == null) return targetList;
        // 获取属性
        List<Attribute> attrs = root.attributes();
        Attrs attrs1 = new Attrs("","","","","","");
        if (attrs != null && attrs.size() > 0) {
            for (Attribute attr : attrs) {

                if(attr.getName().equals("text")){
                    attrs1.setText(attr.getValue());
                }
                if(attr.getName().equals("resource-id")){
                    attrs1.setResource_id(attr.getValue());
                }
                if(attr.getName().equals("class")){
                    attrs1.setC_lass(attr.getValue());
                }
                if(attr.getName().equals("content-desc")){
                    attrs1.setContent_desc(attr.getValue());
                }
                if(attr.getName().equals("clickable")){
                    attrs1.setClickable(attr.getValue());
                }
                if(attr.getName().equals("selected")){
                    attrs1.setSelected(attr.getValue());
                }
            }
            String text = attrs1.getText();
            String content_desc = attrs1.getContent_desc();
            boolean isNet = false;
            if(text.length()>0){
                for(char c:text.toCharArray()){
                    if(Character.isDigit(c)){
                    isNet = true;break;
                    }
                }
            }

            if( text.contains("邮件") || text.contains("隐藏") || text.contains("下载")||text.contains("加载失败")
                    ||text.contains("G")||text.contains("反馈") || text.contains("@") || text.contains("www")
                    || text.contains("分享")){
                isNet = true;
            }
            if(content_desc.contains("下载")|| content_desc.contains("G")||content_desc.contains("反馈")
                    || content_desc.contains("@") || content_desc.contains("www")
                    || content_desc.contains("分享")){
                isNet = true;
            }
            if(!attrs1.getC_lass().equals("android.view.View") && !attrs1.getText().equals("夜间模式") && !isNet){
                if(attrs1.getText().length()>0){
                    if(!attrs1.getC_lass().equals("android.widget.TextView") || attrs1.getResource_id().length()>0){
                        if(attrs1.getText().length() <= 5 || attrs1.getClickable().equals("true")){
                            Target target = new Target("text",attrs1.getText());
                            boolean have = false;
                            for(Target t:targetList){
                                if (t.getType().equals(target.getType()) && t.getValue().equals(target.getValue())) {
                                    have = true;
                                    break;
                                }
                            }
                            if(!have){
                                targetList.add(target);
                            }
                        }
                    }
                    else{
                        Target target = new Target("text",attrs1.getText());
                        target_end.add(target);
                    }
                }
                else if(attrs1.getC_lass().equals("android.widget.ImageButton") && attrs1.getText().length()==0 && attrs1.getResource_id().length()==0){
                    Target target = new Target("class","android.widget.ImageButton");
                    boolean have = false;
                    for(Target t:targetList){
                        if (t.getType().equals(target.getType()) && t.getValue().equals(target.getValue())) {
                            have = true;
                            break;
                        }
                    }
                    if(!have){
                        targetList.add(target);
                    }
                }
                else if(attrs1.getContent_desc().length()>0){
                    Target target = new Target("content-desc",attrs1.getContent_desc());
                    boolean have = false;
                    for(Target t:targetList){
                        if (t.getType().equals(target.getType()) && t.getValue().equals(target.getValue())) {
                            have = true;
                            break;
                        }
                    }
                    if(!have){
                        targetList.add(target);
                    }
                }
            }
        }

        targetList.addAll(target_end);

        List<Element> childNodes = root.elements();
        for (Element e : childNodes) {
            readNode(e,targetList);
        }
        return targetList;
    }

    /**
     * AppiumDriver的初始化逻辑必须写在该函数中
     * @return appium 的 Driver
     * @param appiumUrl 启动appium的Url
     * @param UDID 待测机器的UDID
     * @param apkPath app路径
     */
    private AppiumDriver<org.openqa.selenium.WebElement> initAppiumTest(String apkPath, String UDID, String appiumUrl) {

        AppiumDriver<org.openqa.selenium.WebElement> driver=null;
        File app = new File( apkPath);

        //调用获得入口的方法
        String appPackage = getEntrance(apkPath).split(":")[0];
        String appActivity = getEntrance(apkPath).split(":")[1];

        //设置自动化相关参数
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", "");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", UDID);
        capabilities.setCapability("appPackage", appPackage);
        capabilities.setCapability("appActivity", appActivity);
        capabilities.setCapability("noSign", "true");
        //capabilities.setCapability("noReset","true");

        //设置apk路径
        capabilities.setCapability("app", app.getAbsolutePath());

        //设置用例执行完成后重置键盘
        capabilities.setCapability("resetKeyboard","true");

        //初始化
        try {
            driver = new AppiumDriver<org.openqa.selenium.WebElement>(new URL(appiumUrl), capabilities);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return driver;
    }

    /**
     * 通过apk的路劲来获得app的package以及启动activity，以下数据通过SDK自带的aapt.exe解包apk获得。
     * @param apkPath 手机应用的路径
     * @return 程序入口（包括app的包名以及app的launch Activity）
     */
    private static String getEntrance(String apkPath) {
        String entrance ="";
        if("apk/GuDong.apk".equals(apkPath)){
            entrance = "name.gudong.translate"+":"+"name.gudong.translate.ui.activitys.MainActivity";
        }else if("apk/Bihudaily.apk".equals(apkPath)){
            entrance="com.white.bihudaily"+":"+"com.white.bihudaily.module.splash.SplashActivity";
        }else if("apk/Bilibili.apk".equals(apkPath)){
            entrance = "com.hotbitmapgg.ohmybilibili"+":"+"com.hotbitmapgg.bilibili.module.common.SplashActivity";
        }else if("apk/GeekNews.apk".equals(apkPath)){
            entrance = "com.codeest.geeknews"+":"+"com.codeest.geeknews.ui.main.activity.WelcomeActivity";
        }else if("apk/IThouse.apk".equals(apkPath)){
            entrance = "com.danmo.ithouse"+":"+"com.danmo.ithouse.activity.MainActivity";
        }else if("apk/Leafpic.apk".equals(apkPath)){
            entrance = "org.horaapps.leafpic"+":"+"org.horaapps.leafpic.activities.SplashScreen";
        }else if("apk/Odyssey.apk".equals(apkPath)){
            entrance = "org.gateshipone.odyssey"+":"+"org.gateshipone.odyssey.activities.OdysseySplashActivity";
        }
        else{
            entrance = "error";
        }
        return entrance;
    }

    /**
     * 程序入口
     */
    private void start(String apkPath, String UDID, String appiumUrl) {
        test(initAppiumTest(apkPath,UDID,appiumUrl));
    }

    /**
     * main方法，用于启动程序
     * @param args 需要的参数有四个 ，分别为应用路径，UDID，Url，运行最大时长
     */
    public static void main(String[] args) {
        String apkPath = "apk/Leafpic.apk";
        String UDID = "emulator-5554";
        String appiumUrl = "http://127.0.0.1:4723/wd/hub";
        int runtime = 3600;
        AppTestDemo main = new AppTestDemo();
        //先判断是否有这个app
        if(getEntrance(apkPath).equals("error")){
            System.out.println("No Such App Found!");
        }else{
            main.start(apkPath,UDID,appiumUrl);
        }
    }
}