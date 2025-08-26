package com.example.e_learning_system.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Repository.RolesRepository;
import com.example.e_learning_system.Repository.PackageRepository;
import com.example.e_learning_system.Repository.PackageCourseRepository;
import com.example.e_learning_system.Repository.PromotionCodeRepository;
import com.example.e_learning_system.Service.Interfaces.PurchaseService;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Package;
import com.example.e_learning_system.Entities.PackageCourse;
import com.example.e_learning_system.Entities.PromotionCode;
import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Config.RolesName;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@Transactional
class PurchaseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PackageCourseRepository packageCourseRepository;

    @Autowired
    private PromotionCodeRepository promotionCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Test data
    private UserEntity testUser;
    private List<Course> testCourses;
    private Package testPackage;
    private PromotionCode testPromotionCode;

    @BeforeEach
    void setupTestData() {
        super.setUpDatabase(); // Call parent setup
        
        // Create test user
        testUser = createTestUser();
        

        
        // Create multiple courses
        testCourses = createTestCourses();
        
        // Create package with courses
        testPackage = createTestPackage(testCourses);
        
        // Create promotion code
        testPromotionCode = createTestPromotionCode();
    }




























    /*
     * helper methods for setting up the database 
     */
    private UserEntity createTestUser() {
        // Get or create USER role
        RolesEntity userRole = rolesRepository.findByName(RolesName.USER)
                .orElseGet(() -> {
                    RolesEntity role = new RolesEntity();
                    role.setName(RolesName.USER);
                    role.setDescription("Regular user role");
                    return rolesRepository.save(role);
                });

        UserEntity user = UserEntity.builder()
                .name("Test User")
                .email("testuser@example.com")
                .phone("+1234567890")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .isActive(true)
                .emailVerified(true)
                .bio("Test user for integration tests")
                .build();

        return userRepository.save(user);
    }

    private List<Course> createTestCourses() {
        List<Course> courses = new ArrayList<>();

        // Course 1: Java Programming
        Course javaCourse = Course.builder()
                .name("Java Programming Fundamentals")
                .description("Learn the basics of Java programming language")
                .oneTimePrice(new BigDecimal("99.99"))
                .subscriptionPriceMonthly(new BigDecimal("19.99"))
                .allowsSubscription(true)
                .currency(Currency.USD)
                .status(CourseStatus.PUBLISHED)
                .difficultyLevel(DifficultyLevel.BIGINNER)
                .createdBy(testUser)
                .isActive(true)
                .build();

        // Course 2: Spring Boot
        Course springCourse = Course.builder()
                .name("Spring Boot Mastery")
                .description("Master Spring Boot framework for enterprise applications")
                .oneTimePrice(new BigDecimal("149.99"))
                .subscriptionPriceMonthly(new BigDecimal("29.99"))
                .allowsSubscription(true)
                .currency(Currency.USD)
                .status(CourseStatus.PUBLISHED)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .createdBy(testUser)
                .isActive(true)
                .build();

        // Course 3: Database Design
        Course databaseCourse = Course.builder()
                .name("Database Design & SQL")
                .description("Learn database design principles and SQL")
                .oneTimePrice(new BigDecimal("79.99"))
                .subscriptionPriceMonthly(new BigDecimal("15.99"))
                .allowsSubscription(true)
                .currency(Currency.USD)
                .status(CourseStatus.PUBLISHED)
                .difficultyLevel(DifficultyLevel.BIGINNER)
                .createdBy(testUser)
                .isActive(true)
                .build();

        courses.add(courseRepository.save(javaCourse));
        courses.add(courseRepository.save(springCourse));
        courses.add(courseRepository.save(databaseCourse));

        return courses;
    }

    private Package createTestPackage(List<Course> courses) {
        // Create the package
        Package packageEntity = Package.builder()
                .name("Full Stack Development Bundle")
                .description("Complete package for full stack development learning")
                .price(new BigDecimal("299.99"))
                .discountPercentage(new BigDecimal("20.00"))
                .isActive(true)
                .build();

        Package savedPackage = packageRepository.save(packageEntity);

        // Add courses to the package
        List<PackageCourse> packageCourses = new ArrayList<>();
        for (Course course : courses) {
            PackageCourse packageCourse = PackageCourse.builder()
                    .packageEntity(savedPackage)
                    .course(course)
                    .build();
            packageCourses.add(packageCourseRepository.save(packageCourse));
        }

        savedPackage.setPackageCourses(packageCourses);
        return savedPackage;
    }

    private PromotionCode createTestPromotionCode() {
        PromotionCode promotionCode = PromotionCode.builder()
                .code("SAVE25")
                .description("Save 25% on your purchase")
                .discountPercentage(new BigDecimal("25.00"))
                .discountAmount(BigDecimal.ZERO)
                .maxUses(100)
                .currentUses(0)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .applicableToCourses(true)
                .applicableToPackages(true)
                .isActive(true)
                .build();

        return promotionCodeRepository.save(promotionCode);
    }

    // Getter methods for test data access
    protected UserEntity getTestUser() {
        return testUser;
    }

    protected List<Course> getTestCourses() {
        return testCourses;
    }

    protected Package getTestPackage() {
        return testPackage;
    }

    protected PromotionCode getTestPromotionCode() {
        return testPromotionCode;
    }

    @Test
    void testDataSetupValidation() {
        // Verify user is created
        assertNotNull(testUser);
        assertEquals("testuser@example.com", testUser.getEmail());
        assertTrue(testUser.getIsActive());

        // Verify courses are created
        assertNotNull(testCourses);
        assertEquals(3, testCourses.size());
        assertEquals("Java Programming Fundamentals", testCourses.get(0).getName());
        assertEquals("Spring Boot Mastery", testCourses.get(1).getName());
        assertEquals("Database Design & SQL", testCourses.get(2).getName());

        // Verify package is created with courses
        assertNotNull(testPackage);
        assertEquals("Full Stack Development Bundle", testPackage.getName());
        assertEquals(new BigDecimal("299.99"), testPackage.getPrice());
        assertNotNull(testPackage.getPackageCourses());
        assertEquals(3, testPackage.getPackageCourses().size());

        // Verify promotion code is created
        assertNotNull(testPromotionCode);
        assertEquals("SAVE25", testPromotionCode.getCode());
        assertEquals(new BigDecimal("25.00"), testPromotionCode.getDiscountPercentage());
        assertTrue(testPromotionCode.getValidUntil().isAfter(LocalDateTime.now()));
    }



    
}