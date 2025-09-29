import {Component, OnInit} from '@angular/core';
import {QuizResponseDTO, UpdateQuizDTO} from '../../models/quizzesDto';
import {QuizzesService} from '../../Services/Quizzes/quizzesService';
import {MatDialog} from '@angular/material/dialog';
import {SubmitQuiz} from '../submit-quiz/submit-quiz';
import {ToastService} from '../../Services/ToastService/toast-service';
import {NgClass} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {UserService} from "../../Services/User/user-service";
import {Router} from "@angular/router";

@Component({
    selector: 'app-fetch-quizzes',
    templateUrl: './fetch-quizzes.html',
  imports: [
    FormsModule,
    NgClass
  ],
    styleUrls: ['./fetch-quizzes.css']
})
export class FetchQuizzes implements OnInit {
    quizzes: QuizResponseDTO[] = [];
    loading: boolean = true;
    openModal: boolean = false;
    submitted: boolean = false;
    logicErrors: string[] = [];
    editingQuiz: QuizResponseDTO | null = null;

    quiz: UpdateQuizDTO = {
        title: '',
        totalScore: 0,
        isActive: true,
        questions: [
            {
                text: '',
                questionMark: 0,
                options: [
                    {text: '', isCorrect: false},
                    {text: '', isCorrect: false}
                ]
            }
        ]
    };

    constructor(
        private quizService: QuizzesService,
        private toast: ToastService,
        private dialog: MatDialog,
        private userService: UserService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.fetchQuizzes();
    }

    fetchQuizzes() {
        this.quizService.getQuizzes(1).subscribe({
            next: (res) => {
                this.quizzes = res;
                this.loading = false;
            },
            error: (err) => {
                console.log(err)
                this.loading = false;
            }
        });
    }

    openQuiz(quiz: QuizResponseDTO) {
        if (this.userService.getUser()?.permissions.includes('course:write')) {
            this.router.navigate(['/quizSubmission', quiz.id], {state: {totalScore: quiz.totalScore}});
        } else {
            this.dialog.open(SubmitQuiz, {
                width: '1000px',
                maxWidth: '90vw',
                height: 'auto',
                maxHeight: '90vh',
                data: {quiz}
            });
        }

    }

    openAddModal() {
        this.editingQuiz = null;
        this.quiz = {
            title: '',
            totalScore: 0,
            isActive: true,
            questions: [
                {text: '', questionMark: 0, options: [{text: '', isCorrect: false}, {text: '', isCorrect: false}]}
            ]
        };
        this.submitted = false;
        this.logicErrors = [];
        this.openModal = true;
    }

    openEditModal(quiz: QuizResponseDTO) {
        this.editingQuiz = quiz;
        this.quiz = {
            title: quiz.title,
            totalScore: quiz.totalScore,
            isActive: quiz.isActive,
            questions: quiz.questions.map(q => ({
                id: q.id,
                text: q.text,
                questionMark: q.questionMark,
                options: q.options.map(o => ({
                    id: o.id,
                    text: o.text,
                    isCorrect: o.isCorrect
                }))
            }))
        };
        this.openModal = true;
    }


    getErrors(field: string, qIndex?: number, oIndex?: number): string[] {
        const errors: string[] = [];
        if (field === 'title') {
            if (!this.quiz.title || this.quiz.title.trim() === '') errors.push("Title is required.");
            if (this.quiz.title.length > 500) errors.push("Title must not exceed 500 characters.");
        }
        if (field === 'totalScore') {
            if (!this.quiz.totalScore || this.quiz.totalScore <= 0) errors.push("Total score must be greater than 0.");
        }
        if (field === 'questionText' && qIndex !== undefined) {
            const q = this.quiz.questions[qIndex];
            if (!q.text || q.text.trim() === '') errors.push("Question text is required.");
        }
        if (field === 'questionMark' && qIndex !== undefined) {
            const q = this.quiz.questions[qIndex];
            if (!q.questionMark || q.questionMark <= 0) errors.push("Question mark must be greater than 0.");
        }
        if (field === 'option' && qIndex !== undefined && oIndex !== undefined) {
            const o = this.quiz.questions[qIndex].options[oIndex];
            if (!o.text || o.text.trim() === '') errors.push("Option text is required.");
        }
        return errors;
    }

