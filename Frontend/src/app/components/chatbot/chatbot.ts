import { ChangeDetectionStrategy, Component, computed, effect, input, output, signal, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RagService, ChatMessage as RagChatMessage, RecommendedCourse } from '../../Services/rag.service';
import { Subscription } from 'rxjs';

// Helper Interfaces for structuring chat content
interface Course {
  id: number;
  imageUrl: string;
  title: string;
  level: string;
  duration: string;
  feature: string;
  enrolled: boolean;
}

interface MessageContentText {
  type: 'text';
  header?: string;
  body: string | string[];
}

interface MessageContentCourses {
    type: 'courses';
    header: string;
    courses: Course[];
}

interface ChatMessage {
  sender: 'user' | 'assistant';
  timestamp: string;
  content: MessageContentText | MessageContentCourses;
}


@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chatbot.html',
  styleUrls: ['./chatbot.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Chatbot implements OnInit, OnDestroy {
  // --- INPUTS ---
  // The main title displayed in the chat header.
  chatTitle = input<string>('Course Assistant');
  // You can set this from the parent component to control visibility. Defaults to true.
  isOpen = input<boolean>(true);

  // Expose the global Array constructor so templates can call Array.isArray, Array.from, etc.
  readonly Array: ArrayConstructor = Array;

  // --- OUTPUTS ---
  // Emits the new visibility state (true/false) whenever it changes.
  isOpenChange = output<boolean>();

  // --- STATE MANAGEMENT ---
  // Signal to hold the current value of the input field.
  newMessage = signal<string>('');

  // Signal to hold the array of all chat messages from RAG service.
  messages = signal<ChatMessage[]>([]);
  // Internal signal to manage the open/closed state of the chat window.
  internalIsOpen = signal<boolean>(true);
  // Signal to track loading state
  isLoading = signal<boolean>(false);

  // Subscriptions
  private subscriptions: Subscription[] = [];

  constructor(private ragService: RagService) {
    // Initialize internal state from the input property.
    this.internalIsOpen.set(this.isOpen());
  }

  ngOnInit(): void {
    // Subscribe to messages from RAG service
    const messagesSubscription = this.ragService.messages$.subscribe(ragMessages => {
      const convertedMessages = this.convertRagMessages(ragMessages);
      this.messages.set(convertedMessages);
    });

    // Subscribe to loading state
    const loadingSubscription = this.ragService.loading$.subscribe(loading => {
      this.isLoading.set(loading);
    });

    this.subscriptions.push(messagesSubscription, loadingSubscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  // --- METHODS ---
  /**
   * Handles sending a new message.
   * It sends the message to the RAG service and clears the input field.
   */
  sendMessage(): void {
    const text = this.newMessage().trim();
    if (!text || this.isLoading()) return;

    // Send message to RAG service
    this.ragService.sendMessage(text);

    // Clear the input
    this.newMessage.set('');
  }

  /**
   * Convert RAG service messages to component messages format
   */
  private convertRagMessages(ragMessages: RagChatMessage[]): ChatMessage[] {
    return ragMessages.map(ragMessage => {
      const timestamp = this.formatTimestamp(ragMessage.timestamp);

      if (ragMessage.messageType === 'course_recommendations' && ragMessage.courses) {
        // Convert recommended courses to the component's course format
        const courses: Course[] = ragMessage.courses.map(course => ({
          id: course.course_id,
          imageUrl: `https://placehold.co/150x150/E9D5FF/4C1D95?text=${encodeURIComponent(course.category)}`,
          title: course.name,
          level: course.difficulty_level || 'Beginner',
          duration: course.duration_hours ? `${course.duration_hours}h` : '—',
          feature: course.price === 0 ? 'Free' : `${course.price} ${course.currency}`,
          enrolled: false
        }));

        return {
          sender: ragMessage.sender,
          timestamp: timestamp,
          content: {
            type: 'courses',
            header: 'Recommended Courses',
            courses: courses
          }
        };
      } else {
        // Regular text message
        return {
          sender: ragMessage.sender,
          timestamp: timestamp,
          content: {
            type: 'text',
            body: ragMessage.content
          }
        };
      }
    });
  }

  /**
   * Format timestamp for display
   */
  private formatTimestamp(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);

    if (minutes < 1) return 'just now';
    if (minutes === 1) return '1 minute ago';
    if (minutes < 60) return `${minutes} minutes ago`;

    const hours = Math.floor(minutes / 60);
    if (hours === 1) return '1 hour ago';
    if (hours < 24) return `${hours} hours ago`;

    return date.toLocaleDateString();
  }

  /**
   * Toggles the visibility of the chat window and emits the new state.
   */
  toggleChat(): void {
    this.internalIsOpen.update(open => !open);
    this.isOpenChange.emit(this.internalIsOpen());
  }

  /**
   * Start a new chat session
   */
  newChat(): void {
    this.ragService.clearChat().subscribe({
      next: () => {
        console.log('New chat session started');
      },
      error: (error) => {
        console.error('Error starting new chat:', error);
      }
    });
  }

  /**
   * Handle keyboard events for input
   */
  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  /**
   * Handle course enrollment or viewing
   */
  onCourseAction(course: Course): void {
    // Navigate to course page for enrollment/view
    const url = `http://localhost:4200/course/${course.id}`;
    window.open(url, '_blank');
  }

}
