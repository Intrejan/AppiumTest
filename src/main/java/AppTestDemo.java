import io.appium.java_client.AppiumDriver;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
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


        DFSTest(pageSource,driver);
        driver.closeApp();
    }

    /**
     * 对App的页面进行深度优先遍历测试
     * @param pageSource 当前页面的xml文件，需要对其进行处理，获得可以进行测试的元素的位置。
     */
    private void DFSTest(String pageSource,AppiumDriver<org.openqa.selenium.WebElement> driver) {

        List<Target> targetList = new ArrayList<Target>();

        try{
            Document doc = DocumentHelper.parseText(pageSource);
            Element root = doc.getRootElement();
            targetList = readNode(root,targetList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Target target : targetList) {
            String type = target.getType();
            String value = target.getValue();
            if ("text".equals(type)) {
                driver.findElementsByName(value).get(0).click();
            }else if("resource-id".equals(type)){
                driver.findElementsById(value).get(0).click();
            }else if("content-desc".equals(type)){
                driver.findElementsByAccessibilityId(value).get(0).click();
            }

            System.out.println(target.getType() + " : " + target.getValue());
        }
    }

    /**
     * 本方法用于获得当前页面所有可以进行点击操作的元素的定位必要数据
     * @param root 根节点
     * @param targetList 存储目标的list
     * @return targetList
     */
    private static List<Target> readNode(Element root,List<Target> targetList) {
        if (root == null) return targetList;
        // 获取属性
        List<Attribute> attrs = root.attributes();
        if (attrs != null && attrs.size() > 0) {
            for (Attribute attr : attrs) {
                if(attr.getName().equals("resource-id") && attr.getValue().length()>0){
                    Target target = new Target("resource-id",attr.getValue());
                    targetList.add(target);
                    break;
                }
                else if(attr.getName().equals("text") && attr.getValue().length()>0){
                    Target target = new Target("text",attr.getValue());
                    targetList.add(target);
                    break;
                }
                else if(attr.getName().equals("content-desc") && attr.getValue().length()>0){
                    Target target = new Target("content-desc",attr.getValue());
                    targetList.add(target);
                }
            }
        }
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
        capabilities.setCapability("noReset","true");

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
        String apkPath = "apk/IThouse.apk";
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