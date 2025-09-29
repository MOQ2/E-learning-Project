import {Component, Inject, OnInit} from '@angular/core';
import {QuizResponseDTO, QuizSubmissionResponseDTO, QuizSubmitDTO} from '../../models/quizzesDto';
import {ToastService} from '../../Services/ToastService/toast-service';
import {QuizzesService} from '../../Services/Quizzes/quizzesService';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-submit-quiz',
  imports: [],
  templateUrl: './submit-quiz.html',
  styleUrl: './submit-quiz.css'
})
export class SubmitQuiz implements OnInit{
  loading: boolean = true;
  studentAnswers: { [questionId: number]: number } = {};
  quiz!: QuizResponseDTO;


  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { quiz: QuizResponseDTO },
    protected dialogRef: MatDialogRef<SubmitQuiz>,
     private toast: ToastService , private quizService: QuizzesService) {
  }

  ngOnInit() {
    this.quiz = this.data.quiz;
  }


  submitQuiz() {
    this.loading = true;

    const answers = Object.keys(this.studentAnswers).map(key => ({
      questionId: Number(key),
      selectedOptionId: this.studentAnswers[Number(key)]
    }));

    const quizSubmitDTO: QuizSubmitDTO = {
      quizId: this.quiz.id,
      answers
    };

    this.quizService.submitQuiz(quizSubmitDTO).subscribe({
      next: (res: QuizSubmissionResponseDTO) => {
        this.toast.success(`Quiz submitted! Your Score: ${res.score}`);
        this.dialogRef.close(res);
        this.loading = false;
      },
      error: (err) => {
        this.toast.error(err.error.message || "Server error");
        this.loading = false;
      }
    });
  }
}
