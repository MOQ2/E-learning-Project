export interface CourseoverviewCreationDto {
  id: string | null;
  name: string;
  description: string;
  estimatedDurationInHours: number | null;
  difficultyLevel: 'beginner' | 'intermediate' | 'expert' | '';
  status: 'DRAFT' | 'PUBLISHED';
  currency: 'USD' | 'EUR' | '';
  category: 'design' | 'development' | '';
  thumbnail: string;
  thumbnailName: string;
  previewVideoUrl: string;
  createdBy: string; // Should be a user ID
  tags: string[];
  pricing: {
    oneTimePrice: number | null;
    allowsSubscription: boolean;
    subscriptionPriceMonthly: number | null;
    subscriptionPrice3Months: number | null;
    subscriptionPrice6Months: number | null;
  };
  isActive: boolean;
}
