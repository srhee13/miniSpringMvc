package controller;

import annotation.Controller;
import annotation.Qualifier;
import annotation.RequestMapping;
import service.UserService;

@Controller("userController")
@RequestMapping("/user")
public class UserController {
    @Qualifier("userServiceImpl")
    private UserService userService;

    @RequestMapping("/insert")
    public void insert(){
        userService.insert();
    }

}
