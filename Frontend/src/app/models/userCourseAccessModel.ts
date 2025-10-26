export type AccessType = 'PURCHASED' | 'PACKAGE_ACCESS' | 'SUBSCRIPTION_ACCESS' | 'FREE';

export interface UserCourseAccessToDto {
  userId: number;
  courseId: number;
  accessType: AccessType;
  accessUntil?: string;
  paymentId?: number;
}

export interface UserCourseAccessFromDto {
  id: number;
  userId: number;
  userEmail: string;
  courseId: number;
  courseName: string;
  accessType: string;
  accessUntil?: string;
  isActive: boolean;
  paymentId?: number;
  createdAt: string;
  updatedAt: string;
}
