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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
     * tests we will have : 
     * purchase course : 
     * purchase package : 
     * purchase course with promotion code : 
     * purchase package with promotion code : 
     * purchase course with invalid promotion code : 
     * purchase package with invalid promotion code : 
     * purchase course with expired promotion code : 
     * purchase package with expired promotion code : 
     * purchase course with maxed out promotion code :
     * purchase package with maxed out promotion code :
     * purchase a package with non applicable promotion code to package :
     * purchase a course with non applicable promotion code to course :
     * purchase non-existent course : 
     * purchase non-existent package : 
     * invalid purchase request : 
     * purchase unpublish course : 
     * purchase unpublish package : 
     * purchase course with invalid data : 
     * purchase package with invalid data : 
     */


    // 1. Purchase course test
    @Test
    public void testPurchaseCourse() throws Exception {
        mockMvc.perform(post("/api/purchase/course/{courseid}", testCourses.get(0).getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": null,
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId())))
                .andExpect(status().isCreated());
    }

    // 2. Purchase package test
    @Test
    public void testPurchasePackage() throws Exception {
        mockMvc.perform(post("/api/purchase/package/{packageid}", testPackage.getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": null,
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId())))
                .andExpect(status().isCreated());
    }

    // 3. Purchase course with promotion code test
    @Test
    public void testPurchaseCourseWithPromotionCode() throws Exception {
        mockMvc.perform(post("/api/purchase/course/{courseid}", testCourses.get(0).getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "%s",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId(), testPromotionCode.getCode())))
                .andExpect(status().isCreated());
    }

    // 4. Purchase package with promotion code test
    @Test
    public void testPurchasePackageWithPromotionCode() throws Exception {
        mockMvc.perform(post("/api/purchase/package/{packageid}", testPackage.getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "%s",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId(), testPromotionCode.getCode())))
                .andExpect(status().isCreated());
    }

    // 5. Purchase course with invalid promotion code test
    @Test
    public void testPurchaseCourseWithInvalidPromotionCode() throws Exception {
        mockMvc.perform(post("/api/purchase/course/{courseid}", testCourses.get(0).getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "INVALID_CODE",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId())))
                .andExpect(status().isBadRequest());
    }

    // 6. Purchase package with invalid promotion code test
    @Test
    public void testPurchasePackageWithInvalidPromotionCode() throws Exception {
        mockMvc.perform(post("/api/purchase/package/{packageid}", testPackage.getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "INVALID_CODE",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId())))
                .andExpect(status().isBadRequest());
    }

    // 7. Purchase course with expired promotion code test
    @Test
    public void testPurchaseCourseWithExpiredPromotionCode() throws Exception {
        // Create expired promotion code
        PromotionCode expiredCode = PromotionCode.builder()
                .code("EXPIRED25")
                .description("Expired promotion code")
                .discountPercentage(new BigDecimal("25.00"))
                .discountAmount(BigDecimal.ZERO)
                .maxUses(100)
                .currentUses(0)
                .validFrom(LocalDateTime.now().minusDays(30))
                .validUntil(LocalDateTime.now().minusDays(1))
                .applicableToCourses(true)
                .applicableToPackages(true)
                .isActive(true)
                .build();
        promotionCodeRepository.save(expiredCode);

        mockMvc.perform(post("/api/purchase/course/{courseid}", testCourses.get(0).getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "%s",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId(), expiredCode.getCode())))
                .andExpect(status().isBadRequest());
    }

    // 8. Purchase package with expired promotion code test
    @Test
    public void testPurchasePackageWithExpiredPromotionCode() throws Exception {
        // Create expired promotion code
        PromotionCode expiredCode = PromotionCode.builder()
                .code("EXPIRED_PKG")
                .description("Expired promotion code for packages")
                .discountPercentage(new BigDecimal("30.00"))
                .discountAmount(BigDecimal.ZERO)
                .maxUses(50)
                .currentUses(0)
                .validFrom(LocalDateTime.now().minusDays(30))
                .validUntil(LocalDateTime.now().minusDays(1))
                .applicableToCourses(true)
                .applicableToPackages(true)
                .isActive(true)
                .build();
        promotionCodeRepository.save(expiredCode);

        mockMvc.perform(post("/api/purchase/package/{packageid}", testPackage.getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "%s",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId(), expiredCode.getCode())))
                .andExpect(status().isBadRequest());
    }

    // 9. Purchase course with maxed out promotion code test
    @Test
    public void testPurchaseCourseWithMaxedOutPromotionCode() throws Exception {
        // Create maxed out promotion code
        PromotionCode maxedCode = PromotionCode.builder()
                .code("MAXED_OUT")
                .description("Maxed out promotion code")
                .discountPercentage(new BigDecimal("20.00"))
                .discountAmount(BigDecimal.ZERO)
                .maxUses(1)
                .currentUses(1)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .applicableToCourses(true)
                .applicableToPackages(true)
                .isActive(true)
                .build();
        promotionCodeRepository.save(maxedCode);

        mockMvc.perform(post("/api/purchase/course/{courseid}", testCourses.get(0).getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "%s",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId(), maxedCode.getCode())))
                .andExpect(status().isBadRequest());
    }

    // 10. Purchase package with maxed out promotion code test
    @Test
    public void testPurchasePackageWithMaxedOutPromotionCode() throws Exception {
        // Create maxed out promotion code
        PromotionCode maxedCode = PromotionCode.builder()
                .code("PKG_MAXED")
                .description("Maxed out promotion code for packages")
                .discountPercentage(new BigDecimal("15.00"))
                .discountAmount(BigDecimal.ZERO)
                .maxUses(2)
                .currentUses(2)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .applicableToCourses(true)
                .applicableToPackages(true)
                .isActive(true)
                .build();
        promotionCodeRepository.save(maxedCode);

        mockMvc.perform(post("/api/purchase/package/{packageid}", testPackage.getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "%s",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId(), maxedCode.getCode())))
                .andExpect(status().isBadRequest());
    }

    // 11. Purchase package with non-applicable promotion code to package test
    @Test
    public void testPurchasePackageWithNonApplicablePromotionCode() throws Exception {
        // Create course-only promotion code
        PromotionCode courseOnlyCode = PromotionCode.builder()
                .code("COURSE_ONLY")
                .description("Course only promotion code")
                .discountPercentage(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .maxUses(100)
                .currentUses(0)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .applicableToCourses(true)
                .applicableToPackages(false)
                .isActive(true)
                .build();
        promotionCodeRepository.save(courseOnlyCode);

        mockMvc.perform(post("/api/purchase/package/{packageid}", testPackage.getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "%s",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId(), courseOnlyCode.getCode())))
                .andExpect(status().isBadRequest());
    }

    // 12. Purchase course with non-applicable promotion code to course test
    @Test
    public void testPurchaseCourseWithNonApplicablePromotionCode() throws Exception {
        // Create package-only promotion code
        PromotionCode packageOnlyCode = PromotionCode.builder()
                .code("PACKAGE_ONLY")
                .description("Package only promotion code")
                .discountPercentage(new BigDecimal("35.00"))
                .discountAmount(BigDecimal.ZERO)
                .maxUses(50)
                .currentUses(0)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .applicableToCourses(false)
                .applicableToPackages(true)
                .isActive(true)
                .build();
        promotionCodeRepository.save(packageOnlyCode);

        mockMvc.perform(post("/api/purchase/course/{courseid}", testCourses.get(0).getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": "%s",
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId(), packageOnlyCode.getCode())))
                .andExpect(status().isBadRequest());
    }

    // 13. Purchase non-existent course test
    @Test
    public void testPurchaseNonExistentCourse() throws Exception {
        Long nonExistentCourseId = 99999L;
        
        mockMvc.perform(post("/api/purchase/course/{courseid}", nonExistentCourseId)
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": null,
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId())))
                .andExpect(status().isNotFound());
    }

    // 14. Purchase non-existent package test
    @Test
    public void testPurchaseNonExistentPackage() throws Exception {
        Long nonExistentPackageId = 99999L;
        
        mockMvc.perform(post("/api/purchase/package/{packageid}", nonExistentPackageId)
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": null,
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId())))
                .andExpect(status().isNotFound());
    }

    // 15. Invalid purchase request test (missing required fields)
    @Test
    public void testInvalidPurchaseRequest() throws Exception {
        mockMvc.perform(post("/api/purchase/course/{courseid}", testCourses.get(0).getId())
                .contentType("application/json")
                .content("""
                    {
                        "promotionCode": null
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    // 16. Purchase unpublished course test
    @Test
    public void testPurchaseUnpublishedCourse() throws Exception {
        // Create an unpublished course
        Course unpublishedCourse = Course.builder()
                .name("Unpublished Course")
                .description("This course is not published")
                .oneTimePrice(new BigDecimal("99.99"))
                .subscriptionPriceMonthly(new BigDecimal("19.99"))
                .allowsSubscription(true)
                .currency(Currency.USD)
                .status(CourseStatus.DRAFT)
                .difficultyLevel(DifficultyLevel.BIGINNER)
                .createdBy(testUser)
                .isActive(true)
                .build();
        Course savedUnpublishedCourse = courseRepository.save(unpublishedCourse);

        mockMvc.perform(post("/api/purchase/course/{courseid}", savedUnpublishedCourse.getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": null,
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId())))
                .andExpect(status().isBadRequest());
    }

    // 17. Purchase unpublished package test
    @Test
    public void testPurchaseUnpublishedPackage() throws Exception {
        // Create an inactive package
        Package inactivePackage = Package.builder()
                .name("Inactive Package")
                .description("This package is inactive")
                .price(new BigDecimal("199.99"))
                .isActive(false)
                .build();
        Package savedInactivePackage = packageRepository.save(inactivePackage);

        mockMvc.perform(post("/api/purchase/package/{packageid}", savedInactivePackage.getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": %d,
                        "promotionCode": null,
                        "stripePaymentIntentId": "pi_test123",
                        "stripeSessionId": "cs_test123"
                    }
                    """.formatted(testUser.getId())))
                .andExpect(status().isBadRequest());
    }

    // 18. Purchase course with invalid data test
    @Test
    public void testPurchaseCourseWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/purchase/course/{courseid}", testCourses.get(0).getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": -1,
                        "promotionCode": null,
                        "stripePaymentIntentId": "",
                        "stripeSessionId": ""
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    // 19. Purchase package with invalid data test
    @Test
    public void testPurchasePackageWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/purchase/package/{packageid}", testPackage.getId())
                .contentType("application/json")
                .content("""
                    {
                        "userId": null,
                        "promotionCode": null,
                        "stripePaymentIntentId": null,
                        "stripeSessionId": null
                    }
                    """))
                .andExpect(status().isBadRequest());
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

        UserEntity user1 = UserEntity.builder()
                .name("Test User 1")
                .email("testuser1@example.com")
                .phone("+1234567891")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .isActive(true)
                .emailVerified(true)
                .bio("Test user for integration tests")
                .build();
        userRepository.save(user1);

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