export interface Attachment {
  id?: number;
  file: File | null;
  displayName: string;
}

export interface Lesson {
  id?: number;
  order: number;
  title: string;
  explanation: string;
  whatWeWillLearn: string[];
  videoData?: File;
  thumbnailFile?: File;
  duration: number;
  status: 'Active' | 'Inactive';
  attachments: Attachment[];
  prerequisites: string[];
  state?: 'saved' | 'edited' | 'new';
}
