package servlet;

import annotation.*;
import controller.UserController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "dispatcherServlet", urlPatterns = "/*",loadOnStartup = 1,
        initParams = {@WebInitParam(name = "base-package",value = "minispringmvc")})
public class DispatcherServlet extends HttpServlet {
    //扫描的基包
    private String basePackage = "";
    //基包下面所有带包路径的限定类名
    private List<String> packageNames = new ArrayList<>();
    //注解实例化 注解上的类名称：实例化对象
    private Map<String,Object> instanceMap = new HashMap<>();
    //带包路径的权限定名称：注解的名称
    private Map<String,String> nameMap = new HashMap<>();
    //URL地址和方法的映射关系， SpringMvc就是方法调用链
    private Map<String, Method> urlMethodMap = new HashMap<>();
    //Method和权限定类名映射关系 主要是为了通过Method找到该方法的对象利用反射执行
    private Map<Method,String> methodPackageMap = new HashMap<>();

    public void  init(ServletConfig config) throws ServletException{
        basePackage = config.getInitParameter("base-package");
        try {
            //1.扫描基包得到全部的带包路径权限定类名
            //2.把带有@Controller/@Service/@Repositroy的类实例化放入Map中，key为注释上的名称
            //3.Spring IOC 注入
            //4.完成URl地址与方法的映射关系
            scanBasePackage(basePackage);
            instance(packageNames);
            springIOC();
            handlerUrlMethodMap();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (InstantiationException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private void scanBasePackage(String basePackage){
        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.","/"));
        File basePackageFile = new File(url.getPath());
        System.out.println("scan:"+basePackageFile);
        File[] childFiles = basePackageFile.listFiles();
        for (File file:childFiles){
            if (file.isDirectory()){
                scanBasePackage(basePackage+"."+file.getName());
            }else if(file.isFile()){
                //类似这种：com.minispringmvc.controller.class 去掉class
                packageNames.add(basePackage + "." + file.getName().split("\\.")[0]);
            }
        }
    }

    private void instance(List<String> packageNames) throws ClassNotFoundException,IllegalAccessException,InstantiationException{
        if (packageNames.size()<1){
            return;
        }
        for (String string :packageNames){
            Class c = Class.forName(string);
            if (c.isAnnotationPresent(Controller.class)){
                Controller controller = (Controller)c.getAnnotation(Controller.class);
                String controllerName = controller.value();
                instanceMap.put(controllerName,c.newInstance());
                nameMap.put(string,controllerName);
                System.out.println("Controller:"+string + ",value:"+controller.value());
            }else if(c.isAnnotationPresent(Service.class)){
                Service service = (Service)c.getAnnotation(Service.class);
                String serviceName = service.value();
                instanceMap.put(serviceName,c.newInstance());
                nameMap.put(string,serviceName);
                System.out.println("Service:"+string + ",value:"+service.value());
            }else if(c.isAnnotationPresent(Repository.class)){
                Repository repository = (Repository)c.getAnnotation(Repository.class);
                String repositoryName = repository.value();
                instanceMap.put(repositoryName,c.newInstance());
                nameMap.put(string,repositoryName);
                System.out.println("Controller:"+string + ",value:"+repository.value());
            }
        }
    }
    private void springIOC()throws IllegalAccessException{
        for (Map.Entry<String,Object> entry : instanceMap.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields){
                if(field.isAnnotationPresent(Qualifier.class)){
                    String name = field.getAnnotation(Qualifier.class).value();
                    field.setAccessible(true);
                    field.set(entry.getValue(),instanceMap.get(name));
                }
            }
        }
    }

    private void handlerUrlMethodMap() throws ClassNotFoundException{
        if(packageNames.size()<1){
            return;
        }
        for (String string:packageNames){
            Class c = Class.forName(string);
            if (c.isAnnotationPresent(Controller.class)){
                Method[] methods = c.getMethods();
                StringBuffer baseUrl = new StringBuffer();
                if(c.isAnnotationPresent(RequestMapping.class)){
                    RequestMapping requestMapping = (RequestMapping)c.getAnnotation(RequestMapping.class);
                    baseUrl.append(requestMapping.value());
                }
                for (Method method:methods){
                    if (method.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping requestMapping = (RequestMapping)method.getAnnotation(RequestMapping.class);
                        baseUrl.append(requestMapping.value());
                        urlMethodMap.put(baseUrl.toString(),method);
                        methodPackageMap.put(method,string);
                    }
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
        doPost(req,resp);
    }

    @Override
    protected  void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        String uri = req.getRequestURI();
        String contentPath = req.getContextPath();
        String path = uri.replaceAll(contentPath,"");
        //通过path找到method
        Method method = urlMethodMap.get(path);
        if (method !=null){
            //通过Method拿到Controller对象，准备反射执行
            String packageName = methodPackageMap.get(method);
            String controllerName = nameMap.get(packageName);

            //拿到Controller对象
            UserController userController = (UserController) instanceMap.get(controllerName);
            try {
                method.setAccessible(true);
                method.invoke(userController);
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }catch (InvocationTargetException e){
                e.printStackTrace();
            }
        }
    }

}
