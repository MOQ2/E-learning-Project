export interface CourseoverviewCreationDto {
  id: number | null;
  name: string;
  description: string;
  estimatedDurationInHours: number | null;
  difficultyLevel: 'BIGINNER' | 'INTERMEDIATE' | 'EXPERT' | '';
  status: 'DRAFT' | 'PUBLISHED' ;
  currency: String;
  category: String;
  thumbnail: number;
  thumbnailName: string;
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
