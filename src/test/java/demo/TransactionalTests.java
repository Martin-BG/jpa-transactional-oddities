package demo;

import demo.model.User;
import demo.repository.UserRepository;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Log
@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionalTests {

    @Autowired
    private UserRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Long userId;

    @BeforeAll
    private void init() {
        userId = repository.save(new User()).getId();
    }

    @AfterAll
    private void end() {
        repository.deleteById(userId);
    }

    /**
     * This test fails because findById() returns User$HibernateProxy$
     * cached after a getOne() method invocation instead of a User instance
     */
    @Test
    public void getOne_findById() {
        User getOneUser = repository.getOne(userId);
        User findByIdUser = repository.findById(userId).get();

        log.info("\ngetOne_findById" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName() +
                "\n\tfindById() -> " + findByIdUser.getClass().getName());

        Assertions.assertFalse(findByIdUser.getClass().getName().contains("$HibernateProxy$"), "findById() returned a Proxy");
        Assertions.assertTrue(getOneUser.getClass().getName().contains("$HibernateProxy$"), "getOne() returned an Entity");
    }

    @Test
    public void getOne_clear_findById() {
        User getOneUser = repository.getOne(userId);
        entityManager.clear();
        User findByIdUser = repository.findById(userId).get();

        log.info("\ngetOne_clear_findById" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName() +
                "\n\tentityManager.clear()" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName());

        Assertions.assertFalse(findByIdUser.getClass().getName().contains("$HibernateProxy$"), "findById() returned a Proxy");
        Assertions.assertTrue(getOneUser.getClass().getName().contains("$HibernateProxy$"), "getOne() returned an Entity");
    }

    @Test
    public void getOne_detach_findById() {
        User getOneUser = repository.getOne(userId);
        entityManager.detach(getOneUser);
        User findByIdUser = repository.findById(userId).get();

        log.info("\ngetOne_detach_findById" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName() +
                "\n\tentityManager.detach(getOneUser)" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName());

        Assertions.assertFalse(findByIdUser.getClass().getName().contains("$HibernateProxy$"), "findById() returned a Proxy");
        Assertions.assertTrue(getOneUser.getClass().getName().contains("$HibernateProxy$"), "getOne() returned an Entity");
    }

    /**
     * This test fails because getOne() returns the User instance
     * cached after a findById() method invocation instead of a User$HibernateProxy$
     */
    @Test
    public void findById_getOne() {
        User findByIdUser = repository.findById(userId).get();
        User getOneUser = repository.getOne(userId);

        log.info("\nfindById_getOne" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName() +
                "\n\tgetOne() -> " + getOneUser.getClass().getName());

        Assertions.assertFalse(findByIdUser.getClass().getName().contains("$HibernateProxy$"), "findById() returned a Proxy");
        Assertions.assertTrue(getOneUser.getClass().getName().contains("$HibernateProxy$"), "getOne() returned an Entity");
    }

    @Test
    public void findById_clear_getOne() {
        User findByIdUser = repository.findById(userId).get();
        entityManager.clear();
        User getOneUser = repository.getOne(userId);

        log.info("\nfindById_clear_getOne" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName() +
                "\n\tentityManager.clear()" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName());

        Assertions.assertFalse(findByIdUser.getClass().getName().contains("$HibernateProxy$"), "findById() returned a Proxy");
        Assertions.assertTrue(getOneUser.getClass().getName().contains("$HibernateProxy$"), "getOne() returned an Entity");
    }

    @Test
    public void findById_detach_getOne() {
        User findByIdUser = repository.findById(userId).get();
        entityManager.detach(findByIdUser);
        User getOneUser = repository.getOne(userId);

        log.info("\nfindById_detach_getOne" +
                "\n\tfindById() -> " + findByIdUser.getClass().getName() +
                "\n\tentityManager.detach(findByIdUser)" +
                "\n\tgetOne() -> " + getOneUser.getClass().getName());

        Assertions.assertFalse(findByIdUser.getClass().getName().contains("$HibernateProxy$"), "findById() returned a Proxy");
        Assertions.assertTrue(getOneUser.getClass().getName().contains("$HibernateProxy$"), "getOne() returned an Entity");
    }
}
