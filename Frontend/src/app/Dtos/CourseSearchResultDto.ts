import { TagDto } from "./TagDto";

export interface CourseSearchResultDto {
  id: number;
  name: string;
  description: string;
  thumbnailUrl?: string | null;
  instructor: string;
  estimatedDurationInHours: number;
  lessonCount?: number | null;
  oneTimePrice?: number | null;
  currency?: string | null;
  category?: string | null;
  tags: TagDto[];
  matchType: string;
}
