import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {
  CreateQuizDTO,
  QuizResponseDTO,
  QuizSubmissionResponseDTO,
  QuizSubmitDTO, StudentAnswerResponseDTO,
  UpdateQuizDTO
} from '../../models/quizzesDto';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class QuizzesService {

  private api = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  createQuiz(videoId: number, quiz: CreateQuizDTO): Observable<any> {
    return this.http.post(`${this.api}/quizzes/${videoId}`, quiz);
  }

  getQuizzes(videoId?: number): Observable<QuizResponseDTO[]> {
    let params = new HttpParams();
    if (videoId) params = params.set('videoId', videoId.toString());
    return this.http.get<QuizResponseDTO[]>(`${this.api}/quizzes/getQuizzes`, { params });
  }

  submitQuiz(quizSubmitDTO: QuizSubmitDTO): Observable<QuizSubmissionResponseDTO> {
    return this.http.post<QuizSubmissionResponseDTO>(`${this.api}/submitQuiz`, quizSubmitDTO);
  }

  updateQuiz(quizId: number, quiz: UpdateQuizDTO): Observable<QuizResponseDTO> {
    return this.http.patch<QuizResponseDTO>(`${this.api}/quizzes/${quizId}`, quiz);
  }

  getSubmissions(quizId: number , userName?: string): Observable<QuizSubmissionResponseDTO[]> {
    let params = new HttpParams();
    if(userName) {
      params= params.set('userName', userName);
    }
    return this.http.get<QuizSubmissionResponseDTO[]>(`${this.api}/${quizId}/attempts`,{params});
  }

  getSubmissionAnswers(submissionId: number) {
    return this.http.get<StudentAnswerResponseDTO[]>(`${this.api}/${submissionId}/answers`);
  }


}
