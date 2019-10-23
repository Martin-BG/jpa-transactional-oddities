package demo.service;

import demo.model.User;

public interface UserService {

    Long create(User user);

    void getOne_findById(long userId);

    void getOne_clear_findById(long userId);

    void getOne_detach_findById(long userId);

    void findById_getOne(long userId);

    void findById_clear_getOne(long userId);

    void findById_detach_getOne(long userId);
}
