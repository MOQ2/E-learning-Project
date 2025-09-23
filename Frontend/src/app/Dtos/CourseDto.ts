import { TagDto } from "./TagDto";


export interface CourseDto {
  id: number;
  name: string;
  description: string;
  oneTimePrice: number;
  currency: string;
  thumbnail: string;
  previewVideoUrl: string;
  estimatedDurationInHours: number;
  status: string;
  difficultyLevel: string;
  isActive: boolean;
  tags: TagDto[];
  rating?: number;
  instructor: string;
  ratingsCount?: number;
  students?: number;
}
