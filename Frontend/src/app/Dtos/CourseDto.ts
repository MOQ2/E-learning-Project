import { TagDto } from "./TagDto";


export interface CourseDto {
  id: number;
  name: string;
  description: string;
  oneTimePrice?: number | null;
  currency?: string | null;
  thumbnail?: number | string | null;
  thumbnailUrl?: string | null;
  previewVideoUrl?: string | null;
  estimatedDurationInHours: number;
  status: string;
  difficultyLevel: string;
  isActive: boolean;
  tags: TagDto[];
  instructor: string;
  averageRating?: number | null;
  reviewCount?: number | null;
  enrolledCount?: number | null;
  category?: string | null;
}
