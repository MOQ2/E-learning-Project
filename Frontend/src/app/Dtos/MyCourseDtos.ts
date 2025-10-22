export interface TeacherCourseDto {
  // Basic course info
  courseId: number;
  courseName: string;
  description?: string;
  thumbnailUrl?: string;
  category?: string;
  difficultyLevel?: string;
  status?: string;

  // Course structure
  totalModules: number;
  totalLessons: number;
  totalQuizzes: number;
  totalDurationMinutes: number;

  // Pricing information
  oneTimePrice?: number;
  subscriptionPriceMonthly?: number;
  allowsSubscription?: boolean;
  isFree: boolean;

  // Statistics
  totalEnrollments: number;
  activeEnrollments: number;
  averageProgress: number;
  averageRating?: number;
  totalReviews: number;
  totalRevenue?: number;

  // Timestamps
  createdAt: string;
  updatedAt?: string;
  lastEnrollmentDate?: string;

  // Activity indicators
  isActive: boolean;
  isPublished: boolean;
  recentEnrollments30Days: number;
  completions: number;
}

export interface TeacherStatsDto {
  // Course statistics
  totalCourses: number;
  publishedCourses: number;
  draftCourses: number;
  activeCourses: number;

  // Student statistics
  totalStudents: number;
  activeStudents: number;
  recentEnrollments30Days: number;

  // Revenue statistics
  totalRevenue: number;
  monthlyRevenue: number;

  // Engagement statistics
  averageRating: number;
  totalReviews: number;
  totalCompletions: number;
  averageCompletionRate: number;

  // Content statistics
  totalLessons: number;
  totalModules: number;
  totalQuizzes: number;
}

export interface MyCoursesResponseDto {
  stats: TeacherStatsDto;
  courses: TeacherCourseDto[];
  recentlyUpdatedCourses: TeacherCourseDto[];
  topPerformingCourses: TeacherCourseDto[];
}
