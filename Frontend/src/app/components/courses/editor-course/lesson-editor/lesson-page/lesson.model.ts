export interface Attachment {
  file: File | null;
  displayName: string;
}

export interface Lesson {
  id?: number;
  title: string;
  explanation: string;
  whatWeWillLearn: string[];
  videoData?: File;
  thumbnailData?: File;
  duration: number;
  status: 'Active' | 'Inactive';
  attachments: Attachment[];
  prerequisites: string[];
}
