
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.adriencaubel.Owner;
import fr.adriencaubel.Pet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

/**
 * Impossible de créer une méthode initialize() car drop-create avec test unitaire reset la bdd.
 */
public class PetTest {

    @Test
    public void filterAndSortInJava() {       
    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.hibernate.tutorial.jpa");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        // Insert test data
        em.getTransaction().begin();

        Owner owner = new Owner("John Doe");

        Pet pet1 = new Pet("Buddy", "dog", 3);
        Pet pet2 = new Pet("Charlie", "dog", 4);
        Pet pet3 = new Pet("Max", "cat", 2);

        owner.addPet(pet1);
        owner.addPet(pet2);
        owner.addPet(pet3);

        em.persist(owner); // Cascade.ALL

        em.getTransaction().commit();
        System.out.println("Database init");

        em.close(); // Avoid retrieve from cache
        em = emf.createEntityManager();
        
        
        // Retrieve an owner by ID
        Owner foundOwner = em.find(Owner.class, 1L);
        
        // Retrieve and filter pets manually
        List<Pet> pets = new ArrayList<>();
        for (Pet pet : foundOwner.getPets()) {
            if (pet.getType().equals("dog") && pet.getAge() < 5) {
                pets.add(pet);
            }
        }
        
        // Sort pets alphabetically
        pets.sort(Comparator.comparing(Pet::getName));
        
        // Display results
        System.out.println(pets.size());
        pets.forEach(System.out::println);
    }

    @Test
    public void filterWithJPQL() {
    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.hibernate.tutorial.jpa");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        // Insert test data
        em.getTransaction().begin();

        Owner owner = new Owner("John Doe");

        Pet pet1 = new Pet("Buddy", "dog", 3);
        Pet pet2 = new Pet("Charlie", "dog", 4);
        Pet pet3 = new Pet("Max", "cat", 2);

        owner.addPet(pet1);
        owner.addPet(pet2);
        owner.addPet(pet3);

        em.persist(owner);

        em.getTransaction().commit();
        System.out.println("Database init");

        em.close(); // Avoid retrieve from cache
        em = emf.createEntityManager();
        
        // *****************************************************************
        
        Long ownerId = 1L; // Replace with the actual owner ID
        String jpql = "SELECT p FROM Pet p WHERE p.type = :type AND p.age < :age AND p.owner.id = :ownerId ORDER BY p.name ASC";

        TypedQuery<Pet> query = em.createQuery(jpql, Pet.class);
        query.setParameter("type", "dog");
        query.setParameter("age", 5);
        query.setParameter("ownerId", ownerId);

        List<Pet> filteredPets = query.getResultList();
        
        // Display results
        filteredPets.forEach(System.out::println);
    }

    @Test
    public void filterWithCriteriaAPI() {
    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.hibernate.tutorial.jpa");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        // Insert test data
        em.getTransaction().begin();

        Owner owner = new Owner("John Doe");

        Pet pet1 = new Pet("Buddy", "dog", 3);
        Pet pet2 = new Pet("Charlie", "dog", 4);
        Pet pet3 = new Pet("Max", "cat", 2);

        owner.addPet(pet1);
        owner.addPet(pet2);
        owner.addPet(pet3);

        em.persist(owner);

        em.getTransaction().commit();
        System.out.println("Database init");

        em.close(); // Avoid retrieve from cache
        em = emf.createEntityManager();
        
        // *****************************************************************

        
        Long ownerId = 1L;
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Pet> cq = cb.createQuery(Pet.class);
        Root<Pet> pet = cq.from(Pet.class);
        Join<Pet, Owner> ownerJoin = pet.join("owner");

        cq.select(pet)
          .where(
            cb.equal(pet.get("type"), "dog"),
            cb.lessThan(pet.get("age"), 5),
            cb.equal(ownerJoin.get("id"), ownerId)
          )
          .orderBy(cb.asc(pet.get("name")));

        TypedQuery<Pet> q = em.createQuery(cq);
        List<Pet> filteredPets = q.getResultList();
        
        // Display results
        filteredPets.forEach(System.out::println);
    }
    
    
    @Test
    public void joinFetch() {
    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.hibernate.tutorial.jpa");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        // Insert test data
        em.getTransaction().begin();

        Owner owner = new Owner("John Doe");

        Pet pet1 = new Pet("Buddy", "dog", 3);
        Pet pet2 = new Pet("Charlie", "dog", 4);
        Pet pet3 = new Pet("Max", "cat", 2);

        owner.addPet(pet1);
        owner.addPet(pet2);
        owner.addPet(pet3);

        em.persist(owner);

        em.getTransaction().commit();
        System.out.println("Database init");

        em.close(); // Avoid retrieve from cache
        em = emf.createEntityManager();
        
        // *****************************************************************
        
        Long ownerId = 1L; // Replace with the actual owner ID
        String jpql = "SELECT p FROM Pet p JOIN FETCH p.owner WHERE p.type = :type AND p.age < :age AND p.owner.id = :ownerId ORDER BY p.name ASC";
        
        TypedQuery<Pet> query = em.createQuery(jpql, Pet.class);
        query.setParameter("type", "dog");
        query.setParameter("age", 5);
        query.setParameter("ownerId", ownerId);

        List<Pet> filteredPets = query.getResultList();
        
        // Display results
        filteredPets.forEach(System.out::println);
    	
        filteredPets.forEach(pet -> {
        	System.out.println(pet.getOwner().getName());
        });
    }
}