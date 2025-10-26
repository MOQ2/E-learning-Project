export interface CourseFilterParams {
  name?: string;
  description?: string;
  minPrice?: number;
  maxPrice?: number;
  currency?: string;
  minDurationHours?: number;
  maxDurationHours?: number;
  isActive?: boolean;
  isFree?: boolean;
  createdByUserId?: number;
  statuses?: string[];
  difficultyLevels?: string[];
  tags?: string[];
  categories?: string[];
  page?: number;
  size?: number;
  sort?: string;
}
