package demo.service;

import demo.model.User;
import demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Log
@RequiredArgsConstructor
@Transactional //Removing this fixes the problem
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final EntityManager entityManager;

    @Override
    public Long create(User user) {
        return repository.save(user).getId();
    }

    /**
     * In this method findById() returns User$HibernateProxy$
     * cached after a getOne() method invocation instead
     * of a User instance
     */
    @Override
    public void getOne_findById(long userId) {
        User getOneUser = repository.getOne(userId);
        User findByIdUser = repository.findById(userId).get();

        log.info("\ngetOne_findById" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName() +
                "\n\tfindById() -> " + findByIdUser.getClass().getName());
    }

    /**
     * Works as expected because of entityManager.clear()
     * invocation between getOne() and findById()
     */
    @Override
    public void getOne_clear_findById(long userId) {
        User getOneUser = repository.getOne(userId);
        entityManager.clear();
        User findByIdUser = repository.findById(userId).get();

        log.info("\ngetOne_clear_findById" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName() +
                "\n\tentityManager.clear()" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName());
    }

    /**
     * Works as expected because of entityManager.detach(getOneUser)
     * invocation between getOne() and findById()
     */
    @Override
    public void getOne_detach_findById(long userId) {
        User getOneUser = repository.getOne(userId);
        entityManager.detach(getOneUser);
        User findByIdUser = repository.findById(userId).get();

        log.info("\ngetOne_detach_findById" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName() +
                "\n\tentityManager.detach(getOneUser)" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName());
    }

    /**
     * In this method getOne() returns the User instance
     * cached after a findById() method invocation instead
     * of a User$HibernateProxy$
     */
    @Override
    public void findById_getOne(long userId) {
        User findByIdUser = repository.findById(userId).get();
        User getOneUser = repository.getOne(userId);

        log.info("\nfindById_getOne" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName() +
                "\n\tgetOne() -> " + getOneUser.getClass().getName());
    }

    /**
     * Works as expected because of entityManager.clear()
     * invocation between getOne() and findById()
     */
    @Override
    public void findById_clear_getOne(long userId) {
        User findByIdUser = repository.findById(userId).get();
        entityManager.clear();
        User getOneUser = repository.getOne(userId);

        log.info("\nfindById_clear_getOne" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName() +
                "\n\tentityManager.clear()" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName());
    }

    /**
     * Works as expected because of entityManager.detach(findByIdUser)
     * invocation between findById() and getOne()
     */
    @Override
    public void findById_detach_getOne(long userId) {
        User findByIdUser = repository.findById(userId).get();
        entityManager.detach(findByIdUser);
        User getOneUser = repository.getOne(userId);

        log.info("\nfindById_detach_getOne" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName() +
                "\n\tentityManager.detach(findByIdUser)" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName());
    }
}
