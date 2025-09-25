import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {QuizzesService} from '../../Services/Quizzes/quizzesService';
import {QuizSubmissionResponseDTO, StudentAnswerResponseDTO} from '../../models/quizzesDto';
import {DatePipe, NgClass} from '@angular/common';
import {UserService} from '../../Services/User/user-service';

@Component({
  selector: 'app-quiz-submission',
  imports: [
    DatePipe,
    NgClass
  ],
  templateUrl: './quiz-submmision.html',
  styleUrl: './quiz-submmision.css'
})
export class QuizSubmission implements OnInit {
  quizId: number | null = null;
  submissions: QuizSubmissionResponseDTO[] = [];
  profileImage: string = '';
  selectedAnswers: StudentAnswerResponseDTO[] = [];
  showModal: boolean = false;

  constructor(private route: ActivatedRoute,private quizService: QuizzesService, private userService: UserService) {
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    this.quizId = id ? Number(id) : null;
    this.profileImage = this.userService.getUser()?.profile_picture || '';
    if (this.quizId !== null) {
      this.quizService.getSubmissions(this.quizId).subscribe({
        next: (submissions) => {
          this.submissions = submissions;
        },
        error: (err) => console.error(err)
      });
    }
  }

  viewSubmission(submissionId: number) {
    this.quizService.getSubmissionAnswers(submissionId).subscribe({
      next: (res) => {
        this.selectedAnswers = res;
        this.showModal = true;
      },
      error: (err) => console.error(err)
    });
  }

  closeModal() {
    this.showModal = false;
    this.selectedAnswers = [];
  }
}
