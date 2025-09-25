export interface QuizOptionCreateDTO {
  text: string;
  isCorrect: boolean;
}


export interface QuizQuestionCreateDTO {
  text: string;
  questionMark: number;
  options: QuizOptionCreateDTO[];
}


export interface CreateQuizDTO {
  title: string;
  totalScore: number;
  isActive?: boolean;
  questions: QuizQuestionCreateDTO[];
}

export interface QuizOptionResponseDTO {
  id: number;
  text: string;
  isCorrect: boolean;
}


export interface QuizQuestionResponseDTO {
  id: number;
  text: string;
  questionMark: number;
  options: QuizOptionResponseDTO[];
}


export interface QuizResponseDTO {
  id: number;
  title: string;
  totalScore: number;
  isActive: boolean;
  videoId: number;
  questions: QuizQuestionResponseDTO[];
}

export interface QuizSubmissionResponseDTO {
  submissionId: number;
  userName: string;
  score: number;
  submittedAt: string;
}

export interface StudentAnswerDTO {
  questionId: number;
  selectedOptionId?: number;
}

export interface QuizSubmitDTO {
  quizId: number;
  answers: StudentAnswerDTO[];
}

export interface QuizOptionUpdateDTO {
  id?: number;
  text: string;
  isCorrect: boolean;
}

export interface QuizQuestionUpdateDTO {
  id?: number;
  text: string;
  questionMark: number;
  options: QuizOptionUpdateDTO[];
}

export interface UpdateQuizDTO {
  title: string;
  totalScore: number;
  isActive?: boolean;
  questions: QuizQuestionUpdateDTO[];
}

