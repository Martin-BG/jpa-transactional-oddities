package demo;

import demo.model.User;
import demo.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        UserService userService = ctx.getBean(UserService.class);

        Long userId = userService.create(new User());
        userService.getOne_findById(userId);
        userService.getOne_detach_findById(userId);
        userService.getOne_clear_findById(userId);
        userService.findById_getOne(userId);
        userService.findById_detach_getOne(userId);
        userService.findById_clear_getOne(userId);
    }
}
