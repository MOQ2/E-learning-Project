export interface EnrolledCourseDto {
  courseId: number;
  courseName: string;
  description: string;
  instructor: string;
  thumbnailUrl?: string;
  category: string;
  difficultyLevel: string;
  status: string;

  // Progress tracking
  totalModules: number;
  completedModules: number;
  totalLessons: number;
  completedLessons: number;
  totalQuizzes: number;
  completedQuizzes: number;
  progressPercentage: number;
  totalDurationMinutes: number;
  watchedDurationMinutes: number;

  // Access information
  accessType: string;
  enrolledDate: string;
  accessUntil?: string;
  isActive: boolean;
  hasLifetimeAccess: boolean;
  daysRemaining?: number;

  // Additional info
  lastAccessedDate?: string;
  currentModule?: string;
  currentLesson?: string;
  averageRating?: number;
  pricePaid?: number;

  // Package info
  packageId?: number;
  packageName?: string;
}

export interface MyLearningStatsDto {
  totalEnrolledCourses: number;
  activeCourses: number;
  completedCourses: number;
  totalLessonsCompleted: number;
  totalQuizzesCompleted: number;
  totalLearningHours: number;
  averageProgressPercentage: number;
  certificatesEarned: number;
  currentStreak: number;
}

export interface MyLearningResponseDto {
  stats: MyLearningStatsDto;
  enrolledCourses: EnrolledCourseDto[];
  continueLearning: EnrolledCourseDto[];
  upcomingDeadlines: EnrolledCourseDto[];
}
