# JPA @Transactional oddities

Exposes oddities of getOne(id) and findById(id) methods when used in a single transaction.

In [reply](https://stackoverflow.com/a/58514800/7598851) to a [question](https://stackoverflow.com/q/58509408/7598851) at StackOverflow.

## Preface
When a method is executed in a transaction, entities acquired or merged/saved from/to the 
database are cached until the end of the transaction (usually the end of the method). 

In result any call for entity with same ID will be returned directly from the cache and will not hit the database:

* [Understanding Hibernate First Level Cache with Example](https://howtodoinjava.com/hibernate/understanding-hibernate-first-level-cache-with-example/)
* [How does a JPA Proxy work and how to unproxy it with Hibernate](https://vladmihalcea.com/how-does-a-jpa-proxy-work-and-how-to-unproxy-it-with-hibernate/)
* [The best way to initialize LAZY entity and collection proxies with JPA and Hibernate](https://vladmihalcea.com/initialize-lazy-proxies-collections-jpa-hibernate/)

`T getOne(ID id)` and `Optional findById(ID id)` are the two most commonly used methods for entity retrieval:

```
T getOne(ID id)

Returns a reference to the entity with the given identifier.
Depending on how the JPA persistence provider is implemented 
this is very likely to always return an instance and throw 
an EntityNotFoundException on first access. Some of them will 
reject invalid identifiers immediately.

Parameters: 
    id - must not be null.
Returns: 
    a reference to the entity with the given identifier.
```
```
Optional findById(ID id)

Retrieves an entity by its id.

Parameters: 
    id - must not be null.
Returns: 
    the entity with the given id or Optional#empty() if none found
```

## The Problem

When both `getOne(id)` and `findById(id)` methods are called for the same `id` 
in a **single transaction** only the first call is actually executed and the second 
one returns the already cached *entity* (when `findById()` is called first) or *proxy* 
(when `getOne(id)` is called first):

* call findById(id) first and then getOne(id) returns the same entity object for both
* call getOne(id) first and then findById(id) returns the same proxy for both

Documentation on `getOne(id)` states that it could return an instance instead 
of reference (*HibernateProxy*), so having it returning an entity could be expected.

Documentation on `findById()` from the other hand does not have any hints in the 
direction that it could return anything but `Optional` of entity or empty `Optional`.

There's not much information to be found and it is hard to conclude if this is a **bug 
in the implementation of `findById()`** or just a not (well) documented feature.

## Workarounds

 1. Do not acquire the same entity twice in the same transactional method using 
 both `getOne(id)` and `findById(id)`.
 1. Avoid using @Transactional when not need. Transactions can be managed manually too. 
 Here are some good articles on that subject:
    * [5 common Spring @Transactional pitfalls](https://codete.com/blog/5-common-spring-transactional-pitfalls/)
    * [Spring Transactional propagation modes](https://codete.com/blog/spring-transaction-propagation-modes/)
    * [Spring pitfalls: transactional tests considered harmful](https://www.nurkiewicz.com/2011/11/spring-pitfalls-transactional-tests.html)
 1. Detach the first loaded entity/proxy before (re-)loading using the other method:
     ```java
    import javax.persistence.EntityManager;
    import org.springframework.transaction.annotation.Transactional;
    
    @Transactional
    @Service
    public class SomeServiceImpl implements SomeService {
    
        private final SomeRepository repository;
        private final EntityManager entityManager;
    
        // constructor, autowiring
    
        @Override
        public void someMethod(long id) {
            SomeEntity getOne = repository.getOne(id); // Proxy -> added to cache
            entityManager.detach(getOne); // removes getOne from the cache
            SomeEntity findById = repository.findById(id).get(); // Entity from the DB
        }
    }
     ```
 1. Similar to the 3rd approach, but instead of removing a single object from the cache, 
 remove all at once using the `clear()` method:
      ```java
     import javax.persistence.EntityManager;
     import org.springframework.transaction.annotation.Transactional;
     
     @Transactional
     @Service
     public class SomeServiceImpl implements SomeService {
     
         private final SomeRepository repository;
         private final EntityManager entityManager;
     
         // constructor, autowiring
     
         @Override
         public void someMethod(long id) {
             SomeEntity getOne = repository.getOne(id); // Proxy -> added to cache
             entityManager.clear(); // clears the cache
             SomeEntity findById = repository.findById(id).get(); // Entity from the DB
         }
     }
      ```
    
## Additional Information

* [When use getOne and findOne methods Spring Data JPA](https://stackoverflow.com/questions/24482117)
* [Hibernate Session: evict() and merge() Example](https://www.concretepage.com/hibernate/hibernate-session-evict-and-merge-example)
* [clear(), evict() and close() methods in Hibernate](https://www.connect2java.com/tutorials/hibernate/clear-evict-and-close-methods-in-hibernate/)
* [JPA - Detaching an Entity Instance from the Persistence Context](https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/detaching.html)
* [Difference between getOne and findById in Spring Data JPA?](https://www.javacodemonk.com/difference-between-getone-and-findbyid-in-spring-data-jpa-3a96c3ff)

## Project Structure
1. Configuration:
    * [pom.xml](pom.xml) - dependencies(`spring-boot-starter-data-jpa`, `h2`, `lombok`)
    * [application.properties](src/main/resources/application.properties) - enable detailed logging
1. Application:
    * [User](src/main/java/demo/model/User.java) - model with just `id` field
    * [UserRepository](src/main/java/demo/repository/UserRepository.java) - default `JpaRepository`
    * [UserService](src/main/java/demo/service/UserService.java) - service interface, **not used by tests**
    * [UserServiceImpl](src/main/java/demo/service/UserServiceImpl.java) - service interface implementation, **not used by tests**
    * [Application](src/main/java/demo/Application.java) - program entry point, runs methods from `UserService`, logs results.
1. Tests - There are 6 combinations of `getOne(id)` and `findById(id)` 
with or without `em.clear()` and `em.detach(entity)` between them.
    * [NonTransactionalTests](src/test/java/demo/NonTransactionalTests.java) - runs all 6 tests **WITHOUT** 
    `@Transactional` annotation on test class. **All tests succeed**
    * [TransactionalTests](src/test/java/demo/TransactionalTests.java) - runs all 6 tests **WITH** 
    `@Transactional` annotation on test class. **`findById_getOne()` and `getOne_findById()` fail**
