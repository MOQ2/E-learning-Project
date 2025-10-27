import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
// Using crypto.randomUUID() instead of uuid library

export interface CourseRecommendationRequest {
  chatId: string;
  query: string;
  topK?: number;
}

export interface CourseRecommendationResponse {
  chatId: string;
  response: string;
  recommended_courses: RecommendedCourse[];
  message_type: string;
}

export interface RecommendedCourse {
  course_id: number;
  name: string;
  description: string;
  category: string;
  difficulty_level: string;
  price: number;
  currency: string;
  duration_hours: number;
}

export interface CourseQuestionRequest {
  chatId: string;
  courseId: number;
  query: string;
}

export interface CourseQuestionResponse {
  chatId: string;
  response: string;
  courseId: number;
  message_type: string;
}

export interface ChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  messageType: 'text' | 'course_recommendations';
  courses?: RecommendedCourse[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class RagService {
  private readonly baseUrl = 'http://localhost:5000/api/chatbot';
  private currentChatId: string;

  // Subject to track chat messages
  private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
  public messages$ = this.messagesSubject.asObservable();

  // Subject to track loading state
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {
    this.currentChatId = this.generateChatId();
    this.initializeChat();
  }

  private generateChatId(): string {
    return `chat_${crypto.randomUUID()}`;
  }

  private getHttpOptions() {
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      })
    };
  }

  /**
   * Initialize chat with welcome message
   */
  private initializeChat(): void {
    const welcomeMessage: ChatMessage = {
      id: crypto.randomUUID(),
      sender: 'assistant',
      content: 'Hi! Tell me what you want to learn. Describe your goals, current level, preferred tools, and available time per week.',
      timestamp: new Date(),
      messageType: 'text'
    };
    this.messagesSubject.next([welcomeMessage]);
  }

  /**
   * Get course recommendations based on user query
   */
  getCourseRecommendations(query: string, topK: number = 5): Observable<CourseRecommendationResponse> {
    const request: CourseRecommendationRequest = {
      chatId: this.currentChatId,
      query: query,
      topK: topK
    };

    this.setLoading(true);

    return this.http.post<ApiResponse<CourseRecommendationResponse>>(
      `${this.baseUrl}/recommend`,
      request,
      this.getHttpOptions()
    ).pipe(
      map(response => {
        if (response.success && response.data) {
          return response.data;
        } else {
          throw new Error(response.message || 'Failed to get course recommendations');
        }
      }),
      tap(response => {
        this.addUserMessage(query);
        // First show the assistant narrative text
        this.addAssistantMessage(response.response, 'text');
        // Then show recommended courses as a card
        if (response.recommended_courses && response.recommended_courses.length > 0) {
          this.addAssistantMessage('Here are some courses that match your goal:', 'course_recommendations', response.recommended_courses);
        }
        this.setLoading(false);
      }),
      catchError(error => {
        this.setLoading(false);
        this.handleError(query, error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Ask specific questions about a course
   */
  askAboutCourse(courseId: number, query: string): Observable<CourseQuestionResponse> {
    const request: CourseQuestionRequest = {
      chatId: this.currentChatId,
      courseId: courseId,
      query: query
    };

    this.setLoading(true);

    return this.http.post<ApiResponse<CourseQuestionResponse>>(
      `${this.baseUrl}/ask-about-course`,
      request,
      this.getHttpOptions()
    ).pipe(
      map(response => {
        if (response.success && response.data) {
          return response.data;
        } else {
          throw new Error(response.message || 'Failed to get answer about course');
        }
      }),
      tap(response => {
        this.addUserMessage(query);
        this.addAssistantMessage(response.response, 'text');
        this.setLoading(false);
      }),
      catchError(error => {
        this.setLoading(false);
        this.handleError(query, error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Clear chat history and start a new chat session
   */
  clearChat(): Observable<any> {
    const request = { chatId: this.currentChatId };

    return this.http.post<ApiResponse<any>>(
      `${this.baseUrl}/clear-chat`,
      request,
      this.getHttpOptions()
    ).pipe(
      tap(() => {
        // Generate new chat ID and reset messages
        this.currentChatId = this.generateChatId();
        this.initializeChat();
      }),
      catchError(this.handleHttpError)
    );
  }

  /**
   * Get health status of the RAG service
   */
  getHealthStatus(): Observable<any> {
    return this.http.get<ApiResponse<any>>(
      `${this.baseUrl}/health`,
      this.getHttpOptions()
    ).pipe(
      catchError(this.handleHttpError)
    );
  }

  /**
   * Add user message to chat
   */
  private addUserMessage(content: string): void {
    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      sender: 'user',
      content: content,
      timestamp: new Date(),
      messageType: 'text'
    };

    const currentMessages = this.messagesSubject.value;
    this.messagesSubject.next([...currentMessages, userMessage]);
  }

  /**
   * Add assistant message to chat
   */
  private addAssistantMessage(
    content: string,
    messageType: 'text' | 'course_recommendations' = 'text',
    courses?: RecommendedCourse[]
  ): void {
    const assistantMessage: ChatMessage = {
      id: crypto.randomUUID(),
      sender: 'assistant',
      content: content,
      timestamp: new Date(),
      messageType: messageType,
      courses: courses
    };

    const currentMessages = this.messagesSubject.value;
    this.messagesSubject.next([...currentMessages, assistantMessage]);
  }

  /**
   * Set loading state
   */
  private setLoading(loading: boolean): void {
    this.loadingSubject.next(loading);
  }

  /**
   * Handle API errors
   */
  private handleError(userQuery: string, error: any): void {
    console.error('RAG Service Error:', error);

    // Add user message if it wasn't added yet
    this.addUserMessage(userQuery);

    // Add error message from assistant
    let errorMessage = "I'm sorry, I encountered an error while processing your request. Please try again.";

    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }

    this.addAssistantMessage(errorMessage, 'text');
  }

  /**
   * Handle HTTP errors
   */
  private handleHttpError = (error: HttpErrorResponse): Observable<never> => {
    console.error('HTTP Error:', error);

    let errorMessage = 'An error occurred while communicating with the server.';

    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      errorMessage = 'Unable to connect to the server. Please check your connection.';
    } else if (error.status >= 400 && error.status < 500) {
      errorMessage = 'Invalid request. Please try again.';
    } else if (error.status >= 500) {
      errorMessage = 'Server error. Please try again later.';
    }

    return throwError(() => new Error(errorMessage));
  };

  /**
   * Get current chat ID
   */
  getCurrentChatId(): string {
    return this.currentChatId;
  }

  /**
   * Get current messages
   */
  getCurrentMessages(): ChatMessage[] {
    return this.messagesSubject.value;
  }

  /**
   * Send a message and get recommendations
   */
  sendMessage(query: string): void {
    this.getCourseRecommendations(query).subscribe({
      next: (response) => {
        console.log('Course recommendations received:', response);
      },
      error: (error) => {
        console.error('Error getting course recommendations:', error);
      }
    });
  }
}
