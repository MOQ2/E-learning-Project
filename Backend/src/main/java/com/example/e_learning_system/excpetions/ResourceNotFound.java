package com.example.e_learning_system.excpetions;


import org.springframework.http.HttpStatus;

public class ResourceNotFound extends BaseException{
    public ResourceNotFound(String resouseName , String resouseValue) {
        super( "%s with value %s was not not found".formatted(resouseName, resouseValue),"ResourceNotFound", HttpStatus.NOT_FOUND,null);
    }

    //may use factory mehtod for making exception ...
    public static ResourceNotFound userNotFound(String studnetId){
        return new ResourceNotFound("Student", studnetId);
    }

    public static ResourceNotFound courseNotFound(String courseId){
        return new ResourceNotFound("Course", courseId);
    }
    public static ResourceNotFound teacherNotFound (String teacherId){
        return new ResourceNotFound("Teacher", teacherId);
    }

    public static ResourceNotFound quizzNotFound(String quizzId){
        return new ResourceNotFound("Quizz", quizzId);
    }
    public static ResourceNotFound questionNotFound(String questionId){
        return new ResourceNotFound("Question", questionId);
    }
    public static ResourceNotFound answerNotFound(String answerId){
        return new ResourceNotFound("Answer", answerId);
    }
    public static ResourceNotFound quizzAttempNotFound(String quizzAttemptId){
        return new ResourceNotFound("Quizz Attempt", quizzAttemptId);
    }
    public static ResourceNotFound  moduleNotFound(String moduleId){
        return new ResourceNotFound("Module", moduleId);
    }
    public static ResourceNotFound coursNotFound(String courseId){
        return new ResourceNotFound("Course", courseId);
    }
    public static ResourceNotFound roleNotFound(String roleId){
        return new ResourceNotFound("Role", roleId);
    }
    
    public static ResourceNotFound permissionNotFound(String permissionId){
        return new ResourceNotFound("Permission", permissionId);
    }
    
    public static ResourceNotFound rolePermissionNotFound(String roleId, String permissionId){
        return new ResourceNotFound("Role-Permission relationship", "roleId: " + roleId + ", permissionId: " + permissionId);
    }
    
    public static ResourceNotFound packageNotFound(Long packageId){
        return new ResourceNotFound("Package", packageId.toString());
    }
    
    public static ResourceNotFound promotionCodeNotFound(String promotionCode){
        return new ResourceNotFound("Promotion Code", promotionCode);
    }
    
    public static ResourceNotFound simplePaymentNotFound(Integer paymentId){
        return new ResourceNotFound("Simple Payment", paymentId.toString());
    }
    
    public static ResourceNotFound simplePaymentNotFound(String stripePaymentIntentId){
        return new ResourceNotFound("Simple Payment", stripePaymentIntentId);
    }



}