    validateLogic(): boolean {
        this.submitted = true;
        this.logicErrors = [];
        if (!this.quiz.questions.length) this.logicErrors.push("Quiz must have at least one question.");
        let totalMarks = 0;
        this.quiz.questions.forEach((q, idx) => {
            if (!q.text || q.text.trim() === '') this.logicErrors.push(`Question ${idx + 1} text is required.`);
            if (q.questionMark <= 0) this.logicErrors.push(`Question ${idx + 1} must have mark greater than 0.`);
            if (q.options.length < 2) this.logicErrors.push(`Question ${idx + 1} must have at least 2 options.`);
            const correctOptions = q.options.filter(o => o.isCorrect);
            if (correctOptions.length !== 1) this.logicErrors.push(`Question ${idx + 1} must have exactly one correct option.`);
            totalMarks += q.questionMark;
        });
        if (totalMarks !== this.quiz.totalScore) this.logicErrors.push(`Total score (${this.quiz.totalScore}) does not match sum of question marks (${totalMarks}).`);
        return this.logicErrors.length === 0;
    }

    addQuestion() {
        this.quiz.questions.push({
            text: '',
            questionMark: 0,
            options: [{text: '', isCorrect: false}, {text: '', isCorrect: false}]
        });
    }

    addOption(index: number) {
        this.quiz.questions[index].options.push({text: '', isCorrect: false});
    }

    removeQuestion(index: number) {
        this.quiz.questions.splice(index, 1);
    }

    removeOption(questionIndex: number, optionIndex: number) {
        this.quiz.questions[questionIndex].options.splice(optionIndex, 1);
    }

    markAsCorrect(questionIndex: number, optionIndex: number) {
        const options = this.quiz.questions[questionIndex].options;
        options.forEach(o => o.isCorrect = false);
        options[optionIndex].isCorrect = true;
    }

    saveQuiz() {
        this.submitted = true;

        const hasFieldErrors =
            this.getErrors('title').length > 0 ||
            this.getErrors('totalScore').length > 0 ||
            this.quiz.questions.some((q, qIndex) =>
                this.getErrors('questionText', qIndex).length > 0 ||
                this.getErrors('questionMark', qIndex).length > 0 ||
                q.options.some((o, oIndex) => this.getErrors('option', qIndex, oIndex).length > 0)
            );

        if (hasFieldErrors) return;

        if (!this.validateLogic()) return;

        if (this.editingQuiz) {
            const updateDto: UpdateQuizDTO = {
                title: this.quiz.title,
                totalScore: this.quiz.totalScore,
                isActive: this.quiz.isActive,
                questions: this.quiz.questions.map(q => ({
                    ...(q.id && {id: q.id}),
                    text: q.text,
                    questionMark: q.questionMark,
                    options: q.options.map(o => ({
                        ...(o.id && {id: o.id}),
                        text: o.text,
                        isCorrect: o.isCorrect
                    }))
                }))
            };

            this.quizService.updateQuiz(this.editingQuiz.id, updateDto).subscribe({
                next: (updatedQuiz) => {
                    this.toast.success('Quiz updated successfully!');
                    this.openModal = false;
                    this.editingQuiz = null;
                    this.quizzes = this.quizzes.map(q => q.id === updatedQuiz.id ? updatedQuiz : q);
                },
                error: (err) => this.toast.error(err.error.message)
            });
        } else {
            this.quizService.createQuiz(1, this.quiz).subscribe({
                next: (newQuiz) => {
                    this.toast.success('Quiz created successfully!');
                    this.openModal = false;
                    this.quizzes = [...this.quizzes, newQuiz];
                },
                error: (err) => this.toast.error(err.error.message)
            });
        }
    }
}
